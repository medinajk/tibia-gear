package com.example.tibiagear.domain

/** Modelo de domínio imutável. Campos opcionais são `null` quando a wiki não informa o valor. */
data class Item(
    val name: String,
    val itemClass: String? = null,
    val type: String? = null,
    val armor: Int? = null,
    val attack: Int? = null,
    val defense: Int? = null,
    val defenseMod: String? = null,
    val weight: Double? = null,
    val levelRequired: Int? = null,
    val vocations: String? = null,
    val hands: String? = null,
    val imbueSlots: Int? = null,
    val npcValue: Int? = null,
    val resistances: String? = null,
    val saved: Boolean = false,
    val fetchedAt: Long = 0L,
) {
    /** Linhas "rótulo -> valor" exibidas na tela de detalhes (somente valores conhecidos). */
    fun detailRows(): List<Pair<String, String>> = listOfNotNull(
        itemClass?.let { "Classe" to it },
        type?.let { "Tipo" to it },
        armor?.let { "Armadura" to it.toString() },
        attack?.let { "Ataque" to it.toString() },
        defense?.let { "Defesa" to it.toString() },
        defenseMod?.let { "Modificador de defesa" to it },
        hands?.let { "Mãos" to it },
        weight?.let { "Peso" to "${formatNumber(it)} oz" },
        levelRequired?.let { "Nível necessário" to it.toString() },
        vocations?.let { "Vocações" to it },
        imbueSlots?.let { "Slots de imbuement" to it.toString() },
        resistances?.let { "Resistências" to it },
        npcValue?.let { "Valor NPC" to "$it gp" },
    )

}

fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value)
