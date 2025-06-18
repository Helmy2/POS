# Application Architecture & MVI Blueprint

This document outlines the complete architecture of the Offline-First POS/ERP Android application.
It covers both the high-level layered structure and the specific Model-View-Intent (MVI) pattern
used in the presentation layer. This is the definitive blueprint for all development.

## 1. High-Level Architecture: Clean Architecture

The application is built upon the principles of **Clean Architecture**. This separates the code into
distinct layers, each with a specific responsibility. This separation makes the app more robust,
easier to test, and simpler to maintain over time.

The data flows inwards, with dependencies only pointing from outer layers to inner layers. The inner
layers have no knowledge of the outer layers.

```
+------------------------------------------------+
|              Presentation Layer (UI)           |
| Android Framework, Jetpack Compose, ViewModels.|
|           (Depends on Domain Layer)            |
+------------------------------------------------+
                       |
                       V
+------------------------------------------------+
|                    Domain Layer                |  
|       Pure Kotlin, Business Logic, Entities    |
|       (No dependencies on other layers)        |
+------------------------------------------------+
                       |
                       V
+------------------------------------------------+
|                     Data Layer                 | 
|        Room, Retrofit/Ktor, Repositories.      |
|           (Depends on Domain Layer)            |
+------------------------------------------------+
```

### 1.1. Data Layer

- **Responsibility**: Manages all data operations. It is the single source of truth for all
  application data.

- **Components**:

    - **Repositories**: Expose data to the rest of the app (e.g., `UserRepository`). They are
      responsible for coordinating data from different sources.

    - **Data Sources**: The concrete implementations of data retrieval (e.g., Room DAOs for the
      local database, Retrofit/Ktor services for the network API).

- **Key Technologies**: Room, Retrofit/Ktor, Kotlinx Serialization.

### 1.2. Domain Layer

- **Responsibility**: Contains the core business logic and rules of the application. This layer is
  completely independent of any frameworks.

- **Components**:

    - **Entities**: Simple data classes that represent the core objects of the app (e.g., `User`,
      `Product`).

- **Key Technologies**: Pure Kotlin.

### 1.3. Presentation Layer (UI)

- **Responsibility**: Displays the UI to the user and handles user interaction. This layer is built
  using our specific MVI blueprint.

- **Components**: ViewModels, Composables (Screens), and the MVI framework.

- **Key Technologies**: Jetpack Compose, Kotlin Coroutines & Flow, Koin for Dependency Injection.

## 2. MVI Architecture Blueprint

The presentation layer is built exclusively using a strict Model-View-Intent (MVI) pattern.

### 2.1. Core Principles

The architecture is built on the principle of **Unidirectional Data Flow (UDF)**. Data flows in a
single, predictable loop, which makes the app's state easier to reason about and debug.

The flow is as follows:

1. The **View** (Composable) sends an **Event** (e.g., a button click).

2. The **ViewModel** receives the Event and processes it, executing any necessary business logic (
   e.g., calling a Repository).

3. The ViewModel then sends a new Event to a **Reducer**.

4. The **Reducer**, a pure function, takes the current **State** and the Event to create a new *
   *State**.

5. The new State is emitted back to the View, which re-renders to reflect the changes.

### 2.2. Architectural Components

Each feature is composed of several key components that work together.

- **`BaseViewModel<State, Event, Effect>`**: An abstract ViewModel that manages the state flow,
  event handling, and the connection to the Reducer.

- **`Reducer<State, Event, Effect>`**: An interface for a pure function that is the _only_ component
  allowed to change the state.

- **Feature `Contract`**: A simple `object` that defines a feature's unique `State` and `Event`
  sealed interfaces. This provides a clear and discoverable API for the feature.

- **Feature `Reducer`**: A class that implements the `Reducer` interface. Its sole responsibility is
  to take the current `State` and an `Event` and return a new `State`. **It must be a pure function
  with no side effects.**

- **Feature `ViewModel`**: The orchestrator for the feature. It receives Events from the UI,
  executes business logic, triggers side effects through global controllers, and sends events to the
  Reducer to trigger state changes.

- **Feature `Screen`**: Contains the Composable UI. It is split into two parts:

    - **`...Route`**: A stateful Composable that connects to the `ViewModel` and collects the state.

    - **`...Screen`**: A completely stateless Composable that only receives state and emits events.

