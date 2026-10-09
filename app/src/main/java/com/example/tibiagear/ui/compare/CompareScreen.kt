package com.example.tibiagear.ui.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tibiagear.domain.Item
import com.example.tibiagear.domain.ItemComparator
import com.example.tibiagear.domain.StatComparison
import com.example.tibiagear.domain.Winner
import com.example.tibiagear.ui.common.ItemImage
import com.example.tibiagear.ui.common.LoadingBox
import com.example.tibiagear.ui.common.MessageBox

@Composable
fun CompareRoute(
    first: String,
    second: String,
    onBack: () -> Unit,
    viewModel: CompareViewModel = viewModel(factory = CompareViewModel.factory(first, second)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CompareScreen(state, onBack, viewModel::load)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(state: CompareUiState, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparar equipamentos") },
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
                CompareUiState.Loading -> LoadingBox()
                is CompareUiState.Error -> MessageBox(
                    title = "Não foi possível comparar",
                    body = state.message,
                    actionLabel = "Tentar novamente",
                    onAction = onRetry,
                )
                is CompareUiState.Content -> CompareContent(state.first, state.second, state.rows)
            }
        }
    }
}

@Composable
private fun CompareContent(first: Item, second: Item, rows: List<StatComparison>) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemHeader(first, Modifier.weight(1f))
                ItemHeader(second, Modifier.weight(1f))
            }
            if (!ItemComparator.sameEquipmentSlot(first, second)) {
                Text(
                    "Slots diferentes ou não identificados: os valores aparecem lado a lado, sem indicar um vencedor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        items(rows, key = { it.label }) { StatRow(it) }
        item {
            Text(
                "✓ indica vantagem numérica no mesmo slot. Nível necessário e valor NPC são informativos; menor peso indica um item mais leve.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ItemHeader(item: Item, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ItemImage(item.name, size = 56.dp)
        Text(item.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StatRow(row: StatComparison) {
    val description = buildString {
        append("${row.label}: ${row.first}")
        if (row.winner == Winner.First) append(" (melhor)")
        append(" contra ${row.second}")
        if (row.winner == Winner.Second) append(" (melhor)")
        if (row.winner == Winner.Tie) append(", empate")
    }
    Column(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Text(row.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ValueCell(row.first, row.winner == Winner.First, Modifier.weight(1f))
            ValueCell(row.second, row.winner == Winner.Second, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ValueCell(text: String, isWinner: Boolean, modifier: Modifier) {
    val bg = if (isWinner) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isWinner) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = if (isWinner) "✓ $text" else text,
        color = fg,
        fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .background(bg, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    )
}
