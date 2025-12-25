# Personal Diary App - Coding Principles

## Architecture Overview

This app follows a **clean architecture** approach with clear separation of concerns across layers:

```
View (Compose UI) → ViewModel → UseCase → Repository → DAO → Database
```

### Key Architectural Principles

1. **MVVM Pattern**: Model-View-ViewModel architecture
2. **Dependency Injection**: Hilt for all DI needs
3. **Reactive Programming**: Prefer Flows over polling/manual refreshes
4. **Domain-Centric Design**: All layers work with domain models except the repository internals

---

## Layer Responsibilities

### 1. Repository Module (`repository/`)

**Purpose**: Data access layer that abstracts database operations

**Key Rules**:
- Contains **DB entities (storage models)** and **DAOs**
- Contains **Repository implementations**
- **Storage models are PRIVATE** to this module - never exposed outside
- Repositories work with **Domain objects** as input/output
- Repositories convert:
  - Domain → Storage when storing data
  - Storage → Domain when retrieving data
- Domain and Storage models are **1-1 compatible** (one can fully create the other)
- No network layer currently exists, but repositories prepare for future extension
- Keep DAOs and Repositories together in this module

**Example Flow**:
```kotlin
// Repository receives Domain model
fun saveDiary(diary: DiaryEntry) {
    val storageModel = diary.toStorageModel()
    dao.insert(storageModel)
}

// Repository returns Domain model
fun getDiaries(): Flow<List<DiaryEntry>> {
    return dao.getAllDiaries().map { list ->
        list.map { it.toDomainModel() }
    }
}
```

---

### 2. UseCase Module (`usecase/`)

**Purpose**: Business logic layer containing reusable operations

**Key Rules**:
- **One file per feature** containing all related use cases
- Use cases work **exclusively with Domain models**
- A use case may orchestrate **multiple repositories**
- Keep use cases focused and single-purpose
- No direct database or UI dependencies

**Example Structure**:
```kotlin
// DiaryUseCases.kt
class SaveDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry): Result<Unit>
}

class GetDiaryEntriesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<List<DiaryEntry>>
}
```

---

### 3. Presentation Layer (`feature/`)

#### ViewModel Rules

**Input Pattern**:
- ViewModel exposes a **sealed class/interface of Input actions**
- View calls these actions - **no return values**
- ViewModel exposes a **single LiveState/StateFlow** called `viewData`
- All UI state changes are reflected in this single state object

**Example**:
```kotlin
class DiaryViewModel @Inject constructor(
    private val saveDiaryUseCase: SaveDiaryEntryUseCase,
    private val getDiariesUseCase: GetDiaryEntriesUseCase
) : ViewModel() {

    sealed interface Input {
        data class SaveEntry(val content: String) : Input
        data object LoadEntries : Input
        data class DeleteEntry(val id: String) : Input
    }

    data class ViewData(
        val entries: List<DiaryEntry> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _viewData = MutableStateFlow(ViewData())
    val viewData: StateFlow<ViewData> = _viewData.asStateFlow()

    fun onInput(input: Input) {
        when (input) {
            is Input.SaveEntry -> saveEntry(input.content)
            Input.LoadEntries -> loadEntries()
            is Input.DeleteEntry -> deleteEntry(input.id)
        }
    }
}
```

**ViewModel Responsibilities**:
- Contains **business logic for the view**
- Delegates data operations to **UseCases**
- **Never directly calls repositories**
- Manages UI state transformations

#### View (Compose) Rules

- Use **Jetpack Compose** for all UI
- Prefer **stable, production-ready components**
- When stable components unavailable, use those expected to remain stable
- Views **subscribe to ViewModel's viewData** StateFlow
- Views **send actions** to ViewModel via `onInput()`
- Keep composables focused and reusable

**Example**:
```kotlin
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val viewData by viewModel.viewData.collectAsStateWithLifecycle()

    DiaryContent(
        entries = viewData.entries,
        isLoading = viewData.isLoading,
        onSaveEntry = { content ->
            viewModel.onInput(DiaryViewModel.Input.SaveEntry(content))
        }
    )
}
```

---

## Domain Models

**Purpose**: Shared data models used across layers (except repository internals)

**Key Rules**:
- Domain models can **group related properties** in data classes for convenience
- Domain models can **transform data** to make life easier for the app
- Must maintain **fast conversion** to/from storage models
- Can be more developer-friendly than storage models
- **1-1 compatible** with storage models (no data loss in conversion)

**Example**:
```kotlin
// Domain Model (exposed to all layers)
data class DiaryEntry(
    val id: String,
    val metadata: Metadata,
    val content: String
) {
    data class Metadata(
        val createdAt: Instant,
        val modifiedAt: Instant,
        val tags: List<String>
    )
}

// Storage Model (private to repository module)
internal data class DiaryEntryEntity(
    val id: String,
    val content: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val tagsJson: String
)
```

