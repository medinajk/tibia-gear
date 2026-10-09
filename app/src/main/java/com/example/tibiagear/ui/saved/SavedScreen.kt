package com.example.tibiagear.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tibiagear.domain.Item
import com.example.tibiagear.ui.common.ItemImage
import com.example.tibiagear.ui.common.LoadingBox
import com.example.tibiagear.ui.common.MessageBox

@Composable
fun SavedRoute(
    preselected: String?,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onCompare: (String, String) -> Unit,
    viewModel: SavedViewModel = viewModel(factory = SavedViewModel.factory(preselected)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SavedScreen(state, onBack, onOpenItem, viewModel::toggle, viewModel::remove, onCompare)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    state: SavedUiState,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCompare: (String, String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Itens salvos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        bottomBar = {
            if (state is SavedUiState.Content) {
                Surface(tonalElevation = 3.dp) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (state.canCompare) "Pronto para comparar" else "Selecione 2 itens (${state.selected.size}/2)",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(
                            onClick = { onCompare(state.selected[0], state.selected[1]) },
                            enabled = state.canCompare,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text("Comparar selecionados") }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                SavedUiState.Loading -> LoadingBox()
                SavedUiState.Empty -> MessageBox(
                    title = "Nenhum item salvo",
                    body = "Abra um item na busca e toque em \"Salvar para comparar\". Os itens ficam disponíveis mesmo offline.",
                    actionLabel = "Ir para a busca",
                    onAction = onBack,
                )
                is SavedUiState.Error -> MessageBox(title = "Ops!", body = state.message)
                is SavedUiState.Content -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.items, key = { it.name }) { item ->
                        SavedRow(
                            item = item,
                            selected = item.name in state.selected,
                            onToggle = { onToggle(item.name) },
                            onOpen = { onOpenItem(item.name) },
                            onRemove = { onRemove(item.name) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRow(item: Item, selected: Boolean, onToggle: () -> Unit, onOpen: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = null)
        Spacer(Modifier.width(12.dp))
        ItemImage(item.name)
        Spacer(Modifier.width(12.dp))
        Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remover ${item.name} dos salvos")
        }
    }
}
