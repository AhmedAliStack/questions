package com.example.taskapp.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private const val USER_PREFERENCES_NAME = "user_preferences"
        private val Context.dataStore by preferencesDataStore(
            name = USER_PREFERENCES_NAME
        )

        private object Keys {
            val USER_SCORE = stringPreferencesKey("USER_SCORE")
        }
    }

    suspend fun setUserScore(userScore: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_SCORE] = userScore
        }
    }

    suspend fun clearDataStore() {
        context.dataStore.edit {
            it[Keys.USER_SCORE] = "0"
        }
    }

    val getUserScore: Flow<String> = context.dataStore.data.catch { exception ->
        Log.d("LogginStateEx", exception.toString())
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map {
        it[Keys.USER_SCORE] ?: "0"
    }


    data class UserPreferences(val isLoggedIn: Boolean)
}