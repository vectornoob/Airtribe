package com.taskmanager.app;

import com.taskmanager.api.TaskApi;
import com.taskmanager.event.AuditTrailListener;
import com.taskmanager.event.TaskEventListener;
import com.taskmanager.model.ApiResponse;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskRequest;
import com.taskmanager.parser.FakeLlmClient;
import com.taskmanager.parser.FaultyLlmClient;
import com.taskmanager.parser.LlmTaskParser;
import com.taskmanager.parser.RuleBasedTaskParser;
import com.taskmanager.query.TaskQuery;
import com.taskmanager.repository.InMemoryTaskRepository;
import com.taskmanager.service.BreakdownStrategy;
import com.taskmanager.service.IdempotencyStore;
import com.taskmanager.service.LlmBreakdownStrategy;
import com.taskmanager.service.RuleBasedBreakdownStrategy;
import com.taskmanager.service.TaskService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Offline acceptance checks. Runs without a test framework, API key, or internet connection. */
public final class TestRunner {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        InMemoryTaskRepository repository = InMemoryTaskRepository.getInstance();
        repository.clear();
        IdempotencyStore.getInstance().clear();

        TaskService service = new TaskService(repository, new RuleBasedTaskParser(), new RuleBasedBreakdownStrategy());
        TaskApi api = new TaskApi(service);
        AuditTrailListener audit = new AuditTrailListener();
        AtomicInteger removableListenerCalls = new AtomicInteger();
        TaskEventListener removableListener = (event, task) -> removableListenerCalls.incrementAndGet();
        service.addListener(audit);
        service.addListener(removableListener);
        var monthly = new RuleBasedTaskParser().parse("Remind me to pay rent on the 1st of every month", "2026-11-27");
        check("rule parser detects monthly recurrence, next first day, and preserves source text", "monthly".equals(monthly.recurrence()) && "2026-12-01".equals(monthly.dueDate()) && "Pay rent".equals(monthly.title()) && "Remind me to pay rent on the 1st of every month".equals(monthly.description()));
        var tomorrow = new RuleBasedTaskParser().parse("Remind me to call the dentist tomorrow", "2026-11-27");
        check("rule parser resolves tomorrow using supplied today", "2026-11-28".equals(tomorrow.dueDate()));

        TaskRequest rejectedFirst = request("", "Missing title", "false", null, "LOW");
        check("failed idempotent request is not stored", api.createTask(rejectedFirst, "fixable-key").getStatusCode() == 400);
        check("corrected request can reuse key after previous failure", api.createTask(request("Fixed title", "Fixed description", "false", null, "LOW"), "fixable-key").getStatusCode() == 201);

        TaskRequest rent = request("Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH");
        ApiResponse created = api.createTask(rent, "create-rent-001");
        check("createTask returns 201", created.getStatusCode() == 201);
        int parentId = taskId(created);
        int countAfterCreate = repository.findAll().size();
        check("same key and same request replays response without another task",
                api.createTask(rent, "create-rent-001").getStatusCode() == 201 && repository.findAll().size() == countAfterCreate);
        check("same key with different request returns 422",
                api.createTask(request("Different", "Description", "false", null, "LOW"), "create-rent-001").getStatusCode() == 422);

        check("listTasks returns 200 and a list", api.listTasks().getStatusCode() == 200 && api.listTasks().getBody() instanceof List<?>);
        check("getTask returns existing task", api.getTask(parentId).getStatusCode() == 200);
        check("unknown ID returns 404", api.getTask(9999).getStatusCode() == 404);

        int countBeforeInvalid = repository.findAll().size();
        ApiResponse invalid = api.createTask(request(" ", "", "yes", "2026-02-30", "URGENT"));
        check("title, description, completed, date and priority validations aggregate into 400", invalid.getStatusCode() == 400);
        check("validation error has required shape and string code/message", hasValidationShape(invalid));
        check("failed validation does not persist a task", repository.findAll().size() == countBeforeInvalid);
        check("blank title alone is rejected", api.createTask(request("", "Description", "false", null, "LOW")).getStatusCode() == 400);
        check("blank description alone is rejected", api.createTask(request("Title", " ", "false", null, "LOW")).getStatusCode() == 400);
        check("completed accepts only lowercase true/false", api.createTask(request("Title", "Description", "TRUE", null, "LOW")).getStatusCode() == 400);
        check("priority accepts only LOW/MEDIUM/HIGH", api.createTask(request("Title", "Description", "false", null, "URGENT")).getStatusCode() == 400);
        check("impossible calendar date is rejected", api.createTask(request("Title", "Description", "false", "2026-02-30", "LOW")).getStatusCode() == 400);
        check("valid leap-day date is accepted", api.createTask(request("Leap day", "Date check", "false", "2028-02-29", "LOW")).getStatusCode() == 201);

