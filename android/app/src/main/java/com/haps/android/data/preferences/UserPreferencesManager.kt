package com.haps.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "haps_user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("jwt_auth_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("jwt_refresh_token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_full_name")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_BASE_URL = stringPreferencesKey("backend_base_url")

        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/"
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_USER_EMAIL] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USER_NAME] }
    val rememberMe: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMEMBER_ME] ?: false }
    val appTheme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "SYSTEM" }
    val baseUrl: Flow<String> = context.dataStore.data.map { it[KEY_BASE_URL] ?: DEFAULT_BASE_URL }

    suspend fun saveAuthSession(
        token: String,
        refreshToken: String?,
        userId: Long?,
        email: String?,
        fullName: String?,
        remember: Boolean = true
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
            if (refreshToken != null) prefs[KEY_REFRESH_TOKEN] = refreshToken
            if (userId != null) prefs[KEY_USER_ID] = userId
            if (email != null) prefs[KEY_USER_EMAIL] = email
            if (fullName != null) prefs[KEY_USER_NAME] = fullName
            prefs[KEY_REMEMBER_ME] = remember
        }
    }

    suspend fun getAuthTokenSync(): String? {
        return context.dataStore.data.first()[KEY_AUTH_TOKEN]
    }

    suspend fun getRefreshTokenSync(): String? {
        return context.dataStore.data.first()[KEY_REFRESH_TOKEN]
    }

    suspend fun getBaseUrlSync(): String {
        return context.dataStore.data.first()[KEY_BASE_URL] ?: DEFAULT_BASE_URL
    }

    suspend fun setBaseUrl(url: String) {
        val sanitized = if (!url.endsWith("/")) "$url/" else url
        context.dataStore.edit { it[KEY_BASE_URL] = sanitized }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_NAME)
        }
    }
}
