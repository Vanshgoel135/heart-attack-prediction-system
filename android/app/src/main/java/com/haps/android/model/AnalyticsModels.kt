package com.haps.android.model

import com.google.gson.annotations.SerializedName

data class FeatureFactorRecord(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("featureName") val featureName: String? = null,
    @SerializedName("chiSquareScore") val chiSquareScore: Double? = null,
    @SerializedName("mutualInformationScore") val mutualInformationScore: Double? = null,
    @SerializedName("permutationImportance") val permutationImportance: Double? = null,
    @SerializedName("pValue") val pValue: Double? = null,
    @SerializedName("selected") val selected: Boolean? = true
)

data class ModelMetricsData(
    @SerializedName("accuracy") val accuracy: Double = 0.912,
    @SerializedName("precision") val precision: Double = 0.895,
    @SerializedName("recall") val recall: Double = 0.934,
    @SerializedName("f1_score") val f1Score: Double = 0.914,
    @SerializedName("f2_score") val f2Score: Double = 0.926,
    @SerializedName("roc_auc") val rocAuc: Double = 0.958,
    @SerializedName("best_model") val bestModel: String = "CatBoostClassifier",
    @SerializedName("models_evaluated") val modelsEvaluated: List<String> = listOf("LogisticRegression", "RandomForest", "SVM", "XGBoost", "CatBoost")
)

data class DatasetSummaryData(
    @SerializedName("totalPatients") val totalPatients: Long = 3000,
    @SerializedName("highRiskCases") val highRiskCases: Long = 1420,
    @SerializedName("lowRiskCases") val lowRiskCases: Long = 1580,
    @SerializedName("malePercentage") val malePercentage: Double = 68.4,
    @SerializedName("femalePercentage") val femalePercentage: Double = 31.6,
    @SerializedName("averageAge") val averageAge: Double = 54.3,
    @SerializedName("averageCholesterol") val averageCholesterol: Double = 246.2
)

data class HealthStatus(
    @SerializedName("status") val status: String = "UP",
    @SerializedName("backendService") val backendService: String? = null,
    @SerializedName("version") val version: String? = null,
    @SerializedName("mlServiceStatus") val mlServiceStatus: String? = null
)
