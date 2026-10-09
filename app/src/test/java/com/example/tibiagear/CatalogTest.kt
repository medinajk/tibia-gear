package com.example.tibiagear

import com.example.tibiagear.data.remote.ItemMapper
import com.example.tibiagear.domain.CatalogItem
import com.example.tibiagear.domain.ItemCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogTest {

    @Test fun classifiesByWikiTexts() {
        assertEquals(ItemCategory.ARMOR, ItemCategory.classify("Body Equipment", "Armors"))
        assertEquals(ItemCategory.HELMET, ItemCategory.classify("Body Equipment", "Helmets"))
        assertEquals(ItemCategory.WEAPON, ItemCategory.classify("Weapons", "Sword Weapons"))
        assertEquals(ItemCategory.SHIELD, ItemCategory.classify("Body Equipment", "Shields"))
        assertEquals(ItemCategory.RING, ItemCategory.classify("Body Equipment", "Rings"))
        assertEquals(ItemCategory.OTHER, ItemCategory.classify("Valuables", "Gems"))
    }

    @Test fun catalogParsesExpandedList() {
        val json = """[{"name":"Magic Plate Armor","itemclass":"Body Equipment","primarytype":"Armors","armor":"17","levelrequired":"0","vocrequired":"all"},
                      {"name":"Fire Sword","itemclass":"Weapons","primarytype":"Sword Weapons","attack":"24","vocrequired":"Knights and Paladins"}]"""
        val list = ItemMapper.catalogFrom(Json.parseToJsonElement(json))
        assertEquals(ItemCategory.ARMOR, list[0].category)
        assertEquals(17, list[0].armor)
        assertEquals(null, list[0].vocations)
        assertEquals("Knights and Paladins", list[1].vocations)
    }

}
