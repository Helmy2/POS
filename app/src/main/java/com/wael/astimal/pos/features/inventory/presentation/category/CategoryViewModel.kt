package com.wael.astimal.pos.features.inventory.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository, private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryScreenState())
    val state: StateFlow<CategoryScreenState> = _state.asStateFlow()

    private var searchJob: Job? = null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(currentUser = userRepository.getCurrentUser()) }
        }
        onEvent(CategoryScreenEvent.Search(""))
    }

    fun onEvent(event: CategoryScreenEvent) {
        when (event) {
            is CategoryScreenEvent.CreateCategory -> saveCategory()
            is CategoryScreenEvent.UpdateCategory -> saveCategory()
            is CategoryScreenEvent.DeleteCategory -> deleteSelectedCategory()
            is CategoryScreenEvent.Search -> searchCategories(event.query)
            is CategoryScreenEvent.SelectCategory -> handleSelectCategory(event.category)
            is CategoryScreenEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchCategories(event.query)
            }

            is CategoryScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            is CategoryScreenEvent.UpdateInputArName -> _state.update { it.copy(inputArName = event.name) }
            is CategoryScreenEvent.UpdateInputEnName -> _state.update { it.copy(inputEnName = event.name) }
        }
    }

    private fun searchCategories(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            categoryRepository.getCategories(query).catch { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_fetching_categories))
            }.collect { categories ->
                _state.update { it.copy(loading = false, searchResults = categories) }
            }
        }
    }

    private fun handleSelectCategory(category: Category?) {
        if (category == null) {
            _state.update {
                it.copy(
                    selectedCategory = null, inputArName = "", inputEnName = ""
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedCategory = category,
                    inputArName = category.localizedName.arName ?: "",
                    inputEnName = category.localizedName.enName ?: "",
                )
            }
        }
    }

    private fun saveCategory() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.inputArName.isBlank() && currentState.inputEnName.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_some_field_are_required))
                return@launch
            }

            _state.update { it.copy(loading = true) }

            val result = categoryRepository.saveCategory(
                CategoryEntity(
                    localId = currentState.selectedCategory?.id?.local ?: 0,
                    serverId = currentState.selectedCategory?.id?.server,
                    arName = currentState.inputArName,
                    enName = currentState.inputEnName,
                    createdAt = currentState.selectedCategory?.createdAt
                        ?: Clock.now(),
                )
            )


            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false, selectedCategory = null, inputArName = "", inputEnName = ""
                    )
                }
            }, onFailure = { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.failed_to_save_category))
            })
        }
    }

    private fun deleteSelectedCategory() {
        viewModelScope.launch {
            val categoryToDelete = _state.value.selectedCategory
            if (categoryToDelete == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.no_category_selected_for_deletion))
                return@launch
            }

            _state.update { it.copy(loading = true) }
            val result = categoryRepository.deleteCategory(categoryToDelete)
            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false, selectedCategory = null, inputArName = "", inputEnName = ""
                    )
                }
            }, onFailure = { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.failed_to_delete_category))
            })
        }
    }
}