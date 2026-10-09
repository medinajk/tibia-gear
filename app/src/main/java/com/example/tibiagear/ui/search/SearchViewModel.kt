package com.example.tibiagear.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tibiagear.appContainer
import com.example.tibiagear.data.repository.ItemRepository
import com.example.tibiagear.data.repository.userMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchContent {
    data object Loading : SearchContent
    data object Idle : SearchContent
    data object Empty : SearchContent
    data class Results(val names: List<String>) : SearchContent
    data class Error(val message: String) : SearchContent
}

data class SearchUiState(
    val queryError: String? = null,
    val content: SearchContent = SearchContent.Loading,
)

private sealed interface SyncStatus {
    data object Running : SyncStatus
    data object Done : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

class SearchViewModel(private val repo: ItemRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val sync = MutableStateFlow<SyncStatus>(SyncStatus.Running)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = combine(_query, sync) { q, s -> q.trim() to s }
        .flatMapLatest { (q, s) -> stateFor(q, s) } // flatMapLatest cancela a busca anterior
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    init {
        syncNames()
    }

    fun onQueryChange(value: String) {
        _query.value = value.take(MAX_QUERY)
    }

    fun retry() = syncNames()

    private fun syncNames() {
        viewModelScope.launch {
            sync.value = SyncStatus.Running
            sync.value = repo.syncNamesIfEmpty().fold(
                onSuccess = { SyncStatus.Done },
                onFailure = { SyncStatus.Failed(it.userMessage()) },
            )
        }
    }

    private fun stateFor(q: String, s: SyncStatus): Flow<SearchUiState> = flow {
        val error = if (q.isNotEmpty() && q.length < MIN_QUERY) "Digite ao menos $MIN_QUERY letras" else null
        when (s) {
            SyncStatus.Running -> emit(SearchUiState(error, SearchContent.Loading))
            is SyncStatus.Failed -> emit(SearchUiState(error, SearchContent.Error(s.message)))
            SyncStatus.Done ->
                if (q.length < MIN_QUERY) {
                    emit(SearchUiState(error, SearchContent.Idle))
                } else {
                    delay(DEBOUNCE_MS)
                    val names = repo.searchNames(q)
                    emit(SearchUiState(null, if (names.isEmpty()) SearchContent.Empty else SearchContent.Results(names)))
                }
        }
    }

    companion object {
        const val MIN_QUERY = 2
        private const val MAX_QUERY = 60
        private const val DEBOUNCE_MS = 300L

        val Factory = viewModelFactory {
            initializer { SearchViewModel(appContainer().repository) }
        }
    }
}
