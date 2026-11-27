# Task Manager API Contract

This is a method-level contract. The project does not start an HTTP server; these Java methods are shaped so they can later be mapped to HTTP routes.

## Operations

| Method | Inputs | Success status | Error statuses |
|---|---|---:|---|
| `createTask(TaskRequest, key?)` | title, description, completed, dueDate?, priority?, key? | 201 | 400 validation; 422 key conflict |
| `listTasks()` | none | 200 | 500 |
| `getTask(id)` | integer ID | 200 | 404 |
| `updateTask(id, request)` | ID and request | 200 | 400, 404 |
| `completeTask(id)` | ID | 200 | 404 |
| `deleteTask(id)` | ID | 200 | 404 |
| `createSubtask(parentId, request)` | parent ID and request | 201 | 400 invalid parent/task; 404 |
| `listSubtasks(parentId)` | parent ID | 200 | 404 |
| `breakDownTask(id, key?)` | parent task ID, key? | 201 | 400 subtask cannot be broken down; 404; 502 invalid/model failure; 422 key conflict |
| `createTaskFromText(text, key?)` | natural-language text, key? | 201 | 400 validation; 502 parser/model failure; 503 missing LLM config; 422 key conflict |
| `queryTasks(TaskQuery)` | optional search/status/priority/parentId/page/size/sortBy | 200 | 500 |

`sortBy` accepts `TITLE`, `DUE_DATE`, `PRIORITY`, or `ID`; page is zero-based and size must be positive. A page beyond the end returns an empty list.

## TaskRequest
All request fields are raw strings so the service performs validation:
- `title`: required, nonblank
- `description`: required, nonblank
- `completed`: exactly `true` or `false`
- `dueDate`: optional, valid `YYYY-MM-DD`
- `priority`: `LOW`, `MEDIUM`, or `HIGH`

## Error body
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message",
    "details": [{"field": "fieldName", "issue": "reason"}]
  }
}
```
`code` and `message` are strings. `details` is an array; validation errors contain one or more field issues, while non-validation errors use an empty array.

## Error codes and statuses
| Status | Code | Meaning |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | Request fields invalid or a subtask operation violates task hierarchy |
| 404 | `NOT_FOUND` | Task ID does not exist |
| 422 | `IDEMPOTENCY_CONFLICT` | Same idempotency key reused for a different request |
| 502 | `LLM_BAD_GATEWAY` | LLM/parser call failed or output could not be validated |
| 503 | `SERVICE_UNAVAILABLE` | Required LLM configuration unavailable |
| 500 | `INTERNAL_ERROR` | Unexpected exception |

## Idempotency
Optional keys are supported by task creation, natural-language creation, and breakdown. Same key + same fingerprint returns the stored successful response. Same key + different fingerprint returns 422. Failed responses are not stored, allowing a corrected retry with the same key.
