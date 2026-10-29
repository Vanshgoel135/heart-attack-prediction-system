package com.haps.android.model

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: Long?,
    @SerializedName("userId") val userId: Long?,
    @SerializedName("email") val email: String?,
    @SerializedName("fullName") val fullName: String?
)

data class UserProfile(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("age") val age: Int? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("bloodGroup") val bloodGroup: String? = null,
    @SerializedName("emergencyContact") val emergencyContact: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class MessageResponse(
    @SerializedName("message") val message: String
)
