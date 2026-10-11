# JVM Basics Used by LearnTrack

This document connects a few JVM and Java runtime concepts to the LearnTrack application.

## JDK, JRE, and JVM

- **JVM (Java Virtual Machine):** executes Java bytecode.
- **JRE (Java Runtime Environment):** the runtime components needed to run Java applications; modern Java distributions commonly package these within a JDK.
- **JDK (Java Development Kit):** includes tools such as `javac` for compiling Java source code and tools for running/debugging applications.

## What happens when LearnTrack runs?

1. `javac` compiles `.java` source files into `.class` bytecode files.
2. The `java` launcher starts the JVM.
3. The JVM loads `com.airtribe.learntrack.Main`.
4. The JVM calls `Main.main(String[] args)`.
5. `Main` creates repository and service objects, then reads console input until the user exits.
6. When the process ends, the in-memory objects are no longer available. LearnTrack does not write its data to persistent storage.

## Source code, bytecode, and execution

```text
Main.java + other .java files
             |
             | javac
             v
      .class bytecode files
             |
             | java launcher
             v
             JVM
             |
             v
       Program executes
```

## Objects and memory

- `new Student(...)`, `new Course(...)`, and `new Enrollment(...)` create objects.
- Local variables such as `studentService` hold references to objects.
- Repositories keep references to entities in `ArrayList` collections.
- `IdGenerator` uses `static` counters, so each counter belongs to the class rather than an individual `IdGenerator` object.
- The JVM manages object memory and garbage collection. The application should not attempt to manually free Java objects.

## Classpath and packages

The fully qualified name of the entry point is `com.airtribe.learntrack.Main`. The classpath tells the Java launcher where compiled classes are located. In the command-line setup, `-cp out` points to the output directory containing the package folders.

## Exceptions

`LearnTrackException` extends `RuntimeException`, so service methods can report invalid operations without declaring it in a `throws` clause. `Main` catches this expected application exception and prints its message. `NumberFormatException` is handled when parsing integer input.

## Further learning

Useful next topics are class loading, stack versus heap, garbage collection, classpath, checked versus unchecked exceptions, and JVM monitoring tools. These topics are explanatory notes; this project does not implement custom JVM monitoring.
