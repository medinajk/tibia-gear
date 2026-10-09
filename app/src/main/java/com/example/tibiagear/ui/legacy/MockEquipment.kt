package com.example.tibiagear.ui.legacy

/** Modelo imutável usado pelo fluxo demonstrativo em Android Views/XML. */
data class MockEquipment(
    val id: String,
    val name: String,
    val slot: String,
    val armor: Int? = null,
    val attack: Int? = null,
    val defense: Int? = null,
    val weight: Double? = null,
    val requiredLevel: Int? = null,
    val vocations: String? = null,
) {
    fun summary(): String = buildList {
        add(slot)
        armor?.let { add("Armadura $it") }
        attack?.let { add("Ataque $it") }
        defense?.let { add("Defesa $it") }
    }.joinToString(" · ")
}

object MockEquipmentCatalog {
    val all: List<MockEquipment> = listOf(
        MockEquipment(
            id = "demon_armor", name = "Demon Armor", slot = "Armadura",
            armor = 16, weight = 99.5, requiredLevel = 100, vocations = "Knight, Paladin",
        ),
        MockEquipment(
            id = "magic_plate_armor", name = "Magic Plate Armor", slot = "Armadura",
            armor = 17, weight = 85.0,
        ),
        MockEquipment(
            id = "prismatic_armor", name = "Prismatic Armor", slot = "Armadura",
            armor = 15, weight = 75.0, requiredLevel = 120, vocations = "Knight, Paladin",
        ),
        MockEquipment(
            id = "demon_shield", name = "Demon Shield", slot = "Escudo",
            defense = 35, weight = 26.0, requiredLevel = 90, vocations = "Knight, Paladin",
        ),
    )

    fun find(id: String?): MockEquipment? = all.firstOrNull { it.id == id }

}
