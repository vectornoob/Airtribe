package com.taskmanager.app;

import com.taskmanager.api.TaskApi;
import com.taskmanager.event.AuditTrailListener;
import com.taskmanager.event.ConsoleLogListener;
import com.taskmanager.model.ApiResponse;
import com.taskmanager.model.TaskRequest;
import com.taskmanager.parser.HttpLlmClient;
import com.taskmanager.parser.ParserFactory;
import com.taskmanager.parser.RuleBasedTaskParser;
import com.taskmanager.parser.TaskParser;
import com.taskmanager.query.TaskQuery;
import com.taskmanager.repository.InMemoryTaskRepository;
import com.taskmanager.service.BreakdownStrategy;
import com.taskmanager.service.IdempotencyStore;
import com.taskmanager.service.LlmBreakdownStrategy;
import com.taskmanager.service.RuleBasedBreakdownStrategy;
import com.taskmanager.service.TaskService;

/**
 * Demonstration driver for the method-based API required by the assignment.
 *
 * Run normally to select the LLM automatically when LLM_API_KEY is set;
 * otherwise the rule-based offline strategies are used.
 * Use --offline to force offline mode or --llm to require a configured LLM.
 */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        boolean forceOffline = hasArg(args, "--offline");
        boolean requireLlm = hasArg(args, "--llm");
        String apiKey = System.getenv("LLM_API_KEY");
        boolean llmConfigured = apiKey != null && !apiKey.isBlank();

        if (forceOffline && requireLlm) {
            System.err.println("Choose either --offline or --llm, not both.");
            System.exit(2);
            return;
        }

        TaskParser parser;
        BreakdownStrategy breakdown;
        if (forceOffline || (!requireLlm && !llmConfigured)) {
            parser = new RuleBasedTaskParser();
            breakdown = new RuleBasedBreakdownStrategy();
            System.out.println("Mode: OFFLINE (rule-based parser and breakdown)");
        } else {
            try {
                parser = requireLlm ? ParserFactory.createLlmRequired() : ParserFactory.create();
            } catch (RuntimeException configurationError) {
                System.err.println("Cannot start LLM mode: " + configurationError.getMessage());
                System.err.println("Set LLM_API_KEY or run Main with --offline.");
                System.exit(1);
                return;
            }
            String provider = System.getenv().getOrDefault("LLM_PROVIDER", "openai");
            breakdown = new LlmBreakdownStrategy(new HttpLlmClient(provider, apiKey));
            System.out.println("Mode: LLM (" + provider + "). Live calls may incur provider charges.");
        }

        System.out.println("Task Manager started");
        InMemoryTaskRepository repository = InMemoryTaskRepository.getInstance();
        repository.clear();
        IdempotencyStore.getInstance().clear();

        TaskService service = new TaskService(repository, parser, breakdown);
        ConsoleLogListener console = new ConsoleLogListener();
        AuditTrailListener audit = new AuditTrailListener();
        service.addListener(console);
        service.addListener(audit);
        TaskApi api = new TaskApi(service);

        TaskRequest rentRequest = new TaskRequest(
                "Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH");
        ApiResponse createResponse = api.createTask(rentRequest, "demo-rent-create-001");
        System.out.println("\n1. Create task: " + createResponse);
        System.out.println("2. Retry same request/key (idempotency): "
                + api.createTask(rentRequest, "demo-rent-create-001"));

        int rentId = responseId(createResponse, 1);
        System.out.println("3. Get task: " + api.getTask(rentId));
        System.out.println("4. List tasks: " + api.listTasks());

        TaskRequest updatedRent = new TaskRequest(
                "Pay house rent", "Pay monthly rent to the landlord", "false", "2026-12-01", "HIGH");
        System.out.println("5. Update task: " + api.updateTask(rentId, updatedRent));

        ApiResponse childResponse = api.createSubtask(rentId,
                new TaskRequest("Transfer rent", "Transfer funds to the landlord", "false", null, "HIGH"));
        System.out.println("6. Create subtask: " + childResponse);
        System.out.println("7. List subtasks: " + api.listSubtasks(rentId));

        System.out.println("8. Break down task (LLM when configured, otherwise rules): "
                + api.breakDownTask(rentId, "demo-rent-breakdown-001"));

        System.out.println("9. Invalid request (validation/error shape): "
                + api.createTask(new TaskRequest(" ", "", "yes", "2026-02-30", "URGENT")));

        System.out.println("10. Recurring rent example from the reference repository: "
                + api.createTaskFromText("Remind me to pay rent on the 1st of every month",
                        "demo-recurring-rent-001"));
        System.out.println("11. Create from natural language (relative date): "
                + api.createTaskFromText("Remind me to call the dentist tomorrow", "demo-dentist-text-001"));

        System.out.println("12. Combined search/filter/sort/page: "
                + api.queryTasks(TaskQuery.builder().search("rent").completed(false)
                        .sortBy("DUE_DATE").page(0).size(5).build()));

        System.out.println("13. Audit counts before listener removal: " + audit.getCounts());
        service.removeListener(console);
        System.out.println("14. Complete task after removing console listener: " + api.completeTask(rentId));
        System.out.println("15. Audit counts after completion: " + audit.getCounts());

        System.out.println("16. Repository before deleting parent: " + repository.findAll());
        System.out.println("17. Delete parent (also deletes its children): " + api.deleteTask(rentId));
        System.out.println("18. Repository after deleting parent: " + repository.findAll());

        System.out.println("\nDemo finished. For automated offline checks, run TestRunner.");
    }

    private static boolean hasArg(String[] args, String expected) {
        for (String arg : args) {
            if (expected.equalsIgnoreCase(arg)) return true;
        }
        return false;
    }

    private static int responseId(ApiResponse response, int fallback) {
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300
                || !(response.getBody() instanceof java.util.Map<?, ?> body)
                || !(body.get("id") instanceof Number id)) {
            System.out.println("Task creation failed; subsequent demo uses fallback ID " + fallback
                    + " and will print API errors rather than abort.");
            return fallback;
        }
        return id.intValue();
    }
}
