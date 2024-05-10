package com.magic.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalDataStoreDataSource @Inject constructor(
    private val dataStore : DataStore<Preferences> ,
) {
    suspend fun setPath(pathId : Int) {
        dataStore.setValue(PATH , pathId)
    }

    suspend fun getPath() : Int =
        dataStore.data.map {
            it[PATH] ?: 0
        }.first()

    private suspend fun <T> DataStore<Preferences>.setValue(
        key : Preferences.Key<T> ,
        value : T ,
    ) {
        this.edit { preferences ->
            preferences[key] = value
        }
    }

    companion object {
        val PATH = intPreferencesKey("path")
    }
}