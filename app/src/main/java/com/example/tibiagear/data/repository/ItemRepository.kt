package com.example.tibiagear.data.repository

import com.example.tibiagear.domain.CatalogItem
import com.example.tibiagear.domain.Item
import com.example.tibiagear.domain.ItemCategory
import kotlinx.coroutines.flow.Flow

/** Contrato da camada de dados (permite trocar por um fake nos testes). */
interface ItemRepository {
    fun observeSaved(): Flow<List<Item>>
    fun observeItem(name: String): Flow<Item?>

    /** Catálogo local filtrado por categoria (null = todas). */
    fun observeCatalog(category: ItemCategory?): Flow<List<CatalogItem>>

    /** Garante a lista de nomes local; só falha se não houver nada em cache e a rede falhar. */
    suspend fun syncNamesIfEmpty(): Result<Unit>
    suspend fun searchNames(query: String): List<String>

    /** Busca os detalhes na API e usa a cópia local quando estiver sem conexão. */
    suspend fun getItem(name: String): Result<Item>
    suspend fun setSaved(name: String, saved: Boolean)
}
