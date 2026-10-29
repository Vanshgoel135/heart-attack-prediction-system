package com.haps.android.network

import com.haps.android.model.*
import retrofit2.Response
import retrofit2.http.*

interface HapsApiService {

    // Authentication Endpoints
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse

    @GET("api/auth/profile")
    suspend fun getProfile(@Query("email") email: String? = null): UserProfile

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body profile: UserProfile): UserProfile

    // Prediction Endpoints
    @POST("api/predict")
    suspend fun predict(@Body request: PredictionRequest): PredictionResponse

    @GET("api/predictions")
    suspend fun getPredictions(
        @Query("riskLevel") riskLevel: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<PredictionResponse>

    @GET("api/predictions/{id}")
    suspend fun getPredictionById(@Path("id") id: Long): PredictionResponse

    @DELETE("api/predictions/{id}")
    suspend fun deletePrediction(@Path("id") id: Long): Response<Unit>

    // Analytics and System Model
    @GET("api/model/metrics")
    suspend fun getModelMetrics(): Map<String, Any>

    @GET("api/model/schema")
    suspend fun getModelSchema(): Map<String, Any>

    @GET("api/analysis/features")
    suspend fun getFeatureAnalysis(): List<FeatureFactorRecord>

    @GET("api/dataset/summary")
    suspend fun getDatasetSummary(): Map<String, Any>

    @GET("api/health")
    suspend fun checkHealth(): HealthStatus
}
