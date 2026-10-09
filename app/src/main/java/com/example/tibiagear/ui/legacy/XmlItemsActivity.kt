package com.example.tibiagear.ui.legacy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.tibiagear.databinding.ActivityXmlItemsBinding
import com.example.tibiagear.databinding.ItemEquipmentRowBinding

/** Tela Android Views que permite buscar e selecionar equipamentos simulados. */
class XmlItemsActivity : ComponentActivity() {

    private lateinit var binding: ActivityXmlItemsBinding
    private val selectedIds = linkedSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityXmlItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restaura a seleção após rotação; o texto do EditText é restaurado pelo próprio Android.
        savedInstanceState?.getStringArrayList(STATE_SELECTED)?.let { selectedIds.addAll(it) }
        applySearch()
        updateSelection()

        binding.buttonSearch.setOnClickListener { applySearch() }

        binding.buttonCompare.setOnClickListener {
            val chosen = MockEquipmentCatalog.all.filter { it.id in selectedIds }
            if (chosen.size != 2) {
                updateSelection()
                return@setOnClickListener
            }
            // Intent explícita com identificadores dos dois equipamentos selecionados.
            startActivity(
                Intent(this, XmlCompareActivity::class.java)
                    .putExtra(XmlCompareActivity.EXTRA_FIRST_ID, chosen[0].id)
                    .putExtra(XmlCompareActivity.EXTRA_SECOND_ID, chosen[1].id),
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(STATE_SELECTED, ArrayList(selectedIds))
    }

    private fun applySearch() {
        val query = binding.editSearch.text?.toString().orEmpty().trim()
        renderItems(MockEquipmentCatalog.all.filter { it.name.contains(query, ignoreCase = true) })
    }

    private fun renderItems(items: List<MockEquipment>) {
        binding.equipmentList.removeAllViews()
        binding.textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        items.forEach { item ->
            val row = ItemEquipmentRowBinding.inflate(layoutInflater, binding.equipmentList, false)
            // As linhas repetem o mesmo id; a seleção é salva pela Activity, não pelo estado automático da View.
            row.checkEquipment.isSaveEnabled = false
            row.checkEquipment.text = "${item.name}\n${item.summary()}"
            row.checkEquipment.isChecked = item.id in selectedIds
            row.checkEquipment.setOnCheckedChangeListener { button, checked ->
                if (checked && selectedIds.size >= 2) {
                    button.isChecked = false
                    Toast.makeText(this, "Selecione no máximo 2 equipamentos.", Toast.LENGTH_SHORT).show()
                    return@setOnCheckedChangeListener
                }
                if (checked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                updateSelection()
            }
            binding.equipmentList.addView(row.root)
        }
    }

    private fun updateSelection() {
        binding.textSelectionCount.text = "Selecione 2 equipamentos (${selectedIds.size}/2)"
        binding.buttonCompare.isEnabled = selectedIds.size == 2
    }

    private companion object {
        const val STATE_SELECTED = "selected_ids"
    }
}
