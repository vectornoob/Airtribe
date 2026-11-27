# Task Manager — Plain Java + Maven

A learning project matching the supplied project brief: a REST-style API implemented as Java methods (no Spring Boot or other web framework). It turns natural-language requests into tasks through a pluggable parser, supports subtasks and breakdown, safe retries, validation, querying, events, and offline tests.

## Requirements
- JDK 17+
- Maven 3.8+ (IntelliJ can import the `pom.xml` and use its bundled Maven)
- Optional: an LLM API key for live model calls

No third-party runtime libraries are required. The project uses Java's built-in `HttpClient` for the optional LLM connection and a small JSON helper for this learning project.

## Open in IntelliJ
1. Extract the ZIP.
2. IntelliJ IDEA → **Open** → select the extracted `task-manager-maven` folder (the folder containing `pom.xml`).
3. Choose **Open as Project** and allow Maven to load.
4. Set Project SDK to JDK 17 or newer (File → Project Structure → Project).
5. Open the Maven tool window and run `Lifecycle → clean`, then `compile`.
6. Run `com.taskmanager.app.Main` or run `mvn exec:java`. Main is a demonstration driver because the assignment asks you to call the method-based API from `Main`; it is not an HTTP server or an interactive task-entry app.
7. Run `com.taskmanager.app.TestRunner` to execute the offline checks.

From a terminal at the project root:
```bash
mvn clean compile
mvn exec:java
mvn exec:java -Dexec.mainClass=com.taskmanager.app.TestRunner
# force offline strategies
mvn exec:java -Dexec.args="--offline"
# require a configured live LLM
mvn exec:java -Dexec.args="--llm"
```
If your Maven version does not honor the overridden main class in the last command, use IntelliJ to run `TestRunner` directly or run:
```bash
mvn clean test-compile
java -cp target/classes com.taskmanager.app.TestRunner
```

## LLM configuration
The project includes a small `LlmClient` abstraction and `HttpLlmClient` implementation for OpenAI Chat Completions, Anthropic Messages, and Gemini generateContent.

Set environment variables in IntelliJ's Run Configuration (Environment variables):
- `LLM_API_KEY` — secret API key
- `LLM_PROVIDER` — `openai`, `anthropic`, or `gemini` (defaults to `openai`)
- Optional provider-specific model variables: `OPENAI_MODEL`, `ANTHROPIC_MODEL`, and `GEMINI_MODEL`. The built-in defaults are examples and may need to be changed to a model enabled for your API account. For OpenAI you may also set `OPENAI_BASE_URL`.

Never commit the API key or `.env`. Main now selects the live LLM parser and breakdown strategy automatically when `LLM_API_KEY` is set. Without a key, it selects rule-based strategies. Use `--offline` to force rules, or `--llm` to require a key. Live calls need a valid provider key, supported model name, network access, and may incur charges. `TestRunner` always uses fake/offline clients and never needs a key.

> Note: The course brief mentions an instructor-supplied LlmClient and SimpleJson scaffold. This downloadable version includes its own minimal equivalents so the project is runnable standalone. The JSON reader intentionally supports the flat JSON string fields used by this task; it is not a general-purpose JSON library.

## Package structure
```text
com.taskmanager
├── model       Task (immutable Builder), TaskRequest, ParsedTask, Priority, ApiResponse
├── repository  TaskRepository, Singleton InMemoryTaskRepository
├── service     TaskService, IdempotencyStore, breakdown strategies
├── parser      TaskParser, LlmTaskParser, RuleBasedTaskParser, LlmClient, factory
├── query       TaskQuery Builder, TaskFilter implementations, SortStrategy
├── event       TaskEventListener, ConsoleLogListener, AuditTrailListener
├── exception   ApiException hierarchy
├── api         TaskApi, ErrorResponseFactory
├── util        JSON serialization and minimal JSON field reader
└── app         Main, TestRunner
```

## API operations (method-based, not HTTP endpoints)
Every operation returns an `ApiResponse` with `statusCode` and JSON-style `body`.

| Operation | Parameters | Success | Typical errors |
|---|---|---:|---|
| `createTask` | `TaskRequest`, optional idempotency key | 201 | 400, 422 |
| `listTasks` | none | 200 | 500 |
| `getTask` | task ID | 200 | 404 |
| `updateTask` | ID, `TaskRequest` | 200 | 400, 404 |
| `completeTask` | ID | 200 | 404 |
| `deleteTask` | ID | 200 | 404 |
| `createSubtask` | parent ID, request | 201 | 400, 404 |
| `listSubtasks` | parent ID | 200 | 404 |
| `breakDownTask` | ID, optional key | 201 | 400, 404, 502, 422 |
| `createTaskFromText` | text, optional key | 201 | 400, 502, 503, 422 |
| `queryTasks` | `TaskQuery` | 200 | 500 |

### Request example
```java
TaskRequest request = new TaskRequest(
    "Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH");
ApiResponse response = api.createTask(request, "rent-001");
System.out.println(response.getStatusCode());
System.out.println(response.toJson());
```

