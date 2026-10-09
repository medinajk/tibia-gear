package com.example.tibiagear

import android.app.Application
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tibiagear.data.local.SavedItemsStore
import com.example.tibiagear.data.remote.TibiaWikiService
import com.example.tibiagear.data.repository.DefaultItemRepository
import com.example.tibiagear.data.repository.ItemRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Injeção manual de dependências. */
class AppContainer(app: Application) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val api: TibiaWikiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
        .build()
        .create(TibiaWikiService::class.java)

    val repository: ItemRepository =
        DefaultItemRepository(api, SavedItemsStore(app))
}

class TibiaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
    }

    companion object {
        lateinit var instance: TibiaApp
            private set
    }
}

@Suppress("UnusedReceiverParameter")
fun CreationExtras.appContainer(): AppContainer = TibiaApp.instance.container
