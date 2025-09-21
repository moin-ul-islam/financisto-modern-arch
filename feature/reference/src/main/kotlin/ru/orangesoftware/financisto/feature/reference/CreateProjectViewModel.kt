package ru.orangesoftware.financisto.feature.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.usecase.modern.CreateProjectUseCase
import javax.inject.Inject

/**
 * ViewModel for the Create Project screen.
 */
@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    // Navigation callback
    var onProjectCreated: ((Long) -> Unit)? = null

    /**
     * Handles user actions.
     */
    fun handleAction(action: CreateProjectAction) {
        when (action) {
            is CreateProjectAction.UpdateName -> updateName(action.name)
            is CreateProjectAction.CreateProject -> createProject()
            is CreateProjectAction.DismissError -> dismissError()
        }
    }

    private fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            projectName = name,
            errorMessage = null
        )
    }

    private fun createProject() {
        val name = _uiState.value.projectName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Project name cannot be empty"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = createProjectUseCase.execute(name)
            result.onSuccess { projectId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                onProjectCreated?.invoke(projectId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to create project"
                )
            }
        }
    }

    private fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}