---

## Testing Strategy

### Testing Philosophy

- Use **Test-Driven Development (TDD)** with flexibility
- Tests serve as **verification** and **documentation**
- **Don't write unnecessary tests** just for coverage
- Focus on **meaningful test cases**

### Testing Libraries

- **MockK**: For mocking dependencies
- **Strikt**: For assertions
- **Compose Test**: For UI testing

### Testing Layers

#### 1. Repository Tests
- Test data conversion (Domain ↔ Storage)
- Test DAO interactions
- Mock DAO, verify conversions

#### 2. UseCase Tests
- Test business logic
- Mock repositories
- Verify orchestration of multiple repositories

#### 3. ViewModel Tests
- Test input handling
- Test viewData state transitions
- Mock use cases
- Verify all input actions produce correct states

#### 4. UI Tests (Compose)
- Run as **unit tests** (not instrumented)
- Verify UI rendering based on viewData
- Verify user interactions trigger correct inputs
- Use `createComposeRule()` for testing

### Test-Driven Development Workflow

1. **Define test function names FIRST** (before implementation)
   - This sets clear targets for what needs to be built
   - **Get approval** for test names before coding
   
2. **Write the tests** (they will fail initially)

3. **Implement the code** to make tests pass

4. **Refactor** while keeping tests green

**Example Test Names (to be approved first)**:
```kotlin
class DiaryViewModelTest {
    // Test names defined before implementation
    @Test fun `onInput SaveEntry updates viewData with new entry`()
    @Test fun `onInput SaveEntry shows loading state during save`()
    @Test fun `onInput SaveEntry shows error when save fails`()
    @Test fun `onInput LoadEntries emits all diary entries`()
    @Test fun `onInput DeleteEntry removes entry from viewData`()
}
```

---

## Development Workflow

### Coding Order: **UI-First Approach**

Build layers in this order:

1. **UI Layer** (Compose screens & ViewModels)
   - Design the UI and define what data/actions are needed
   - Define ViewModel Input actions and ViewData structure
   
2. **UseCase Layer**
   - Implement only the use cases needed by ViewModels
   - Avoids creating unused code
   
3. **Repository Layer**
   - Implement only the repository methods needed by UseCases
   - Define storage models and DAOs

**Rationale**: This ensures we don't create unnecessary code in lower layers

### Per-Feature Development Steps

1. **Design Phase**
   - Sketch UI requirements
   - Define ViewModel inputs and viewData
   - List required use cases
   
2. **Test Planning Phase**
   - Write test function names for ViewModel
   - Write test function names for UseCases
   - Write test function names for Repositories
   - **GET APPROVAL** before proceeding
   
3. **Implementation Phase**
   - Start with UI (Compose + ViewModel)
   - Write and pass ViewModel tests
   - Implement UseCases
   - Write and pass UseCase tests
   - Implement Repositories
   - Write and pass Repository tests
   - Write and pass UI tests

---

## Reactive Programming

### Prefer Flows Over Polling

- Use **Kotlin Flows** for reactive data streams
- Avoid manual refresh mechanisms
- Let data updates propagate automatically

**Example**:
```kotlin
// Good: Reactive flow
class GetDiariesUseCase {
    operator fun invoke(): Flow<List<DiaryEntry>> {
        return diaryRepository.observeDiaries()
    }
}

// Avoid: Manual polling
class GetDiariesUseCase {
    suspend fun invoke(): List<DiaryEntry> {
        return diaryRepository.getDiaries() // Requires manual refresh
    }
}
```

---

## Dependency Injection (Hilt)

- Use **Hilt** for all dependency injection
- Annotate repositories, use cases, and view models appropriately
- Define modules in the appropriate layer
- Follow Hilt best practices for scoping

**Example**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindDiaryRepository(
        impl: DiaryRepositoryImpl
    ): DiaryRepository
}
```

---

## Summary Checklist

When implementing a new feature, ensure:

- [ ] UI designed with Compose using stable components
- [ ] ViewModel uses Input pattern and single ViewData state
- [ ] ViewModel only calls UseCases (not repositories)
- [ ] UseCases work with Domain models only
- [ ] Repositories convert between Domain and Storage models
- [ ] Storage models are private to repository module
- [ ] Test names written and approved before implementation
- [ ] All layers have meaningful unit tests
- [ ] UI tests verify user interactions
- [ ] Flows used for reactive data
- [ ] Hilt used for dependency injection
- [ ] Followed UI-first development order

---

## Questions or Clarifications?

When in doubt:
- Check existing code for patterns
- Follow the principle of least surprise
- Ask for approval on architectural decisions
- Remember: Better to ask than to assume

