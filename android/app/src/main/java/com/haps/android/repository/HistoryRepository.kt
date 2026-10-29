package com.haps.android.repository

import com.google.gson.Gson
import com.haps.android.local.PredictionDao
import com.haps.android.local.PredictionEntity
import com.haps.android.model.PageResponse
import com.haps.android.model.PredictionResponse
import com.haps.android.network.HapsApiService
import com.haps.android.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val apiService: HapsApiService,
    private val predictionDao: PredictionDao,
    private val gson: Gson
) {
    val localHistoryFlow: Flow<List<PredictionEntity>> = predictionDao.getAllPredictionsFlow()

    suspend fun getPredictions(
        riskLevel: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<PredictionResponse>> = withContext(Dispatchers.IO) {
        try {
            val filter = if (riskLevel == "ALL" || riskLevel.isNullOrBlank()) null else riskLevel
            val response: PageResponse<PredictionResponse> = apiService.getPredictions(filter, page, size)

            // Cache remote records locally
            if (response.content.isNotEmpty()) {
                val entities = response.content.map { pred ->
                    PredictionEntity(
                        remoteId = pred.id,
                        timestamp = parseTimestamp(pred.createdAt),
                        probability = pred.probability,
                        label = pred.label,
                        riskLevel = pred.riskLevel,
                        modelVersion = pred.modelVersion ?: "1.0.0",
                        inputJson = gson.toJson(pred.inputFeatures ?: emptyMap<String, Any>()),
                        topFactorsJson = gson.toJson(pred.topFactors ?: emptyList<Any>())
                    )
                }
                predictionDao.insertAll(entities)
            }

            Result.Success(response.content)
        } catch (e: Exception) {
            // Offline fallback: load from local Room database!
            val cached = if (riskLevel == null || riskLevel == "ALL") {
                predictionDao.getAllPredictions()
            } else {
                predictionDao.getPredictionsByRisk(riskLevel)
            }

            if (cached.isNotEmpty()) {
                val converted = cached.map { entity ->
                    val topFactorsType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
                    val inputType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
                    PredictionResponse(
                        id = entity.remoteId ?: entity.id,
                        inputFeatures = try { gson.fromJson(entity.inputJson, inputType) } catch (ex: Exception) { emptyMap() },
                        probability = entity.probability,
                        label = entity.label,
                        riskLevel = entity.riskLevel,
                        topFactors = try { gson.fromJson(entity.topFactorsJson, topFactorsType) } catch (ex: Exception) { emptyList() },
                        modelVersion = entity.modelVersion,
                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date(entity.timestamp))
                    )
                }
                Result.Success(converted)
            } else {
                Result.Error("Offline: No cached prediction history available.", e)
            }
        }
    }

    suspend fun deletePrediction(localId: Long, remoteId: Long?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (remoteId != null) {
                try {
                    apiService.deletePrediction(remoteId)
                } catch (ignored: Exception) {}
            }
            predictionDao.deleteById(localId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to delete prediction record", e)
        }
    }

    suspend fun clearAllHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            predictionDao.clearAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to clear history", e)
        }
    }

    private fun parseTimestamp(isoString: String?): Long {
        if (isoString.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            sdf.parse(isoString.substring(0, 19))?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
