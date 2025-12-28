# Copilot Instructions for Financisto Modern Architecture

## Project Overview

This repository contains two Android applications:

- **`:legacy-app`** - Ancient monolithic expense and budget manager codebase. Fully functional but uses outdated architecture. This module should NOT be modified unless explicitly requested.
- **`:modern-app`** - Work-in-progress rewrite using modern Android architecture. This is the primary development target. Currently not fully functional and may have bugs.

**Important**: All new development and features should be implemented in the `:modern-app` module and its supporting modules unless otherwise specified.

## Architecture

The application follows **Clean Architecture** with these layers and data flow:

```
Compose UI → ViewModel → UseCase → Repository → Room DAO → SQLite
```

### Module Structure

- **`repository/`** - Data access layer (DAOs, storage models, repository implementations)
- **`usecase/`** - Business logic layer (use cases working with domain models)
- **`feature/`** - Presentation layer (ViewModels, Compose UI, navigation)
- **`core/common/`** - Shared utilities and common code
- **`core/ui/`** - Shared UI components and theming
- **`modern-app/`** - Main application module for modern architecture

## Important points
- When asked to implement a feature that requires data layer support, check if existing usecases are sufficient. If not, add usecases for it. These new usecases should rely on the existing repository support. If no relevant repository exists, check the Room DB and if some DAO is there, create corresponding repository. Finally, if there is no ROOM DAO either, check if the legacy app had this feature and try to mimic its SQLite data structures.

## Coding Guidelines

### Architecture Principles

1. **Layer Responsibilities**:
   - **Repository**: Handles data access, converts between domain and storage models. Storage models are PRIVATE to this module.
   - **UseCase**: Contains business logic, works exclusively with domain models, may orchestrate multiple repositories.
   - **ViewModel**: Manages UI state, delegates to use cases, NEVER calls repositories directly.
   - **View (Compose)**: Subscribes to ViewModel state, sends actions to ViewModel.

2. **Domain Models**: 
   - Shared across all layers except repository internals
   - Must be 1-1 compatible with storage models
   - Can group properties and transform data for convenience

3. **ViewModel Pattern**:
   - Expose a sealed interface/class for Input actions
   - Expose a single `viewData` StateFlow for all UI state
   - Views call `onInput()` with actions (no return values)
   - All state changes reflected in the single state object

4. **Reactive Programming**:
   - Prefer Kotlin Flows over polling/manual refreshes
   - Use reactive data streams throughout

5. **Dependency Injection**:
   - Use Hilt for all dependency injection
   - Follow proper scoping and module definitions

### Development Workflow

1. **UI-First Approach** - Build layers in this order:
   - UI Layer (Compose screens & ViewModels)
   - UseCase Layer (only what ViewModels need)
   - Repository Layer (only what UseCases need)

2. **Per-Feature Steps**:
   - Design UI requirements and ViewModel structure
   - Plan tests (write test function names first)
   - Implement UI and ViewModel with tests
   - Implement UseCases with tests
   - Implement Repositories with tests

3. **Test-Driven Development**:
   - Define test function names FIRST
   - Write tests (they will fail initially)
   - Implement code to make tests pass
   - Refactor while keeping tests green

### Testing Requirements

- Use **MockK** for mocking, **Strikt** for assertions, **Compose Test** for UI
- Write meaningful tests - don't test just for coverage
- Test layers:
  - Repository: Data conversion and DAO interactions
  - UseCase: Business logic and repository orchestration
  - ViewModel: Input handling and state transitions
  - UI: Rendering and user interaction verification


## Verification Steps

### During Development
- After each significant change, verify compilation with:
  ```
  ./gradlew :modern-app:assembleDebug
  ```

### After Feature Completion
- Run all unit tests for the corresponding feature module:
  ```
  ./gradlew :feature:[feature-name]:test
  ```
- Run repository tests if applicable:
  ```
  ./gradlew :repository:test
  ```
- Run usecase tests if applicable:
  ```
  ./gradlew :usecase:test
  ```

## Code Style and Preferences

- Use **Jetpack Compose** for all UI (stable, production-ready components preferred)
- Follow **Kotlin** best practices and idioms
- Use **1-1 compatible** domain and storage models
- Keep files focused: one file per feature for use cases
- Maintain clear separation of concerns across layers
- ViewModel never calls repositories directly - always through use cases

## Important Reminders

- **Do NOT modify** `:legacy-app` unless explicitly requested
- **Always verify** compilation with `:modern-app:assembleDebug` after changes
- **Storage models are private** to repository module - never expose them
- **ViewModels delegate** to UseCases, never to Repositories directly
- **Write tests** as you develop, not as an afterthought
- **Use reactive flows** instead of manual polling/refresh patterns

## When in Doubt

- Check existing code in `:modern-app` and feature modules for patterns
- Refer to `docs/CODING_PRINCIPLES.md` for detailed guidance
- Follow the principle of least surprise
- Ask for clarification on architectural decisions rather than assuming