        check("updateTask returns 200", api.updateTask(parentId,
                request("Pay rent", "Pay monthly rent to landlord", "false", "2026-12-01", "HIGH")).getStatusCode() == 200);
        check("completeTask returns 200", api.completeTask(parentId).getStatusCode() == 200);
        check("updated/completed events reach Observer", audit.getCounts().containsKey("UPDATED") && audit.getCounts().containsKey("COMPLETED"));

        check("createSubtask returns 201", api.createSubtask(parentId,
                request("Transfer money", "Transfer rent to landlord", "false", null, "HIGH")).getStatusCode() == 201);
        check("listSubtasks returns the child", ((List<?>) api.listSubtasks(parentId).getBody()).size() == 1);
        Task subtask = repository.findAll().stream().filter(t -> t.getParentId() != null && t.getParentId() == parentId).findFirst().orElseThrow();
        check("a subtask cannot receive another subtask", api.createSubtask(subtask.getId(), request("Nested", "Not allowed", "false", null, "LOW")).getStatusCode() == 400);
        check("a subtask cannot be broken down", api.breakDownTask(subtask.getId()).getStatusCode() == 400);

        // Search is checked before completion in the earlier example, so complete the parent back to false via update.
        api.updateTask(parentId, request("Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH"));
        check("combined search/filter/sort/pagination works", api.queryTasks(TaskQuery.builder()
                .search("rent").completed(false).priority("HIGH").sortBy("DUE_DATE").page(0).size(5).build()).getStatusCode() == 200);
        check("other sort strategies work", api.queryTasks(TaskQuery.builder().sortBy("PRIORITY").build()).getStatusCode() == 200
                && api.queryTasks(TaskQuery.builder().sortBy("ID").build()).getStatusCode() == 200
                && api.queryTasks(TaskQuery.builder().sortBy("TITLE").build()).getStatusCode() == 200);
        check("page beyond last page returns empty list", ((List<?>) api.queryTasks(TaskQuery.builder().page(999).size(2).build()).getBody()).isEmpty());
        check("parentId filter works", api.queryTasks(TaskQuery.builder().parentId(parentId).build()).getStatusCode() == 200);

        int beforeBreakdown = repository.findAll().size();
        check("rule-based breakdown creates subtasks", api.breakDownTask(parentId, "break-rent-001").getStatusCode() == 201
                && repository.findAll().size() > beforeBreakdown);
        int afterFirstBreakdown = repository.findAll().size();
        check("breakdown retry does not create duplicates", api.breakDownTask(parentId, "break-rent-001").getStatusCode() == 201
                && repository.findAll().size() == afterFirstBreakdown);

        int beforeBadBatch = repository.findAll().size();
        BreakdownStrategy invalidBatch = task -> List.of(
                request("Valid first item", "Must not be saved early", "false", null, "LOW"),
                request(" ", "Invalid second item", "false", null, "LOW"));
        TaskApi invalidBreakdownApi = new TaskApi(new TaskService(repository, new RuleBasedTaskParser(), invalidBatch));
        check("invalid breakdown returns 502", invalidBreakdownApi.breakDownTask(parentId).getStatusCode() == 502);
        check("invalid breakdown saves none of its batch", repository.findAll().size() == beforeBadBatch);

