package com.example.tibiagear.ui.search

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tibiagear.ui.common.ItemImage
import com.example.tibiagear.ui.common.LoadingBox
import com.example.tibiagear.ui.common.MessageBox
import com.example.tibiagear.domain.ItemCategory
import com.example.tibiagear.domain.CatalogItem
import com.example.tibiagear.ui.explore.ExploreContent
import com.example.tibiagear.ui.explore.ExploreUiState
import com.example.tibiagear.ui.legacy.XmlItemsActivity

@Composable
fun SearchRoute(
    onOpenItem: (String) -> Unit,
    onOpenSaved: () -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val exploreViewModel: com.example.tibiagear.ui.explore.ExploreViewModel = viewModel(factory = com.example.tibiagear.ui.explore.ExploreViewModel.Factory)
    val exploreState by exploreViewModel.uiState.collectAsStateWithLifecycle()
    val category by exploreViewModel.category.collectAsStateWithLifecycle()
    SearchScreen(
        query, state, exploreState, category,
        viewModel::onQueryChange, viewModel::retry, exploreViewModel::onCategory,
        exploreViewModel::retry,
        onOpenItem, onOpenSaved,
        onOpenXmlDemo = { context.startActivity(Intent(context, XmlItemsActivity::class.java)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    state: SearchUiState,
    exploreState: ExploreUiState,
    category: ItemCategory?,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onCategory: (ItemCategory?) -> Unit,
    onExploreRetry: () -> Unit,
    onOpenItem: (String) -> Unit,
    onOpenSaved: () -> Unit,
    onOpenXmlDemo: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tibia Gear") },
                actions = {
                    // Acesso discreto às telas Android Views/XML da etapa parcial (dados simulados).
                    var menuOpen by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Demonstração em Views/XML") },
                            onClick = {
                                menuOpen = false
                                onOpenXmlDemo()
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Explorar") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenSaved,
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Salvos") },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Seu próximo equipamento", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Encontre itens, compare atributos e monte seu set.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Buscar equipamento") },
                    singleLine = true,
                    isError = state.queryError != null,
                    supportingText = { state.queryError?.let { Text(it) } },
                    shape = RoundedCornerShape(18.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
                Text("Explorar itens", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 16.dp, top = 12.dp).semantics { heading() })
                val tabs: List<ItemCategory?> = listOf(null) + ItemCategory.tabs
                ScrollableTabRow(selectedTabIndex = tabs.indexOf(category).coerceAtLeast(0), edgePadding = 12.dp) {
                    tabs.forEach { c -> Tab(selected = c == category, onClick = { onCategory(c) }, text = { Text(c?.label ?: "Todos") }) }
                }
            }
            val searchContent = state.content
            if (query.trim().length >= SearchViewModel.MIN_QUERY) {
                when (searchContent) {
                    SearchContent.Loading -> item { LoadingBox(Modifier.fillMaxWidth().heightIn(min = 100.dp)) }
                    SearchContent.Idle -> Unit
                    SearchContent.Empty -> item { Text("Nenhum item encontrado. Confira a grafia em inglês.", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium) }
                    is SearchContent.Error -> item { MessageBox("Falha na busca", searchContent.message, "Tentar novamente", onRetry, Modifier.heightIn(min = 150.dp)) }
                    is SearchContent.Results -> {
                        val activeFilters = category != null
                        val catalogNames = (exploreState.content as? ExploreContent.Content)
                            ?.items?.mapTo(hashSetOf()) { it.name }
                        val matchingNames = if (!activeFilters) searchContent.names
                        else catalogNames?.let { allowed -> searchContent.names.filter { it in allowed } }.orEmpty()
                        item { Text("Resultados da busca", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp).semantics { heading() }) }
                        if (activeFilters && catalogNames == null) {
                            item { LoadingBox(Modifier.fillMaxWidth().heightIn(min = 120.dp)) }
                        } else if (matchingNames.isEmpty()) {
                            item { Text("Nenhum resultado corresponde aos filtros selecionados.", Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodyMedium) }
                        }
                        items(matchingNames, key = { "search-$it" }) { name ->
                            ListItem(
                                headlineContent = { Text(name) },
                                leadingContent = { ItemImage(name) },
                                modifier = Modifier.clickable { onOpenItem(name) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            } else {
                when (val content = exploreState.content) {
                    ExploreContent.Loading -> item { LoadingBox(Modifier.fillMaxWidth().heightIn(min = 180.dp)) }
                    ExploreContent.Empty -> item {
                        Text(
                            "Nenhum equipamento encontrado nesta categoria.",
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    is ExploreContent.Error -> item { MessageBox("Catálogo indisponível", content.message, "Tentar novamente", onExploreRetry, Modifier.heightIn(min = 180.dp)) }
                    is ExploreContent.Content -> {
                        item { Text("${content.items.size} itens", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                        items(content.items, key = { "catalog-${it.name}" }) { item -> CatalogRow(item, onOpenItem) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogRow(item: CatalogItem, onOpenItem: (String) -> Unit) {
    ListItem(
        headlineContent = { Text(item.name) },
        leadingContent = { ItemImage(item.name) },
        supportingContent = { item.summary().takeIf(String::isNotEmpty)?.let { Text(it) } },
        modifier = Modifier.clickable(onClickLabel = "Abrir detalhes de ${item.name}") { onOpenItem(item.name) },
    )
    HorizontalDivider()
}
