package com.example.tibiagear.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tibiagear.appContainer
import com.example.tibiagear.data.repository.ItemRepository
import com.example.tibiagear.domain.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SavedUiState {
    data object Loading : SavedUiState
    data object Empty : SavedUiState
    data class Content(val items: List<Item>, val selected: List<String>) : SavedUiState {
        val canCompare: Boolean get() = selected.size == 2
    }
    data class Error(val message: String) : SavedUiState
}

class SavedViewModel(private val repo: ItemRepository, preselected: String?) : ViewModel() {

    // Mantém a ordem de seleção; no máximo 2 itens (o mais antigo sai quando entra um terceiro).
    private val selected = MutableStateFlow(listOfNotNull(preselected))

    val uiState: StateFlow<SavedUiState> = combine(repo.observeSaved(), selected) { items, sel ->
        val valid = sel.filter { name -> items.any { it.name == name } }
        if (items.isEmpty()) SavedUiState.Empty else SavedUiState.Content(items, valid)
    }
        .catch { emit(SavedUiState.Error("Não foi possível ler os itens salvos.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState.Loading)

    fun toggle(name: String) {
        selected.value = if (name in selected.value) {
            selected.value - name
        } else {
            (selected.value + name).takeLast(2)
        }
    }

    fun remove(name: String) {
        selected.value = selected.value - name
        viewModelScope.launch { repo.setSaved(name, false) }
    }

    companion object {
        fun factory(preselected: String?) = viewModelFactory {
            initializer { SavedViewModel(appContainer().repository, preselected) }
        }
    }
}
