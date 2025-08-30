package com.wael.astimal.pos.features.user.presentation.employee

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.user.domain.entity.PermissionDetails
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.categories
import pos.app.generated.resources.products
import pos.app.generated.resources.stock_management
import pos.app.generated.resources.stock_transfer
import pos.app.generated.resources.stores
import pos.app.generated.resources.units

class EmployeeReducer :
    Reducer<EmployeeReducer.State, EmployeeReducer.Event, Nothing> {

    data class Item(val key: String, val label: StringResource)

    companion object {
        val screens = listOf(
            Item(Destination.Stores.toString(), Res.string.stores),
            Item(Destination.Units.toString(), Res.string.units),
            Item(Destination.Categories.toString(), Res.string.categories),
            Item(Destination.Products.toString(), Res.string.products),
            Item(Destination.StockManagement.toString(), Res.string.stock_management),
            Item(Destination.StockTransfer().toString(), Res.string.stock_transfer),
        )
    }


    data class State(
        val currentUser: User? = null,
        val selectedEmployee: User? = null,

        // Form Input State
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val arName: String = "",
        val enName: String = "",
        val canHandlePrivatePartner: Boolean = false,

        // UI State
        val permissions: Map<String, PermissionDetails> = emptyMap(),
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,

        // Search State
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val allEmployees: List<User> = emptyList()

    ) : Reducer.ViewState {
        val filteredEmployees
            get() = allEmployees.filter {
                it.localizedName.contains(searchQuery) ||
                        it.localizedName.contains(searchQuery) ||
                        it.email.orEmpty().contains(searchQuery, ignoreCase = true)
            }
        val isNewEmployee: Boolean get() = selectedEmployee == null
        val canSave: Boolean
            get() = if (isNewEmployee) {
                email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        enName.isNotBlank() &&
                        password == confirmPassword &&
                        password.length >= 6
            } else {
                email.isNotBlank() && enName.isNotBlank() && (password.isEmpty() || (password.length >= 6 && password == confirmPassword))
            } && canEdit
        val canEdit: Boolean get() = currentUser?.isAdmin == true
    }

    sealed interface Event : Reducer.ViewEvent {
        // Form Events
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data class ConfirmPasswordChanged(val value: String) : Event
        data class ArNameChanged(val value: String) : Event
        data class EnNameChanged(val value: String) : Event
        data class CanHandlePrivatePartnerChanged(val value: Boolean) : Event
        data object TogglePasswordVisibility : Event
        data object ToggleConfirmPasswordVisibility : Event

        // Data Loading Events
        data class CurrentUserLoaded(val user: User?) : Event
        data class AllEmployeesLoaded(val employees: List<User>) : Event

        // User Action Events
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object NewEmployeeClicked : Event

        // Search Events
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class EmployeeSelected(val employee: User) : Event

        data class PermissionsChanged(val resourceKey: String, val details: PermissionDetails) :
            Event

        // Async Operation Events
        data object SaveSucceeded : Event
        data object SaveFailed : Event
        data object DeleteSucceeded : Event
        data object DeleteFailed : Event
    }

    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            // Form Events
            is Event.EmailChanged -> previousState.copy(email = event.value) to null
            is Event.PasswordChanged -> previousState.copy(password = event.value) to null
            is Event.ConfirmPasswordChanged -> previousState.copy(confirmPassword = event.value) to null
            is Event.ArNameChanged -> previousState.copy(arName = event.value) to null
            is Event.EnNameChanged -> previousState.copy(enName = event.value) to null
            is Event.CanHandlePrivatePartnerChanged -> previousState.copy(
                canHandlePrivatePartner = event.value
            ) to null

            is Event.TogglePasswordVisibility -> previousState.copy(
                isPasswordVisible = !previousState.isPasswordVisible
            ) to null

            is Event.ToggleConfirmPasswordVisibility -> previousState.copy(
                isConfirmPasswordVisible = !previousState.isConfirmPasswordVisible
            ) to null


            // Data Loading
            is Event.CurrentUserLoaded -> previousState.copy(currentUser = event.user) to null
            is Event.AllEmployeesLoaded -> previousState.copy(allEmployees = event.employees) to null

            // Search
            is Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null
            is Event.SearchActiveChanged -> previousState.copy(isSearchActive = event.isActive) to null
            is Event.EmployeeSelected -> previousState.copy(
                selectedEmployee = event.employee,
                email = event.employee.email ?: "",
                arName = event.employee.localizedName.arName ?: "",
                enName = event.employee.localizedName.enName ?: "",
                password = "",
                confirmPassword = "",
                isSearchActive = false,
                canHandlePrivatePartner = event.employee.canHandlePrivatePartner,
                permissions = event.employee.permissions ?: emptyMap()
            ) to null

            // User Actions
            is Event.NewEmployeeClicked, is Event.SaveSucceeded, Event.DeleteSucceeded -> previousState.copy(
                selectedEmployee = null,
                email = "",
                password = "",
                confirmPassword = "",
                arName = "",
                enName = "",
                isLoading = false,
                permissions = emptyMap()
            ) to null

            is Event.PermissionsChanged -> {
                val newPermissions = previousState.permissions.toMutableMap()
                newPermissions[event.resourceKey] = event.details
                previousState.copy(permissions = newPermissions) to null
            }


            // Async Operations
            is Event.SaveClicked -> previousState.copy(isLoading = true) to null
            is Event.SaveFailed -> previousState.copy(isLoading = false) to null
            is Event.DeleteClicked -> previousState.copy(isLoading = true) to null
            is Event.DeleteFailed -> previousState.copy(isLoading = false) to null
        }
    }
}
