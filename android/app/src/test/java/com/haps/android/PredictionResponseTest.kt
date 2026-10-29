package com.haps.android

import com.google.gson.Gson
import com.haps.android.model.PredictionResponse
import org.junit.Assert.*
import org.junit.Test

class PredictionResponseTest {

    private val gson = Gson()

    @Test
    fun testPredictionResponseJsonSerialization() {
        val json = """
            {
                "id": 101,
                "probability": 0.84,
                "label": 1,
                "riskLevel": "High Risk",
                "modelVersion": "CatBoost-1.0.0",
                "topFactors": [
                    {"feature": "cp", "importance": 0.35},
                    {"feature": "oldpeak", "importance": 0.25}
                ]
            }
        """.trimIndent()

        val response = gson.fromJson(json, PredictionResponse::class.java)

        assertEquals(101L, response.id)
        assertEquals(0.84, response.probability, 0.001)
        assertEquals(1, response.label)
        assertEquals("High Risk", response.riskLevel)
        assertEquals("CatBoost-1.0.0", response.modelVersion)
        assertEquals(2, response.topFactors?.size)
    }
}
