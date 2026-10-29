package com.haps.android.model

import com.google.gson.annotations.SerializedName

data class PredictionRequest(
    @SerializedName("features") val features: Map<String, Any>
)

data class TopFactor(
    @SerializedName("feature") val feature: String,
    @SerializedName("importance") val importance: Double,
    @SerializedName("value") val value: Any? = null
)

data class PredictionResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("inputFeatures") val inputFeatures: Map<String, Any>? = null,
    @SerializedName("probability") val probability: Double = 0.0,
    @SerializedName("label") val label: Int = 0,
    @SerializedName("riskLevel") val riskLevel: String = "Low Risk",
    @SerializedName("topFactors") val topFactors: List<Map<String, Any>>? = null,
    @SerializedName("modelVersion") val modelVersion: String? = "1.0.0",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class PageResponse<T>(
    @SerializedName("content") val content: List<T> = emptyList(),
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("totalElements") val totalElements: Long = 0L,
    @SerializedName("number") val number: Int = 0,
    @SerializedName("size") val size: Int = 10,
    @SerializedName("last") val last: Boolean = true
)

// Strongly typed clinical input form data for the prediction screen
data class ClinicalFormState(
    val age: String = "55",
    val sex: Int = 1, // 1: Male, 0: Female
    val chestPainType: Int = 2, // 0: Typical Angina, 1: Atypical, 2: Non-anginal, 3: Asymptomatic
    val restingBp: String = "135", // mm Hg
    val cholesterol: String = "240", // mg/dl
    val fastingBloodSugar: Int = 0, // 1: > 120 mg/dl, 0: normal
    val restingEcg: Int = 1, // 0: Normal, 1: ST-T wave abnormality, 2: LV hypertrophy
    val maxHeartRate: String = "145", // bpm
    val exerciseAngina: Int = 0, // 1: Yes, 0: No
    val oldpeak: String = "1.4", // ST depression
    val slope: Int = 1, // 0: Upsloping, 1: Flat, 2: Downsloping
    val ca: Int = 1, // Number of major vessels (0-3)
    val thal: Int = 2, // 1: Normal, 2: Fixed defect, 3: Reversible defect
    // Candidate extras
    val smoking: Int = 1, // 1: Yes, 0: No
    val obesity: Int = 0, // 1: Yes, 0: No
    val diabetes: Int = 0, // 1: Yes, 0: No
    val hypertension: Int = 1, // 1: Yes, 0: No
    val familyHistory: Int = 1, // 1: Yes, 0: No
    val physicalActivity: Int = 1, // 0: Low, 1: Moderate, 2: High
    val dietQuality: Int = 1, // 0: Poor, 1: Average, 2: Healthy
    val alcoholUse: Int = 0, // 1: Yes, 0: No
    val airPollutionExposure: String = "45.0",
    val chestPainSeverity: Int = 2 // 0: None, 1: Mild, 2: Moderate, 3: Severe
) {
    fun toFeatureMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["age"] = age.toIntOrNull() ?: 55
        map["sex"] = sex
        map["cp"] = chestPainType
        map["trestbps"] = restingBp.toIntOrNull() ?: 130
        map["chol"] = cholesterol.toIntOrNull() ?: 240
        map["fbs"] = fastingBloodSugar
        map["restecg"] = restingEcg
        map["thalach"] = maxHeartRate.toIntOrNull() ?: 145
        map["exang"] = exerciseAngina
        map["oldpeak"] = oldpeak.toDoubleOrNull() ?: 1.0
        map["slope"] = slope
        map["ca"] = ca
        map["thal"] = thal

        // Extras from candidate list
        map["smoking"] = smoking
        map["obesity"] = obesity
        map["diabetes"] = diabetes
        map["hypertension"] = hypertension
        map["family_history"] = familyHistory
        map["physical_activity"] = physicalActivity
        map["diet_quality"] = dietQuality
        map["alcohol_use"] = alcoholUse
        map["air_pollution_exposure"] = airPollutionExposure.toDoubleOrNull() ?: 45.0
        map["chest_pain_severity"] = chestPainSeverity
        return map
    }

    companion object {
        fun sampleHighRisk() = ClinicalFormState(
            age = "63",
            sex = 1,
            chestPainType = 3,
            restingBp = "165",
            cholesterol = "295",
            fastingBloodSugar = 1,
            restingEcg = 2,
            maxHeartRate = "115",
            exerciseAngina = 1,
            oldpeak = "3.2",
            slope = 1,
            ca = 3,
            thal = 3,
            smoking = 1,
            obesity = 1,
            diabetes = 1,
            hypertension = 1,
            familyHistory = 1,
            physicalActivity = 0,
            dietQuality = 0,
            alcoholUse = 1,
            airPollutionExposure = "78.0",
            chestPainSeverity = 3
        )

        fun sampleModerateRisk() = ClinicalFormState(
            age = "52",
            sex = 1,
            chestPainType = 1,
            restingBp = "138",
            cholesterol = "242",
            fastingBloodSugar = 0,
            restingEcg = 1,
            maxHeartRate = "148",
            exerciseAngina = 0,
            oldpeak = "1.2",
            slope = 1,
            ca = 1,
            thal = 2,
            smoking = 1,
            obesity = 0,
            diabetes = 0,
            hypertension = 1,
            familyHistory = 0,
            physicalActivity = 1,
            dietQuality = 1,
            alcoholUse = 0,
            airPollutionExposure = "42.0",
            chestPainSeverity = 1
        )

        fun sampleLowRisk() = ClinicalFormState(
            age = "34",
            sex = 0,
            chestPainType = 0,
            restingBp = "116",
            cholesterol = "182",
            fastingBloodSugar = 0,
            restingEcg = 0,
            maxHeartRate = "172",
            exerciseAngina = 0,
            oldpeak = "0.2",
            slope = 0,
            ca = 0,
            thal = 1,
            smoking = 0,
            obesity = 0,
            diabetes = 0,
            hypertension = 0,
            familyHistory = 0,
            physicalActivity = 2,
            dietQuality = 2,
            alcoholUse = 0,
            airPollutionExposure = "20.0",
            chestPainSeverity = 0
        )
    }
}
