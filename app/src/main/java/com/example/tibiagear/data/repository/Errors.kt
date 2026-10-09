package com.example.tibiagear.data.repository

import java.io.IOException
import retrofit2.HttpException

fun Throwable.userMessage(): String = when {
    this is HttpException && code() == 404 -> "Item não encontrado na TibiaWiki."
    this is HttpException -> "O servidor respondeu com erro (${code()}). Tente novamente."
    this is IOException -> "Sem conexão com a internet. Verifique sua rede e tente novamente."
    else -> "Não foi possível carregar os dados. Tente novamente."
}
