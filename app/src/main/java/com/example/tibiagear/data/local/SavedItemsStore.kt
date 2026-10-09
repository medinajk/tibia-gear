package com.example.tibiagear.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "saved_items")

/** Persistência local (DataStore): nomes dos itens que o usuário salvou para comparar. */
class SavedItemsStore(private val context: Context) {

    private val key = stringSetPreferencesKey("saved_names")

    val savedNames: Flow<Set<String>> = context.dataStore.data.map { it[key].orEmpty() }

    suspend fun setSaved(name: String, saved: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[key].orEmpty()
            prefs[key] = if (saved) current + name else current - name
        }
    }
}
