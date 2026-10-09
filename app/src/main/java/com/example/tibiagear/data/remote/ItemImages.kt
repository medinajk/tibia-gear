package com.example.tibiagear.data.remote

import java.net.URLEncoder
import java.security.MessageDigest

/**
 * URL da imagem (sprite .gif) de um item na TibiaWiki BR.
 * A API não fornece imagens; o MediaWiki guarda cada arquivo em /images/a/ab/Nome.gif,
 * onde "ab" são os dois primeiros dígitos do MD5 do nome do arquivo.
 */
object ItemImages {
    private const val BASE_URL = "https://www.tibiawiki.com.br/images/"

    fun urlFor(itemName: String): String {
        val file = itemName.trim().replace(' ', '_') + ".gif"
        val hash = MessageDigest.getInstance("MD5").digest(file.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val encoded = URLEncoder.encode(file, "UTF-8").replace("+", "%20")
        return "$BASE_URL${hash.take(1)}/${hash.take(2)}/$encoded"
    }
}
