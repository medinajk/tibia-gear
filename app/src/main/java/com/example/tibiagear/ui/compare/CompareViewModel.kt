package com.example.tibiagear.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tibiagear.appContainer
import com.example.tibiagear.data.repository.ItemRepository
import com.example.tibiagear.data.repository.userMessage
import com.example.tibiagear.domain.Item
import com.example.tibiagear.domain.ItemComparator
import com.example.tibiagear.domain.StatComparison
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CompareUiState {
    data object Loading : CompareUiState
    data class Content(val first: Item, val second: Item, val rows: List<StatComparison>) : CompareUiState
    data class Error(val message: String) : CompareUiState
}

class CompareViewModel(
    private val repo: ItemRepository,
    private val firstName: String,
    private val secondName: String,
) : ViewModel() {

    private val _state = MutableStateFlow<CompareUiState>(CompareUiState.Loading)
    val uiState: StateFlow<CompareUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = CompareUiState.Loading
            // Duas buscas em paralelo; ambas são canceladas junto com o viewModelScope.
            val (a, b) = coroutineScope {
                val x = async { repo.getItem(firstName) }
                val y = async { repo.getItem(secondName) }
                x.await() to y.await()
            }
            val failure = a.exceptionOrNull() ?: b.exceptionOrNull()
            _state.value = if (failure != null) {
                CompareUiState.Error(failure.userMessage())
            } else {
                val first = a.getOrThrow()
                val second = b.getOrThrow()
                CompareUiState.Content(first, second, ItemComparator.compare(first, second))
            }
        }
    }

    companion object {
        fun factory(first: String, second: String) = viewModelFactory {
            initializer { CompareViewModel(appContainer().repository, first, second) }
        }
    }
}
