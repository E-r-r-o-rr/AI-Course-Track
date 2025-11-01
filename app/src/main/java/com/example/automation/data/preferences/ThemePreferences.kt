package com.example.automation.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_settings")

class ThemePreferences private constructor(private val context: Context) {

    val themeMode: Flow<Int> = context.themeDataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    suspend fun setThemeMode(mode: Int) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")

        @Volatile
        private var INSTANCE: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
