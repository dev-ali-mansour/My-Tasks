# My Tasks

A modern Android task management app built with Kotlin, Jetpack Compose, and a clean architecture approach. It demonstrates best practices around state-driven UIs, Kotlin coroutines and flows, Room persistence, and dependency injection with Koin.

## Features

- Create, update, and delete tasks
- Mark tasks as completed
- View task details and edit existing tasks
- Material 3 UI built with Jetpack Compose
- Reactive state management using `StateFlow`
- Local persistence via Room database
- Dependency injection with Koin
- Unit tests for ViewModels, use cases, and repository layer

## Tech Stack

**Language & Runtime**
- Kotlin
- Coroutines & Flows

**Android & Jetpack**
- Jetpack Compose UI
- Navigation-Compose
- ViewModel / Lifecycle
- Room for local data storage

**Architecture**
- Clean-ish layered architecture:
  - `core/domain`: use cases and domain models
  - `core/data`: repositories and Room entities/DAOs
  - `feature/*`: UI + ViewModels per feature (home, task details, new task, update task)
- Unidirectional data flow: `Event -> ViewModel -> State/Effect -> UI`

**Dependency Injection & Tools**
- Koin (with KSP for DI code generation)
- JUnit 5
- MockK
- JaCoCo for test coverage

## Project Structure

At a high level:

```text
My-Tasks/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/dev/alimansour/mytasks/
│   │   │   │   ├── core/
│   │   │   │   │   ├── data/...
│   │   │   │   │   ├── domain/...
│   │   │   │   │   └── ui/...
│   │   │   │   └── feature/
│   │   │   │       ├── home/...
│   │   │   │       ├── task/add/...
│   │   │   │       ├── task/details/...
│   │   │   │       └── task/update/...
│   │   │   └── res/...
│   │   └── test/java/dev/alimansour/mytasks/...
│   └── build.gradle.kts
├── build.gradle.kts
└── gradle/libs.versions.toml
```

## Requirements

- **Java JDK 21** (recommended)
- Android Studio Giraffe or newer
- Gradle wrapper (provided in the repo)

> Note: Newer Java versions (e.g., 25) may not be fully supported by the Kotlin/Gradle toolchain used here. If you see errors like `java.lang.IllegalArgumentException: 25.0.1`, run Gradle with JDK 21.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/dev-ali-mansour/My-Tasks.git
cd My-Tasks
```

### 2. Configure Java

On Linux, if you have JDK 21 installed at `/usr/lib/jvm/java-21-openjdk`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

You should see a Java 21 version.

### 3. Open in Android Studio

1. `File` → `Open...`
2. Select the `My-Tasks` folder.
3. Let Gradle sync and KSP/Room code generation complete.

### 4. Build and run

From Android Studio:

- Select a device or emulator.
- Click **Run** on the `app` configuration.

From the command line:

```bash
./gradlew :app:assembleDebug
```

The resulting APK will be in `app/build/outputs/apk/debug/`.

## Running Tests

Unit tests (JVM):

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew :app:testDebugUnitTest
./gradlew :app:testReleaseUnitTest
```

This runs unit tests for core use cases, repository implementations, and feature ViewModels like:

- `HomeViewModelTest`
- `NewTaskViewModelTest`
- `UpdateTaskViewModelTest`
- `TaskDetailsViewModelTest`

### Test coverage (JaCoCo)

To generate a global JaCoCo coverage report:

```bash
./gradlew jacocoTestReport
```

Open the HTML report:

- `app/build/reports/jacoco/jacocoTestReport/html/index.html`

For per-package and per-class coverage, see the HTML tree under:

- `app/build/reports/jacoco/html/...`

## Code Style & Conventions

- Kotlin official code style
- GIVEN / WHEN / THEN comments in tests to clarify test stages
- Use of sealed classes for events and effects where appropriate
- Nullability handled explicitly via Kotlin types and `UiText` wrappers

## Dependency Management

The project uses Gradle version catalogs (`gradle/libs.versions.toml`) to manage versions of:

- Android Gradle Plugin (AGP)
- Kotlin and coroutines
- Compose BOM and related artifacts
- Koin, Room, MockK, JUnit, etc.

To update a dependency, adjust its version in `gradle/libs.versions.toml` and sync.

## Notes on DI and KSP

Koin modules and ViewModels are wired using the Koin KSP compiler. Generated artifacts live under:

- `app/build/generated/ksp/...`

If you see Koin warnings like `no module found`, ensure:

- KSP is enabled in `build.gradle.kts` for the relevant source sets.
- Koin modules are correctly annotated / registered.

A clean rebuild can often resolve stale generated code:

```bash
./gradlew clean :app:assembleDebug
```

## License

This project is provided as-is for learning and demonstration purposes. Adapt licensing as needed for your use case.

