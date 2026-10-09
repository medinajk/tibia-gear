package com.example.tibiagear

import com.example.tibiagear.data.remote.ItemImages
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemImagesTest {

    @Test fun buildsMediaWikiHashedPath() {
        assertEquals(
            "https://www.tibiawiki.com.br/images/e/e0/Demon_Armor.gif",
            ItemImages.urlFor("Demon Armor"),
        )
    }

    @Test fun encodesSpecialCharacters() {
        assertEquals(
            "https://www.tibiawiki.com.br/images/f/fc/Abacus_%28Replica%29.gif",
            ItemImages.urlFor("Abacus (Replica)"),
        )
    }
}
