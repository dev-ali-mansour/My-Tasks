# My Tasks - Project Instructions

## Project Overview
A modern Android task management app built with **Kotlin** and **Jetpack Compose**. The project serves as a showcase for best practices in Android development, including unidirectional data flow, reactive UIs, and local data persistence.

### Tech Stack
- **UI:** Jetpack Compose (Material 3)
- **Navigation:** Jetpack Navigation 3 (State-driven, NavDisplay)
- **Dependency Injection:** Koin (with KSP compiler)
- **Database:** Room
- **Concurrency:** Kotlin Coroutines & Flows
- **Testing:** JUnit 5, MockK, JaCoCo

### Architecture
The project follows a **Clean Architecture** approach with an **MVI (Model-View-Intent)** presentation layer:
- `core/domain`: Use cases and domain models.
- `core/data`: Repositories, Room entities, and DAOs.
- `core/ui`: Shared UI components, theme, and utility functions (e.g., `UiText`).
- `feature/*`: Feature-specific UI (Screens) and ViewModels.
- **Data Flow:** `UI Event -> ViewModel -> UI State / Side Effect -> UI Update`.

---

## Building and Running
### Build Commands
- **Assemble Debug APK:** `./gradlew :app:assembleDebug`
- **Clean Build:** `./gradlew clean :app:assembleDebug`

### Testing Commands
- **Run Unit Tests:** `./gradlew :app:testDebugUnitTest`
- **Generate Coverage Report (JaCoCo):** `./gradlew jacocoTestReport`
- **Report Location:** `app/build/reports/jacoco/jacocoTestReport/html/index.html`

---

## Development Conventions
### Dependency Injection (Koin)
- Use **Koin Annotations** for DI declaration (`@Single`, `@Factory`, `@KoinViewModel`).
- When passing runtime parameters to ViewModels, use `@InjectedParam` in the constructor and `parametersOf` in the `koinViewModel` call within the NavGraph.
- ViewModels should generally be instantiated in `AppNavHost.kt` and passed to screens.

### UI & Strings
- Use **`UiText`** for handling string resources in ViewModels to keep them decoupled from the Android `Context`.
- Prefer sealed classes for representing **UI States**, **Events**, and **Side Effects**.

### Code Style
- Adhere to the official **Kotlin** code style (4-space indentation).
- Maximum line length is **160 characters**.
- Standard import ordering is disabled.

### Testing Practices
- Use **GIVEN / WHEN / THEN** comments to structure test cases.
- Mock external dependencies using **MockK**.
- Use **StandardTestDispatcher** for coroutine testing in ViewModels.
