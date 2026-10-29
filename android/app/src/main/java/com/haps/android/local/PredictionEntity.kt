package com.haps.android.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val probability: Double = 0.0,
    val label: Int = 0,
    val riskLevel: String = "Low Risk",
    val modelVersion: String = "1.0.0",
    val inputJson: String = "{}",
    val topFactorsJson: String = "[]",
    val patientName: String? = null,
    val notes: String? = null
)
