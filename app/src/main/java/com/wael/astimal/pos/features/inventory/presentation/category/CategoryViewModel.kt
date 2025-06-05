package com.wael.astimal.pos.features.inventory.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryScreenState())
    val state: StateFlow<CategoryScreenState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
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
            _state.update { it.copy(loading = true, error = null) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            categoryRepository.getCategories(query)
                .catch { e ->
                    _state.update { it.copy(loading = false, error = "Error fetching categories: ${e.message}") }
                }
                .collect { categories ->
                    _state.update { it.copy(loading = false, searchResults = categories) }
                }
        }
    }

    private fun handleSelectCategory(category: Category?) {
        if (category == null) {
            _state.update {
                it.copy(
                    selectedCategory = null,
                    inputArName = "",
                    inputEnName = ""
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
        val currentState = _state.value
        if (currentState.inputArName.isBlank() && currentState.inputEnName.isBlank()) {
            _state.update { it.copy(error = "At least one name (Arabic or English) is required.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val result = if (currentState.isNew || currentState.selectedCategory == null) {
                categoryRepository.addCategory(
                    arName = currentState.inputArName,
                    enName = currentState.inputEnName
                )
            } else {
                categoryRepository.updateCategory(
                    category = currentState.selectedCategory,
                    newArName = currentState.inputArName,
                    newEnName = currentState.inputEnName
                )
            }

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedCategory = null,
                            inputArName = "",
                            inputEnName = ""
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to save category: ${e.message}") }
                }
            )
        }
    }

    private fun deleteSelectedCategory() {
        val categoryToDelete = _state.value.selectedCategory
        if (categoryToDelete == null) {
            _state.update { it.copy(error = "No category selected for deletion.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = categoryRepository.deleteCategory(categoryToDelete)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedCategory = null,
                            inputArName = "",
                            inputEnName = ""
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to delete category: ${e.message}") }
                }
            )
        }
    }
}