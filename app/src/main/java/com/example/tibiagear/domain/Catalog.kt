package com.example.tibiagear.domain

/** Categorias exibidas como abas na tela Explorar. */
enum class ItemCategory(val label: String) {
    ARMOR("Armaduras"),
    HELMET("Elmos"),
    LEGS("Calças"),
    BOOTS("Botas"),
    SHIELD("Escudos"),
    WEAPON("Armas"),
    AMULET("Amuletos"),
    RING("Anéis"),
    OTHER("Outros");

    companion object {
        /** Abas específicas (a aba "Todos" é tratada à parte). */
        val tabs: List<ItemCategory> = entries - OTHER

        fun fromName(name: String?): ItemCategory = entries.firstOrNull { it.name == name } ?: OTHER

        private val weaponRegex =
            Regex("weapon|distance|throwing|crossbow|\\b(axe|rod|bow|wand|club|sword|spear)s?\\b")
        private val ringRegex = Regex("\\brings?\\b")

        /** Classifica a partir dos textos da wiki (itemclass, primarytype, ...). Tolerante a variações. */
        fun classify(vararg parts: String?): ItemCategory {
            val t = parts.filterNotNull().joinToString(" ").lowercase()
            return when {
                "helmet" in t -> HELMET
                "legs" in t -> LEGS
                "boots" in t -> BOOTS
                "shield" in t || "spellbook" in t -> SHIELD
                "amulet" in t || "necklace" in t -> AMULET
                ringRegex.containsMatchIn(t) -> RING
                "armor" in t -> ARMOR
                weaponRegex.containsMatchIn(t) -> WEAPON
                else -> OTHER
            }
        }
    }
}

/** Versão leve de um item, usada para filtrar/ordenar o catálogo sem baixar cada detalhe. */
data class CatalogItem(
    val name: String,
    val category: ItemCategory,
    val levelRequired: Int?,
    val vocations: String?,
    val armor: Int?,
    val attack: Int?,
    val defense: Int?,
) {
    fun summary(): String = listOfNotNull(
        levelRequired?.let { "Nível $it" },
        armor?.let { "Armadura $it" },
        attack?.let { "Ataque $it" },
        defense?.let { "Defesa $it" },
        vocations,
    ).joinToString(" · ")
}