### Error response
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {"field": "title", "issue": "must not be empty"}
    ]
  }
}
```
All error `code` and `message` values are strings. Validation gathers multiple issues in one response. Unknown IDs return 404. Unexpected exceptions return 500 with the same outer error structure.

## Validation rules
- `title` and `description` must be nonblank.
- `completed` must be exactly `true` or `false`.
- `dueDate` is optional; if supplied it must be a valid ISO date (`YYYY-MM-DD`).
- `priority` must be `LOW`, `MEDIUM`, or `HIGH`.
- Parser output goes through the same validation before persistence.

## Design patterns
- **Builder:** immutable `Task` and optional-field `TaskQuery`.
- **Singleton:** in-memory repository and idempotency store.
- **Factory:** `ParserFactory` and `ErrorResponseFactory`.
- **Strategy:** `TaskParser` and `BreakdownStrategy`; filters/sorts are interface-based.
- **Observer:** `TaskService` notifies registered task listeners.

## Prompt design
Task-creation prompt includes today's date so relative dates can be resolved, asks for only a JSON object with named fields, and says not to invent dates. Breakdown prompt requests one `title|description` item per line so the response can be parsed with simple String operations. Regardless of prompt, output is untrusted and validated before saving.

## Tests and hardening
Run `com.taskmanager.app.TestRunner` directly in IntelliJ, or run `mvn exec:java -Dexec.mainClass=com.taskmanager.app.TestRunner`. It currently executes **49 offline checks** and prints PASS/FAIL. Coverage includes every required validation field, impossible and leap-day dates, the error response shape, unknown IDs, create/update/complete/delete, subtasks and hierarchy limits, parent cascade deletion, idempotent create/text/breakdown operations, same-key/different-request conflicts, corrected retry after a failed request, filters and four sort strategies, pagination beyond the last page, observer registration/removal, rule-based date parsing, and LLM success/failure behavior. `FakeLlmClient` and `FaultyLlmClient` require no API key or internet. The tests also confirm malformed model output, provider failures, and invalid breakdown batches return 502 without persisting partial data, and that a repeated AI idempotency key does not call the fake twice.

### Test-generation critique
The initial checklist did not fully exercise multi-field validation aggregation, the all-or-nothing rule for a batch of generated subtasks, observer removal, recovery using the same key after a failed request, or the monthly recurrence parser. Those gaps were added to `TestRunner` and checked again.

## Submission checklist
- `mvn clean compile` succeeds from a clean checkout.
- `Main` and `TestRunner` run.
- README and API contract are included.
- No API key or `.env` is committed.

## Example calls and representative console output
The following are method calls on the `TaskApi` object created in `Main`. Output bodies are abbreviated where they contain lists. The program intentionally prints a finite demonstration and exits; the brief describes a method-based API, not a continuously running server or interactive menu.

```java
TaskRequest rent = new TaskRequest("Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH");
System.out.println(api.createTask(rent, "rent-001"));
// ApiResponse{statusCode=201, body={"id":1,"title":"Pay rent",...}}

System.out.println(api.listTasks());
// ApiResponse{statusCode=200, body=[{"id":1,"title":"Pay rent",...}]}

System.out.println(api.getTask(1));
// ApiResponse{statusCode=200, body={"id":1,"title":"Pay rent",...}}

System.out.println(api.updateTask(1, new TaskRequest("Pay house rent", "Pay monthly rent", "false", "2026-12-01", "HIGH")));
// ApiResponse{statusCode=200, body={"id":1,"title":"Pay house rent",...}}

System.out.println(api.completeTask(1));
// ApiResponse{statusCode=200, body={"id":1,"completed":true,...}}

System.out.println(api.createSubtask(1, new TaskRequest("Transfer money", "Send rent", "false", null, "HIGH")));
// ApiResponse{statusCode=201, body={"id":2,"parentId":1,...}}

System.out.println(api.listSubtasks(1));
// ApiResponse{statusCode=200, body=[{"id":2,"parentId":1,...}]}

System.out.println(api.breakDownTask(1, "rent-breakdown-001"));
// ApiResponse{statusCode=201, body=[{"id":3,"parentId":1,...}]}

System.out.println(api.queryTasks(TaskQuery.builder().search("rent").completed(false).sortBy("DUE_DATE").page(0).size(5).build()));
// ApiResponse{statusCode=200, body=[...matching tasks...]}

System.out.println(api.createTaskFromText("Remind me to call the dentist tomorrow", "dentist-001"));
// ApiResponse{statusCode=201, body={"id":4,"title":"Call the dentist","dueDate":"<date resolved at runtime>",...}}

System.out.println(api.deleteTask(1));
// ApiResponse{statusCode=200, body={"deleted":true,"id":1}}  (also deletes its children)

System.out.println(api.createTask(new TaskRequest(" ", "", "maybe", "2026-02-30", "URGENT")));
// ApiResponse{statusCode=400, body={"error":{"code":"VALIDATION_ERROR",...}}}
```
## Reference submission comparison

See [`REFERENCE_REPO_COMPARISON.md`](REFERENCE_REPO_COMPARISON.md) for a source-based comparison with the uploaded Vaibhav example. The reference is Django REST Framework/Python, while this assignment requires plain Java method-based API operations. The Java demo now also exercises the example's recurring-rent sentence (`Remind me to pay rent on the 1st of every month`). The reference's LLM function is a placeholder, not a completed provider call.
