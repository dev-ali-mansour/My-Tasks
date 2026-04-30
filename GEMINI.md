# Gemini CLI - MyTasks Project Instructions

## Project Overview
MyTasks is a modern Android task management application built with Kotlin and Jetpack Compose. It follows Clean Architecture principles and employs the MVI (Model-View-Intent) pattern for reactive state management.

### Tech Stack
- **Language:** Kotlin (Coroutines, Flow)
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture with MVI
- **Dependency Injection:** Koin (with KSP for code generation)
- **Persistence:** Room Database
- **Navigation:** Navigation-Compose
- **Testing:** JUnit 5, MockK, Coroutines-Test
- **Static Analysis:** Detekt
- **Build System:** Gradle (KTS) with Version Catalogs

## Project Structure
The project is organized into a main `app` module with the following package structure:

- `dev.alimansour.mytasks.core`: Shared core components
    - `data`: Repositories, Room entities, DAOs, and mappers.
    - `domain`: Domain models, use cases, and repository interfaces.
    - `ui`: Shared UI components, theme, navigation routes, and utilities.
- `dev.alimansour.mytasks.feature`: Feature-specific modules
    - `home`: Main task list screen.
    - `task`: Sub-packages for `add`, `details`, and `update` task screens.
- `dev.alimansour.mytasks.di`: Koin dependency injection modules.

## Architecture & Patterns
### Clean Architecture
- **Domain Layer:** Contains business logic (Use Cases) and models. It is independent of other layers.
- **Data Layer:** Implements repository interfaces and handles data sources (Room).
- **UI Layer:** Features are encapsulated in the `feature` package, containing ViewModels and Compose screens.

### MVI (Model-View-Intent)
Each feature screen follows this pattern:
- **State:** A stable data class representing the UI state.
- **Event:** A sealed interface for user actions.
- **Effect:** A sealed interface for one-time side effects (e.g., navigation, showing a snackbar).
- **ViewModel:** Processes events and updates the state flow.

## Building and Running
### Build Commands
- **Assemble Debug APK:** `./gradlew :app:assembleDebug`
- **Run Detekt:** `./gradlew detekt`
- **Clean Project:** `./gradlew clean`

### Testing Commands
- **Run Unit Tests:** `./gradlew :app:testDebugUnitTest`
- **Generate Coverage Report (JaCoCo):** `./gradlew jacocoTestReport`
  - Report location: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

## Development Conventions
### Coding Style
- Follow official Kotlin coding style.
- Use `UiText` for handling string resources in ViewModels to keep them platform-independent.
- Use `Result` wrapper for domain/data layer operations.

### Testing Practices
- Use **GIVEN / WHEN / THEN** comments to structure test cases.
- Mock dependencies using **MockK**.
- Test ViewModels, Use Cases, and Repository implementations.
- Ensure state transitions and effects are correctly verified in MVI tests.

### Dependency Injection
- Koin modules are annotated with `@Module` and `@Configuration`.
- ViewModels are annotated with `@KoinViewModel`.
- Ensure KSP code generation is completed after adding new modules or ViewModels.

## Key Files
- `gradle/libs.versions.toml`: Version catalog for all dependencies.
- `app/build.gradle.kts`: Main application build configuration.
- `app/src/main/java/dev/alimansour/mytasks/MainActivity.kt`: Main entry point.
- `app/src/main/java/dev/alimansour/mytasks/core/ui/theme/Theme.kt`: Compose theme definition.
- `app/src/main/java/dev/alimansour/mytasks/core/ui/navigation/AppNavHost.kt`: Navigation graph.
