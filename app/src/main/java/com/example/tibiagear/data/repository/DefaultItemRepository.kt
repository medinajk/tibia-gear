package com.example.tibiagear.data.repository

import com.example.tibiagear.data.local.SavedItemsStore
import com.example.tibiagear.data.remote.ItemMapper
import com.example.tibiagear.data.remote.TibiaWikiService
import com.example.tibiagear.domain.CatalogItem
import com.example.tibiagear.domain.Item
import com.example.tibiagear.domain.ItemCategory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Busca tudo na API da TibiaWiki e mantém em memória o que já foi baixado nesta sessão.
 * Só os nomes dos itens salvos são gravados no aparelho ([SavedItemsStore]).
 */
class DefaultItemRepository(
    private val api: TibiaWikiService,
    private val savedStore: SavedItemsStore,
    private val now: () -> Long = System::currentTimeMillis,
) : ItemRepository {

    // Busca e categorias podem abrir juntas. Evita duplicar a primeira carga da lista.
    private val namesSyncMutex = Mutex()
    private val catalog = MutableStateFlow<List<CatalogItem>>(emptyList())
    private val details = MutableStateFlow<Map<String, Item>>(emptyMap())

    override fun observeSaved(): Flow<List<Item>> = combine(savedStore.savedNames, details) { names, cache ->
        names.sorted().map { name -> (cache[name] ?: Item(name)).copy(saved = true) }
    }

    override fun observeItem(name: String): Flow<Item?> = combine(details, savedStore.savedNames) { cache, saved ->
        cache[name]?.copy(saved = name in saved)
    }

    override fun observeCatalog(category: ItemCategory?): Flow<List<CatalogItem>> =
        catalog.map { list -> if (category == null) list else list.filter { it.category == category } }

    override suspend fun syncNamesIfEmpty(): Result<Unit> {
        return namesSyncMutex.withLock {
            if (catalog.value.isNotEmpty()) return@withLock Result.success(Unit)
            try {
                // A lista simples evita baixar atributos de todos os itens de uma vez (erro 413).
                val names = ItemMapper.catalogFrom(api.listItems(expand = false))
                check(names.isNotEmpty()) { "Resposta vazia da API" }
                catalog.value = names.sortedBy { it.name }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchNames(query: String): List<String> {
        val safe = query.filter { it.isLetterOrDigit() || it in " '-:." }.trim()
        if (safe.length < 2) return emptyList()
        // Itens que começam com o texto digitado aparecem primeiro.
        return catalog.value.asSequence()
            .map { it.name }
            .filter { it.contains(safe, ignoreCase = true) }
            .sortedWith(compareByDescending<String> { it.startsWith(safe, ignoreCase = true) }.thenBy { it })
            .take(100)
            .toList()
    }

    override suspend fun getItem(name: String): Result<Item> {
        val cached = details.value[name]
        return try {
            val saved = name in savedStore.savedNames.first()
            val item = ItemMapper.toItem(api.getItem(name), name, saved = saved, now = now())
            details.update { it + (name to item) }
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun setSaved(name: String, saved: Boolean) = savedStore.setSaved(name, saved)

}