### 2.3. Global Side-Effect Controllers

To keep ViewModels decoupled from the Android framework, we use centralized singleton controllers
for handling global side effects. These controllers are **injected via Koin** into the ViewModels
that need them.

- **`NavigationController`**: Manages all navigation events. The ViewModel sends a command like
  `navigate(Destination.Dashboard)`, and a central observer in the `AppNavHost` executes the
  navigation. This means the ViewModel doesn't need a `NavController` instance.

- **`SnackbarController`**: Manages all UI notifications. The ViewModel sends a `SnackbarEvent`, and
  the `MainScaffold` listens for these events and shows the snackbar. This decouples the ViewModel
  from the `SnackbarHostState` and `Context`.

## 3. Key Architectural Decisions (The "Why")

Every part of this architecture was chosen deliberately to enhance robustness and maintainability.

| Decision                                          | Why It's the Right Choice                                                                                                                                                                                                                                                       |
|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Using a `Reducer`**                             | Enforces that all state mutations are pure, predictable, and centralized. This eliminates a major source of bugs and makes the state logic extremely easy to unit test without mocking any dependencies.                                                                        |
| **Global `Navigation` and `Snackbar`Controllers** | This is crucial for **decoupling**. The ViewModel's job is to decide _when_ to navigate or show a message, not _how_. By using controllers, the ViewModel remains a pure Kotlin class, making it easier to test and potentially reuse in a KMP environment.                     |
| **Injecting Controllers into the `ViewModel`**    | Instead of using singleton `object`s directly, we inject the controllers. This follows the principle of Dependency Inversion, making the ViewModel's dependencies explicit and allowing us to easily provide fake or mock controllers during testing.                           |
| **`StringResource`Wrapper**                       | By wrapping all user-facing text in a `StringResource` class, the ViewModel remains completely ignorant of the Android `Context`. This is essential for unit testing and makes the app **future-proof for Compose Multiplatform**, where resource loading is platform-specific. |

## 4. Example in Action: The Login Feature

Here is a simplified look at how the components work together for the login flow.

#### `LoginContract.kt`

Defines the `State` (what the UI can show) and the `Event`s (what the user can do).

```
object LoginContract {
    data class State(...) : Reducer.ViewState
    sealed interface Event : Reducer.ViewEvent {
        data object LoginClicked : Event
        data class LoginSuccess(val username: String) : Event
        // ...
    }
}
```

#### `LoginReducer.kt`

A pure function that calculates the next state.

```
class LoginReducer : Reducer<State, Event, Nothing> {
    override fun reduce(state: State, event: Event): Pair<State, Nothing?> {
        return when (event) {
            is Event.LoginClicked -> state.copy(loading = true) to null
            is Event.LoginSuccess -> state.copy(loading = false) to null
            // ...
        }
    }
}
```

#### `LoginViewModel.kt`

The orchestrator. It executes the login logic and triggers side effects.

```
class LoginViewModel(...) : BaseViewModel<State, Event, Nothing>(...) {
    // ...
    private fun loginUser() {
        // 1. Send an event to the reducer to update state
        setState(Event.LoginClicked)

        viewModelScope.launch {
            userRepository.login(...).fold(
                onSuccess = {
                    // 2. Send another event to the reducer
                    setState(Event.LoginSuccess(it.name))
                    // 3. Trigger a navigation side effect
                    navigationController.navigate(Destination.Dashboard, ...)
                },
                onFailure = {
                    // 4. Trigger a snackbar side effect
                    snackbarController.sendEvent(...)
                    // 5. Send an event to update state
                    setState(Event.LoginFailure)
                }
            )
        }
    }
}
```

#### `LoginScreen.kt`

The UI. It is stateless and only communicates via state and events.

```
@Composable
fun LoginRoute(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // The Route connects the ViewModel to the stateless Screen
    LoginScreen(
        state = state,
        processEvent = viewModel::processEvent
    )
}

@Composable
fun LoginScreen(state: State, processEvent: (Event) -> Unit) {
    // ...
    Button(onClick = { processEvent(Event.LoginClicked) }) {
        // ...
    }
}
```