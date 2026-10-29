package com.haps.android.network

import com.google.gson.Gson
import com.haps.android.data.preferences.UserPreferencesManager
import com.haps.android.model.AuthResponse
import com.haps.android.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val gson: Gson
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refresh fails
        if (responseCount(response) >= 3) {
            return null
        }

        val refreshToken = runBlocking { preferencesManager.getRefreshTokenSync() } ?: return null
        val baseUrl = runBlocking { preferencesManager.getBaseUrlSync() }

        synchronized(this) {
            val currentToken = runBlocking { preferencesManager.getAuthTokenSync() }
            val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")

            // If another thread already refreshed token, retry with current
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Perform synchronous refresh request
            try {
                val refreshRequestJson = gson.toJson(RefreshTokenRequest(refreshToken))
                val body = refreshRequestJson.toRequestBody("application/json".toMediaType())
                val refreshUrl = "${baseUrl}api/auth/refresh"

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(refreshUrl)
                    .post(body)
                    .build()

                val refreshResponse = client.newCall(request).execute()
                if (refreshResponse.isSuccessful) {
                    val respBody = refreshResponse.body?.string()
                    val authResp = gson.fromJson(respBody, AuthResponse::class.java)

                    if (authResp?.token != null) {
                        runBlocking {
                            preferencesManager.saveAuthSession(
                                token = authResp.token,
                                refreshToken = authResp.refreshToken ?: refreshToken,
                                userId = authResp.userId,
                                email = authResp.email,
                                fullName = authResp.fullName
                            )
                        }

                        return response.request.newBuilder()
                            .header("Authorization", "Bearer ${authResp.token}")
                            .build()
                    }
                }
            } catch (e: Exception) {
                // If refresh fails, clear session and return null
                runBlocking { preferencesManager.clearSession() }
            }
        }

        return null
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
