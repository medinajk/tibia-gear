package com.example.tibiagear.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tibiagear.appContainer
import com.example.tibiagear.data.repository.ItemRepository
import com.example.tibiagear.data.repository.userMessage
import com.example.tibiagear.domain.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val item: Item) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(private val repo: ItemRepository, private val name: String) : ViewModel() {

    private val failure = MutableStateFlow<String?>(null)

    // Item observado do Room (fonte única da verdade) + eventual erro de carregamento.
    val uiState: StateFlow<DetailUiState> = combine(repo.observeItem(name), failure) { item, error ->
        when {
            item != null -> DetailUiState.Content(item)
            error != null -> DetailUiState.Error(error)
            else -> DetailUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    init { load() }

    fun load() {
        viewModelScope.launch {
            failure.value = null
            repo.getItem(name).onFailure { failure.value = it.userMessage() }
        }
    }

    fun toggleSaved(current: Boolean) {
        viewModelScope.launch { repo.setSaved(name, !current) }
    }

    /** Garante que o item esteja salvo e só então navega para a seleção de comparação. */
    fun saveAndCompare(onReady: () -> Unit) {
        viewModelScope.launch {
            repo.setSaved(name, true)
            onReady()
        }
    }

    companion object {
        fun factory(name: String) = viewModelFactory {
            initializer { DetailViewModel(appContainer().repository, name) }
        }
    }
}
