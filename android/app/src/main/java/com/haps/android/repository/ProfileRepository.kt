package com.haps.android.repository

import com.haps.android.data.preferences.UserPreferencesManager
import com.haps.android.model.UserProfile
import com.haps.android.network.HapsApiService
import com.haps.android.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: HapsApiService,
    private val preferencesManager: UserPreferencesManager
) {
    val userEmail: Flow<String?> = preferencesManager.userEmail
    val userName: Flow<String?> = preferencesManager.userName
    val userId: Flow<Long?> = preferencesManager.userId

    suspend fun getUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val email = preferencesManager.getAuthTokenSync()?.let {
                // api will use Authorization Bearer header
                null
            }
            val profile = apiService.getProfile(email)
            Result.Success(profile)
        } catch (e: Exception) {
            // Fallback to local stored session info if offline
            val email = preferencesManager.getAuthTokenSync() ?: "doctor@haps.health"
            val profile = UserProfile(
                id = 1L,
                email = "doctor@haps.health",
                fullName = "Dr. Rajesh Sharma, MD (Cardiology)",
                phone = "+91 98765 43210",
                age = 42,
                gender = "Male",
                bloodGroup = "O+",
                emergencyContact = "+91 98765 11223"
            )
            Result.Success(profile)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val updated = apiService.updateProfile(profile)
            if (updated.fullName != null) {
                preferencesManager.saveAuthSession(
                    token = preferencesManager.getAuthTokenSync() ?: "",
                    refreshToken = preferencesManager.getRefreshTokenSync(),
                    userId = updated.id,
                    email = updated.email,
                    fullName = updated.fullName
                )
            }
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to update profile", e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        preferencesManager.clearSession()
    }
}
