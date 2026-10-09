package com.example.tibiagear.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tibiagear.appContainer
import com.example.tibiagear.data.repository.ItemRepository
import com.example.tibiagear.data.repository.userMessage
import com.example.tibiagear.domain.CatalogItem
import com.example.tibiagear.domain.ItemCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ExploreContent {
    data object Loading : ExploreContent
    data object Empty : ExploreContent
    data class Error(val message: String) : ExploreContent
    data class Content(val items: List<CatalogItem>) : ExploreContent
}

data class ExploreUiState(val content: ExploreContent)

private sealed interface CatalogSync {
    data object Running : CatalogSync
    data object Done : CatalogSync
    data class Failed(val message: String) : CatalogSync
}

private data class Params(
    val category: ItemCategory?,
    val sync: CatalogSync,
)

class ExploreViewModel(private val repo: ItemRepository) : ViewModel() {

    private val _category = MutableStateFlow<ItemCategory?>(null) // null = aba "Todos"
    private val sync = MutableStateFlow<CatalogSync>(CatalogSync.Running)

    val category: StateFlow<ItemCategory?> = _category.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExploreUiState> =
        combine(_category, sync) { c, s -> Params(c, s) }
            .flatMapLatest { p ->
                when (val s = p.sync) {
                    CatalogSync.Running -> flowOf(ExploreUiState(ExploreContent.Loading))
                    is CatalogSync.Failed -> flowOf(ExploreUiState(ExploreContent.Error(s.message)))
                    CatalogSync.Done -> repo
                        .observeCatalog(p.category)
                        .map { items ->
                            val content = if (items.isEmpty()) {
                                ExploreContent.Empty
                            } else {
                                ExploreContent.Content(items)
                            }
                            ExploreUiState(content)
                        }
                        .catch { emit(ExploreUiState(ExploreContent.Error("Não foi possível ler o catálogo."))) }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState(ExploreContent.Loading))

    init {
        syncCatalog()
    }

    fun onCategory(category: ItemCategory?) { _category.value = category }

    fun retry() = syncCatalog()

    private fun syncCatalog() {
        viewModelScope.launch {
            sync.value = CatalogSync.Running
            sync.value = repo.syncNamesIfEmpty().fold(
                onSuccess = { CatalogSync.Done },
                onFailure = { CatalogSync.Failed(it.userMessage()) },
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ExploreViewModel(appContainer().repository) }
        }
    }
}
