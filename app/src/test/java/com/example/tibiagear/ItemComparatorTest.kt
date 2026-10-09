package com.example.tibiagear

import com.example.tibiagear.data.remote.ItemMapper
import com.example.tibiagear.domain.Item
import com.example.tibiagear.domain.ItemComparator
import com.example.tibiagear.domain.Winner
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemComparatorTest {

    private fun row(a: Item, b: Item, label: String) = ItemComparator.compare(a, b).first { it.label == label }

    @Test fun higherArmorWins() {
        val r = row(Item("Demon Armor", armor = 17), Item("Magic Plate Armor", armor = 12), "Armadura")
        assertEquals(Winner.First, r.winner)
    }

    @Test fun lowerWeightWins() {
        val r = row(Item("Demon Armor", weight = 80.0), Item("Magic Plate Armor", weight = 50.0), "Peso (oz)")
        assertEquals(Winner.Second, r.winner)
    }

    @Test fun missingValueHasNoWinner() {
        val r = row(Item("A", attack = 40), Item("B"), "Ataque")
        assertEquals(Winner.None, r.winner)
        assertEquals("—", r.second)
    }

    @Test fun equalValuesTie() {
        assertEquals(Winner.Tie, row(Item("Demon Shield", defense = 10), Item("Mastermind Shield", defense = 10), "Defesa").winner)
    }

    @Test fun mapperToleratesStringsNumbersAndNulls() {
        val dto = Json.parseToJsonElement(
            """{"name":"Demon Armor","armor":"16","weight":80.0,"levelrequired":null,"attack":"?","vocrequired":["Knight","Paladin"]}"""
        ).jsonObject
        val e = ItemMapper.toItem(dto, "Demon Armor", saved = false, now = 1L)
        assertEquals(16, e.armor)
        assertEquals(80.0, e.weight!!, 0.0)
        assertNull(e.levelRequired)
        assertNull(e.attack)
        assertEquals("Knight, Paladin", e.vocations)
    }

    @Test fun namesAcceptStringsOrObjects() {
        val a = ItemMapper.namesFrom(Json.parseToJsonElement("""["Sword","Axe"]"""))
        val b = ItemMapper.namesFrom(Json.parseToJsonElement("""[{"name":"Sword"},{"name":"Axe"}]"""))
        assertEquals(a, b)
    }
}
