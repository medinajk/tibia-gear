package com.example.tibiagear.ui.nav

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.tibiagear.ui.compare.CompareRoute
import com.example.tibiagear.ui.detail.DetailRoute
import com.example.tibiagear.ui.saved.SavedRoute
import com.example.tibiagear.ui.search.SearchRoute
import kotlinx.serialization.Serializable

// Rotas (chaves do back stack). São @Serializable para sobreviver à recriação do processo.
@Serializable data object SearchKey : NavKey
@Serializable data class DetailKey(val name: String) : NavKey
@Serializable data class SavedKey(val preselected: String? = null) : NavKey
@Serializable data class CompareKey(val first: String, val second: String) : NavKey

@Composable
fun AppNav() {
    val backStack = rememberNavBackStack(SearchKey)
    val activity = LocalActivity.current
    // Na tela inicial o "voltar" fecha o app; esvaziar o back stack derruba o NavDisplay.
    val goBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeLastOrNull() else activity?.finish()
    }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SearchKey> {
                SearchRoute(
                    onOpenItem = { backStack.add(DetailKey(it)) },
                    onOpenSaved = { backStack.add(SavedKey()) },
                )
            }
            entry<DetailKey> { key ->
                DetailRoute(
                    name = key.name,
                    onBack = goBack,
                    onCompare = { backStack.add(SavedKey(preselected = it)) },
                )
            }
            entry<SavedKey> { key ->
                SavedRoute(
                    preselected = key.preselected,
                    onBack = goBack,
                    onOpenItem = { backStack.add(DetailKey(it)) },
                    onCompare = { a, b -> backStack.add(CompareKey(a, b)) },
                )
            }
            entry<CompareKey> { key ->
                CompareRoute(first = key.first, second = key.second, onBack = goBack)
            }
        },
    )
}