        TaskApi failingBreakdownApi = new TaskApi(new TaskService(repository, new RuleBasedTaskParser(),
                new LlmBreakdownStrategy(new FaultyLlmClient())));
        check("LLM breakdown timeout returns 502", failingBreakdownApi.breakDownTask(parentId).getStatusCode() == 502);
        check("failed LLM breakdown saves no subtasks", repository.findAll().size() == beforeBadBatch);
        FakeLlmClient breakdownFake = new FakeLlmClient("Collect receipts|Collect rent payment receipts\nConfirm transfer|Check bank transfer status");
        TaskApi llmBreakdownApi = new TaskApi(new TaskService(repository, new RuleBasedTaskParser(), new LlmBreakdownStrategy(breakdownFake)));
        check("LLM breakdown parses fixed title|description format", llmBreakdownApi.breakDownTask(parentId, "llm-breakdown-1").getStatusCode() == 201);
        int breakdownCalls = breakdownFake.getCallCount();
        int afterLlmBreakdown = repository.findAll().size();
        check("LLM breakdown idempotent retry neither duplicates nor calls fake again", llmBreakdownApi.breakDownTask(parentId, "llm-breakdown-1").getStatusCode() == 201
                && repository.findAll().size() == afterLlmBreakdown && breakdownFake.getCallCount() == breakdownCalls);

        FakeLlmClient fake = new FakeLlmClient();
        TaskApi fakeApi = new TaskApi(new TaskService(repository, new LlmTaskParser(fake), new RuleBasedBreakdownStrategy()));
        check("FakeLlmClient creates an AI task without network/key", fakeApi.createTaskFromText("Prepare slides", "ai-001").getStatusCode() == 201);
        int fakeCalls = fake.getCallCount();
        check("idempotent AI retry does not call fake twice", fakeApi.createTaskFromText("Prepare slides", "ai-001").getStatusCode() == 201
                && fake.getCallCount() == fakeCalls);

        int beforeBadModel = repository.findAll().size();
        check("malformed model JSON returns 502 and saves nothing",
                new TaskApi(new TaskService(repository, new LlmTaskParser(new FaultyLlmClient("not JSON")), new RuleBasedBreakdownStrategy()))
                        .createTaskFromText("bad JSON").getStatusCode() == 502 && repository.findAll().size() == beforeBadModel);
        String invalidFields = "{\\\"title\\\":\\\"\\\",\\\"description\\\":\\\"bad\\\",\\\"dueDate\\\":\\\"2026-02-30\\\",\\\"recurrence\\\":null}";
        check("invalid LLM fields return 502 and save nothing",
                new TaskApi(new TaskService(repository, new LlmTaskParser(new FaultyLlmClient(invalidFields)), new RuleBasedBreakdownStrategy()))
                        .createTaskFromText("bad fields").getStatusCode() == 502 && repository.findAll().size() == beforeBadModel);
        check("LLM timeout returns 502",
                new TaskApi(new TaskService(repository, new LlmTaskParser(new FaultyLlmClient()), new RuleBasedBreakdownStrategy()))
                        .createTaskFromText("timeout").getStatusCode() == 502);
        check("missing LLM configuration in LLM-required mode returns 503",
                new TaskApi(new TaskService(repository, null, new RuleBasedBreakdownStrategy())).createTaskFromText("needs model").getStatusCode() == 503);

        int eventCount = removableListenerCalls.get();
        service.removeListener(removableListener);
        api.updateTask(parentId, request("Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH"));
        check("removing an Observer stops its future notifications", removableListenerCalls.get() == eventCount);
        check("audit listener continues receiving events", audit.getCounts().containsKey("CREATED") && audit.getCounts().containsKey("UPDATED"));

        check("delete parent returns 200", api.deleteTask(parentId).getStatusCode() == 200);
        check("deleting parent cascades to all children", repository.findById(parentId).isEmpty()
                && repository.findAll().stream().noneMatch(t -> t.getParentId() != null && t.getParentId() == parentId));
        check("delete event is observed", audit.getCounts().containsKey("DELETED"));

        System.out.println("\nTest summary: " + passed + " PASS, " + failed + " FAIL");
        if (failed > 0) System.exit(1);
    }

    private static TaskRequest request(String title, String description, String completed, String dueDate, String priority) {
        return new TaskRequest(title, description, completed, dueDate, priority);
    }
    private static int taskId(ApiResponse response) {
        return ((Number) ((Map<?, ?>) response.getBody()).get("id")).intValue();
    }
    private static boolean hasValidationShape(ApiResponse response) {
        if (!(response.getBody() instanceof Map<?, ?> outer) || !(outer.get("error") instanceof Map<?, ?> error)) return false;
        return error.get("code") instanceof String && error.get("message") instanceof String
                && error.get("details") instanceof List<?> details && details.size() == 5;
    }
    private static void check(String name, boolean condition) {
        System.out.println((condition ? "PASS" : "FAIL") + " - " + name);
        if (condition) passed++; else failed++;
    }
}
