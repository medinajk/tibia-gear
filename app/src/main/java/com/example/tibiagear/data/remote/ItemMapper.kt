package com.example.tibiagear.data.remote

import com.example.tibiagear.domain.CatalogItem
import com.example.tibiagear.domain.Item
import com.example.tibiagear.domain.ItemCategory
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Conversão DTO (JSON da API) -> modelos de domínio. Tolera campos ausentes, nulos ou como string/número. */
object ItemMapper {

    fun toItem(dto: JsonObject, requestedName: String, saved: Boolean, now: Long) = Item(
        name = requestedName,
        itemClass = dto.text("itemclass", "objectclass"),
        type = dto.text("primarytype", "type"),
        armor = dto.number("armor")?.toInt(),
        attack = dto.number("attack")?.toInt(),
        defense = dto.number("defense")?.toInt(),
        defenseMod = dto.text("defensemod", "defense_mod"),
        weight = dto.number("weight"),
        levelRequired = dto.number("levelrequired", "level", "levelreq")?.toInt(),
        vocations = dto.text("vocrequired", "vocation", "vocations"),
        hands = dto.text("hands"),
        imbueSlots = dto.number("imbueslots")?.toInt(),
        npcValue = dto.number("npcvalue", "value", "npcprice")?.toInt(),
        resistances = dto.text("resist", "resistances"),
        saved = saved,
        fetchedAt = now,
    )

    /**
     * Catálogo a partir de /api/items (com `expand=true` cada elemento é um objeto com atributos).
     * Também aceita ["Nome", ...]: nesse caso os itens ficam sem categoria/nível (categoria "Outros").
     */
    fun catalogFrom(element: JsonElement): List<CatalogItem> {
        val array: JsonArray = when (element) {
            is JsonArray -> element
            is JsonObject -> element.values.filterIsInstance<JsonArray>().firstOrNull() ?: return emptyList()
            else -> return emptyList()
        }
        return array.mapNotNull { e ->
            if (e is JsonObject) {
                val name = e.text("name", "title", "actualname") ?: return@mapNotNull null
                CatalogItem(
                    name = name,
                    category = ItemCategory.classify(
                        e.text("itemclass", "objectclass"), e.text("primarytype", "type"), e.text("secondarytype"), name,
                    ),
                    levelRequired = e.number("levelrequired", "level", "levelreq")?.toInt(),
                    vocations = e.text("vocrequired", "vocation", "vocations")
                        ?.takeUnless { it.equals("all", true) || it.equals("none", true) },
                    armor = e.number("armor")?.toInt(),
                    attack = e.number("attack")?.toInt(),
                    defense = e.number("defense")?.toInt(),
                )
            } else {
                e.asText()?.let { name ->
                    CatalogItem(name, ItemCategory.classify(null, name), null, null, null, null, null)
                }
            }
        }.distinctBy { it.name }
    }

    fun namesFrom(element: JsonElement): List<String> = catalogFrom(element).map { it.name }

    private val numberRegex = Regex("-?\\d+(?:\\.\\d+)?")

    private fun JsonObject.text(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { this[it].asText() }

    private fun JsonObject.number(vararg keys: String): Double? =
        keys.firstNotNullOfOrNull { key -> this[key].asText()?.let { numberRegex.find(it)?.value?.toDoubleOrNull() } }

    private fun JsonElement?.asText(): String? = when (this) {
        null, is JsonNull -> null
        is JsonPrimitive -> contentOrNull?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", true) && it != "?" }
        is JsonArray -> mapNotNull { it.asText() }.joinToString(", ").ifBlank { null }
        is JsonObject -> entries.mapNotNull { (k, v) -> v.asText()?.let { "$k $it" } }.joinToString(", ").ifBlank { null }
    }
}
