package com.example.tibiagear.data.remote

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API pública TibiaWiki (https://tibiawiki.dev) - dados de itens do jogo. Sem chave de API.
 * Os DTOs são mantidos como JSON bruto e mapeados de forma tolerante em [ItemMapper].
 */
interface TibiaWikiService {
    @GET("api/items")
    suspend fun listItems(@Query("expand") expand: Boolean): JsonElement

    @GET("api/items/{name}")
    suspend fun getItem(@Path("name") name: String): JsonObject
}
