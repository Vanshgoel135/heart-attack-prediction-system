package com.haps.android.repository

import com.google.gson.Gson
import com.haps.android.local.PredictionDao
import com.haps.android.local.PredictionEntity
import com.haps.android.model.ClinicalFormState
import com.haps.android.model.PredictionRequest
import com.haps.android.model.PredictionResponse
import com.haps.android.network.HapsApiService
import com.haps.android.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepository @Inject constructor(
    private val apiService: HapsApiService,
    private val predictionDao: PredictionDao,
    private val gson: Gson
) {
    suspend fun predict(formState: ClinicalFormState, patientName: String? = null): Result<PredictionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val featureMap = formState.toFeatureMap()
                val request = PredictionRequest(featureMap)
                val response = apiService.predict(request)

                // Cache prediction to local Room database for offline access & history
                val inputJson = gson.toJson(featureMap)
                val topFactorsJson = gson.toJson(response.topFactors ?: emptyList<Any>())

                val entity = PredictionEntity(
                    remoteId = response.id,
                    timestamp = System.currentTimeMillis(),
                    probability = response.probability,
                    label = response.label,
                    riskLevel = response.riskLevel,
                    modelVersion = response.modelVersion ?: "1.0.0",
                    inputJson = inputJson,
                    topFactorsJson = topFactorsJson,
                    patientName = patientName
                )
                val localId = predictionDao.insertPrediction(entity)

                val resultWithId = if (response.id == null) {
                    response.copy(id = localId)
                } else response

                Result.Success(resultWithId)
            } catch (e: Exception) {
                // If network fails, return detailed error
                Result.Error(e.localizedMessage ?: "Failed to connect to HAPS prediction engine", e)
            }
        }

    suspend fun getLocalPredictions(): List<PredictionEntity> = withContext(Dispatchers.IO) {
        predictionDao.getAllPredictions()
    }

    suspend fun getPredictionById(id: Long): Result<PredictionResponse> = withContext(Dispatchers.IO) {
        try {
            // First check local Room cache
            val local = predictionDao.getPredictionById(id)
            if (local != null) {
                val topFactorsType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
                val inputType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type

                val topFactors: List<Map<String, Any>> = try {
                    gson.fromJson(local.topFactorsJson, topFactorsType)
                } catch (e: Exception) { emptyList() }

                val inputFeatures: Map<String, Any> = try {
                    gson.fromJson(local.inputJson, inputType)
                } catch (e: Exception) { emptyMap() }

                val response = PredictionResponse(
                    id = local.remoteId ?: local.id,
                    inputFeatures = inputFeatures,
                    probability = local.probability,
                    label = local.label,
                    riskLevel = local.riskLevel,
                    topFactors = topFactors,
                    modelVersion = local.modelVersion,
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date(local.timestamp))
                )
                return@withContext Result.Success(response)
            }

            // Fallback to API if not cached locally
            val remote = apiService.getPredictionById(id)
            Result.Success(remote)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to retrieve prediction detail", e)
        }
    }

    suspend fun deletePrediction(id: Long, remoteId: Long? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (remoteId != null) {
                try {
                    apiService.deletePrediction(remoteId)
                } catch (ignored: Exception) {}
            }
            predictionDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to delete prediction record", e)
        }
    }

    suspend fun getLatestPrediction(): PredictionEntity? = withContext(Dispatchers.IO) {
        predictionDao.getAllPredictions().firstOrNull()
    }
}
