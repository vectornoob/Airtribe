# Design Notes

## 1. Purpose and scope

LearnTrack is a console-based student, course, and enrollment management application built with Core Java. It demonstrates object-oriented design and in-memory CRUD-style operations without a database or external framework.

## 2. Layer responsibilities

### `Main` — console interaction

Displays menus, reads user input, calls service methods, and reports results/errors. It is the entry point and keeps the UI in one class, as requested for this learning project.

### `entity` — domain objects

- `Person`: common identity/contact fields and shared `displayDetails()`.
- `Student`: extends `Person`, adding `batch` and `active`; overrides `displayDetails()`.
- `Course`: stores course name, description, duration, and active status.
- `Enrollment`: connects a student ID to a course ID and stores date/status.

Fields are private and exposed through getters/setters to demonstrate encapsulation.

### `service` — business rules

- `StudentService`: validates required student fields and performs student operations.
- `CourseService`: validates required course details and requires a positive duration.
- `EnrollmentService`: checks that student/course IDs exist and are active, prevents duplicate active enrollments, and updates/cancels status.

### `repository` — in-memory storage

Each repository stores its entity objects in an `ArrayList` and provides basic save, find, list, and delete operations. The service layer uses repositories instead of directly manipulating the lists.

### `util`, `constants`, `enums`, and `exception`

- `IdGenerator`: generates separate sequential IDs using static counters.
- `AppConstants`: keeps shared application text in one place.
- `EnrollmentStatus`: limits enrollment status values to `ACTIVE`, `COMPLETED`, and `CANCELLED`.
- `LearnTrackException`: represents expected business-rule failures.

## 3. Object relationships

- `Student` **is a** `Person` (inheritance).
- An `Enrollment` refers to one student ID and one course ID.
- A student and a course may each be referenced by multiple enrollment records over time.
- `EnrollmentService` depends on all three repositories to validate references before creating an enrollment.
- Services depend on repository objects supplied through constructors (constructor injection).

The entity uses IDs rather than holding direct `Student` and `Course` object references. This keeps the enrollment record simple and makes the relationship explicit.

## 4. Object-oriented concepts demonstrated

- **Encapsulation:** private fields and public accessors.
- **Inheritance:** `Student extends Person`.
- **Constructor use:** constructors initialize objects; the student constructor calls `super(...)` to initialize inherited fields.
- **Method overriding:** `Student.displayDetails()` calls `super.displayDetails()` and then prints student-specific data.
- **Polymorphism:** a `Student` can be treated as a `Person` where a `Person` is expected; the overridden method is selected at runtime.
- **Static members:** `IdGenerator` counters and methods are shared at class level.
- **Enum:** `EnrollmentStatus` prevents arbitrary status strings.
- **Collections:** `ArrayList` stores records in memory.
- **Exception handling:** services throw `LearnTrackException` for invalid operations; the console layer catches and reports it.

## 5. Active versus deleted records

Student and course “remove” operations currently mark records inactive rather than deleting them from the repository. This preserves the history and ID references used by enrollment records. Inactive students and courses are rejected for new enrollments.

## 6. Known limitations

- No persistent storage: data resets when the application closes.
- No authentication, authorization, or multi-user support.
- No automated unit-test suite is included in the starter project; use the README manual test checklist.
- IDs are sequential counters and are not persisted across runs.
- Email validation currently checks that a value is present, not that it conforms to a complete email-address standard.
- The console lists all stored records, including inactive ones, and displays the active flag.

## 7. Potential future improvements

If the assignment scope permits, add automated tests, stronger email validation, filters for active records, persistent storage, and a separate UI class. These are future ideas, not requirements implemented by this version.
