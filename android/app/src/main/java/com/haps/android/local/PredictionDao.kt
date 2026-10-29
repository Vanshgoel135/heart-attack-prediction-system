package com.haps.android.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<PredictionEntity>)

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    suspend fun getAllPredictions(): List<PredictionEntity>

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictionsFlow(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE riskLevel = :riskLevel ORDER BY timestamp DESC")
    suspend fun getPredictionsByRisk(riskLevel: String): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE id = :id LIMIT 1")
    suspend fun getPredictionById(id: Long): PredictionEntity?

    @Query("DELETE FROM predictions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM predictions")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM predictions")
    suspend fun getCount(): Int
}
