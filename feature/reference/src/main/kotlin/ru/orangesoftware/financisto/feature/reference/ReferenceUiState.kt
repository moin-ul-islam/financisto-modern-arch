package ru.orangesoftware.financisto.feature.reference

/**
 * UI state for the Create Category screen.
 */
data class CreateCategoryUiState(
    val categoryName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI state for the Create Payee screen.
 */
data class CreatePayeeUiState(
    val payeeName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI state for the Create Project screen.
 */
data class CreateProjectUiState(
    val projectName: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Actions that can be performed on the Create Category screen.
 */
sealed class CreateCategoryAction {
    data class UpdateName(val name: String) : CreateCategoryAction()
    object CreateCategory : CreateCategoryAction()
    object DismissError : CreateCategoryAction()
}

/**
 * Actions that can be performed on the Create Payee screen.
 */
sealed class CreatePayeeAction {
    data class UpdateName(val name: String) : CreatePayeeAction()
    object CreatePayee : CreatePayeeAction()
    object DismissError : CreatePayeeAction()
}

/**
 * Actions that can be performed on the Create Project screen.
 */
sealed class CreateProjectAction {
    data class UpdateName(val name: String) : CreateProjectAction()
    object CreateProject : CreateProjectAction()
    object DismissError : CreateProjectAction()
}