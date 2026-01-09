package xyz.kgy_production.res_mgr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.kgy_production.res_mgr.model.CategoryDto
import xyz.kgy_production.res_mgr.model.ItemDto
import xyz.kgy_production.res_mgr.model.SearchQuery
import xyz.kgy_production.res_mgr.repo.ResourceRepository
import xyz.kgy_production.res_mgr.utils.AppLogger

data class ResourceUiState(
    val categories: List<CategoryDto> = emptyList(),
    val selectedCategory: CategoryDto? = null,
    val items: List<ItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class ResourceViewModel(
    private val repository: ResourceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourceUiState())
    val uiState: StateFlow<ResourceUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadItems()
    }

    fun loadCategories() {
        viewModelScope.launch {
             repository.getCategories()
                 .onSuccess { cats ->
                     _uiState.update { it.copy(categories = cats) }
                 }
                 .onFailure {
                     AppLogger.e("ViewModel", "Failed to load categories")
                 }
        }
    }

    fun selectCategory(category: CategoryDto?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            AppLogger.d("ViewModel", "Loading items query=${_uiState.value.searchQuery}")
            _uiState.update { it.copy(isLoading = true, error = null) }
            val filter = mutableMapOf<String, String>()
            _uiState.value.selectedCategory?.let {
                filter["categoryId"] = it.id
            }

            val query = SearchQuery(query = _uiState.value.searchQuery, filter = filter)
            repository.getItems(query)
                .onSuccess { items ->
                    AppLogger.i("ViewModel", "Loaded ${items.size} items")
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { e ->
                    AppLogger.e("ViewModel", "Failed to load items", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadItems()
    }

    fun importBatch(content: ByteArray) {
        viewModelScope.launch {
            AppLogger.i("ViewModel", "Importing batch size=${content.size}")
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.importBatch(content)
                .onSuccess { msg ->
                    AppLogger.i("ViewModel", "Import success: $msg")
                    // reload after import
                    loadItems()
                }
                .onFailure { e ->
                    AppLogger.e("ViewModel", "Import failed", e)
                    _uiState.update { it.copy(error = "Import failed: ${e.message}", isLoading = false) }
                }
        }
    }

    fun deleteItem(id: String) {
         viewModelScope.launch {
            AppLogger.i("ViewModel", "Deleting item $id")
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.deleteItem(id)
                .onSuccess {
                    AppLogger.i("ViewModel", "Deleted item $id")
                    loadItems()
                }
                .onFailure { e ->
                    AppLogger.e("ViewModel", "Delete failed", e)
                    _uiState.update { it.copy(error = "Delete failed: ${e.message}", isLoading = false) }
                }
        }
    }
}
