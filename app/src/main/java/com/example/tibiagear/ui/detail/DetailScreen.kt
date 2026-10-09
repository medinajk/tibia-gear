package com.example.tibiagear.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tibiagear.domain.Item
import com.example.tibiagear.ui.common.ItemImage
import com.example.tibiagear.ui.common.LoadingBox
import com.example.tibiagear.ui.common.MessageBox

@Composable
fun DetailRoute(
    name: String,
    onBack: () -> Unit,
    onCompare: (preselected: String) -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.factory(name)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(
        name = name,
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onToggleSaved = viewModel::toggleSaved,
        onCompare = { viewModel.saveAndCompare { onCompare(name) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    name: String,
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleSaved: (Boolean) -> Unit,
    onCompare: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                DetailUiState.Loading -> LoadingBox()
                is DetailUiState.Error -> MessageBox(
                    title = "Não foi possível abrir o item",
                    body = state.message,
                    actionLabel = "Tentar novamente",
                    onAction = onRetry,
                )
                is DetailUiState.Content -> DetailContent(state.item, onToggleSaved, onCompare)
            }
        }
    }
}

@Composable
private fun DetailContent(item: Item, onToggleSaved: (Boolean) -> Unit, onCompare: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ItemImage(item.name, size = 64.dp)
            Spacer(Modifier.width(12.dp))
            Text(item.name, style = MaterialTheme.typography.headlineSmall)
        }

        val rows = item.detailRows()
        if (rows.isEmpty()) {
            Text("A TibiaWiki não possui atributos para este item.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column {
                rows.forEach { (label, value) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(vertical = 8.dp)
                            .semantics(mergeDescendants = true) {},
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(16.dp))
                        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                }
            }
        }

        Button(
            onClick = { onToggleSaved(item.saved) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Icon(if (item.saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (item.saved) "Remover dos salvos" else "Salvar para comparar")
        }
        OutlinedButton(
            onClick = onCompare,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text("Comparar com outro item salvo") }
    }
}
