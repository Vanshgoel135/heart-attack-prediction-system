package com.haps.android.repository

import com.haps.android.data.preferences.UserPreferencesManager
import com.haps.android.model.*
import com.haps.android.network.HapsApiService
import com.haps.android.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: HapsApiService,
    private val preferencesManager: UserPreferencesManager
) {
    val isLoggedIn: Flow<Boolean> = preferencesManager.authToken.map { !it.isNullOrBlank() }
    val userEmail: Flow<String?> = preferencesManager.userEmail
    val userName: Flow<String?> = preferencesManager.userName
    val userId: Flow<Long?> = preferencesManager.userId

    suspend fun login(email: String, password: String, rememberMe: Boolean = true): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(AuthRequest(email.trim(), password))
                preferencesManager.saveAuthSession(
                    token = response.token,
                    refreshToken = response.refreshToken,
                    userId = response.userId,
                    email = response.email ?: email,
                    fullName = response.fullName,
                    remember = rememberMe
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Failed to login. Please verify credentials.", e)
            }
        }

    suspend fun register(email: String, password: String, fullName: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(email.trim(), password, fullName.trim()))
                preferencesManager.saveAuthSession(
                    token = response.token,
                    refreshToken = response.refreshToken,
                    userId = response.userId,
                    email = response.email ?: email,
                    fullName = response.fullName ?: fullName,
                    remember = true
                )
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Registration failed. Please check inputs.", e)
            }
        }

    suspend fun forgotPassword(email: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email.trim()))
                Result.Success(response.message)
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Could not request password reset.", e)
            }
        }

    suspend fun logout() = withContext(Dispatchers.IO) {
        preferencesManager.clearSession()
    }
}
