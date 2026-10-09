package com.example.tibiagear.domain

enum class Winner { First, Second, Tie, None }

data class StatComparison(
    val label: String,
    val first: String,
    val second: String,
    val winner: Winner,
)

/** Regra de negócio pura (testável sem Android) para comparar dois equipamentos. */
object ItemComparator {

    fun sameEquipmentSlot(a: Item, b: Item): Boolean =
        equipmentSlot(a) != null && equipmentSlot(a) == equipmentSlot(b)

    fun compare(a: Item, b: Item): List<StatComparison> {
        val sameSlot = sameEquipmentSlot(a, b)
        return listOf(
            numeric("Armadura", a.armor, b.armor, higherIsBetter = true, comparable = sameSlot),
            numeric("Ataque", a.attack, b.attack, higherIsBetter = true, comparable = sameSlot),
            numeric("Defesa", a.defense, b.defense, higherIsBetter = true, comparable = sameSlot),
            numeric("Peso (oz)", a.weight, b.weight, higherIsBetter = false, comparable = sameSlot),
            numeric("Nível necessário", a.levelRequired, b.levelRequired, higherIsBetter = false, comparable = false),
            numeric("Slots de imbuement", a.imbueSlots, b.imbueSlots, higherIsBetter = true, comparable = sameSlot),
            numeric("Valor NPC (gp)", a.npcValue, b.npcValue, higherIsBetter = true, comparable = false),
            text("Modificador de defesa", a.defenseMod, b.defenseMod),
            text("Mãos", a.hands, b.hands),
            text("Vocações", a.vocations, b.vocations),
            text("Resistências", a.resistances, b.resistances),
        )
    }

    private fun equipmentSlot(item: Item): String? {
        val text = listOfNotNull(item.itemClass, item.type, item.name).joinToString(" ").lowercase()
        return when {
            "helmet" in text -> "helmet"
            Regex("\\blegs?\\b").containsMatchIn(text) -> "legs"
            "boots" in text -> "boots"
            "spellbook" in text -> "spellbook"
            "shield" in text -> "shield"
            "amulet" in text || "necklace" in text -> "amulet"
            Regex("\\bring\\b").containsMatchIn(text) -> "ring"
            "armor" in text -> "armor"
            "crossbow" in text -> "crossbow"
            "bow" in text || "distance" in text -> "bow"
            "wand" in text || "rod" in text -> "wand-or-rod"
            Regex("\\baxe\\b").containsMatchIn(text) -> "axe"
            Regex("\\bclub\\b").containsMatchIn(text) -> "club"
            Regex("\\bsword\\b").containsMatchIn(text) -> "sword"
            Regex("\\bspear\\b").containsMatchIn(text) -> "spear"
            else -> null
        }
    }

    private fun numeric(
        label: String,
        x: Number?,
        y: Number?,
        higherIsBetter: Boolean,
        comparable: Boolean,
    ): StatComparison {
        val winner = if (!comparable || x == null || y == null) {
            Winner.None
        } else {
            val cmp = x.toDouble().compareTo(y.toDouble())
            when {
                cmp == 0 -> Winner.Tie
                (cmp > 0) == higherIsBetter -> Winner.First
                else -> Winner.Second
            }
        }
        return StatComparison(label, display(x), display(y), winner)
    }

    private fun text(label: String, x: String?, y: String?) =
        StatComparison(label, x ?: DASH, y ?: DASH, Winner.None)

    private fun display(n: Number?): String = when (n) {
        null -> DASH
        is Double -> formatNumber(n)
        else -> n.toString()
    }

    private const val DASH = "—"
}
