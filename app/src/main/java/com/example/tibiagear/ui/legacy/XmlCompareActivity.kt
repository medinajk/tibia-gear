package com.example.tibiagear.ui.legacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.tibiagear.databinding.ActivityXmlCompareBinding

/** Segunda tela XML: compara os dois itens recebidos pela Intent explícita. */
class XmlCompareActivity : ComponentActivity() {

    private lateinit var binding: ActivityXmlCompareBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityXmlCompareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val first = MockEquipmentCatalog.find(intent.getStringExtra(EXTRA_FIRST_ID))
        val second = MockEquipmentCatalog.find(intent.getStringExtra(EXTRA_SECOND_ID))
        if (first == null || second == null) {
            finish()
            return
        }

        binding.textFirstName.text = "${first.name} · ${first.slot}"
        binding.textSecondName.text = "${second.name} · ${second.slot}"
        val sameSlot = first.slot == second.slot
        binding.textCompareSummary.text = if (sameSlot) {
            "Mesmo slot. Os atributos numéricos podem ser comparados."
        } else {
            "Slots diferentes. Os valores são exibidos lado a lado, sem vencedor automático."
        }
        binding.textCompareValues.text = listOf(
            compareLine("Armadura", first.armor, second.armor, sameSlot, higherIsBetter = true),
            compareLine("Ataque", first.attack, second.attack, sameSlot, higherIsBetter = true),
            compareLine("Defesa", first.defense, second.defense, sameSlot, higherIsBetter = true),
            compareLine("Peso (oz)", first.weight, second.weight, sameSlot, higherIsBetter = false),
            "Nível necessário: ${first.requiredLevel ?: "—"} × ${second.requiredLevel ?: "—"}",
            "Vocações: ${first.vocations ?: "—"} × ${second.vocations ?: "—"}",
        ).joinToString("\n\n")

        binding.buttonBackToItems.setOnClickListener { finish() }
    }

    private fun compareLine(label: String, first: Number?, second: Number?, comparable: Boolean, higherIsBetter: Boolean): String {
        val a = first?.toString() ?: "—"
        val b = second?.toString() ?: "—"
        val marker = if (!comparable || first == null || second == null) {
            ""
        } else {
            val firstWins = (first.toDouble() > second.toDouble()) == higherIsBetter
            when {
                first.toDouble() == second.toDouble() -> " · empate"
                firstWins -> " · ${firstItemName()} tem vantagem"
                else -> " · ${secondItemName()} tem vantagem"
            }
        }
        return "$label: $a × $b$marker"
    }

    private fun firstItemName() = binding.textFirstName.text.toString().substringBefore(" · ")
    private fun secondItemName() = binding.textSecondName.text.toString().substringBefore(" · ")

    companion object {
        const val EXTRA_FIRST_ID = "com.example.tibiagear.extra.FIRST_EQUIPMENT_ID"
        const val EXTRA_SECOND_ID = "com.example.tibiagear.extra.SECOND_EQUIPMENT_ID"
    }
}
