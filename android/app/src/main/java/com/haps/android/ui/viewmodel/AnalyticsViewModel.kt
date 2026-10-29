package com.haps.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haps.android.model.DatasetSummaryData
import com.haps.android.model.FeatureFactorRecord
import com.haps.android.model.HealthStatus
import com.haps.android.model.ModelMetricsData
import com.haps.android.repository.AnalyticsRepository
import com.haps.android.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _metrics = MutableStateFlow(ModelMetricsData())
    val metrics: StateFlow<ModelMetricsData> = _metrics.asStateFlow()

    private val _featureFactors = MutableStateFlow<List<FeatureFactorRecord>>(emptyList())
    val featureFactors: StateFlow<List<FeatureFactorRecord>> = _featureFactors.asStateFlow()

    private val _datasetSummary = MutableStateFlow(DatasetSummaryData())
    val datasetSummary: StateFlow<DatasetSummaryData> = _datasetSummary.asStateFlow()

    private val _healthStatus = MutableStateFlow<HealthStatus?>(null)
    val healthStatus: StateFlow<HealthStatus?> = _healthStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Load metrics
            when (val res = analyticsRepository.getModelMetrics()) {
                is Result.Success -> _metrics.value = res.data
                is Result.Error -> _errorMessage.value = res.message
                Result.Loading -> {}
            }

            // Load feature factor correlations
            when (val res = analyticsRepository.getFeatureAnalysis()) {
                is Result.Success -> _featureFactors.value = res.data
                is Result.Error -> {}
                Result.Loading -> {}
            }

            // Load dataset summary
            when (val res = analyticsRepository.getDatasetSummary()) {
                is Result.Success -> _datasetSummary.value = res.data
                is Result.Error -> {}
                Result.Loading -> {}
            }

            // Load health check
            when (val res = analyticsRepository.getHealth()) {
                is Result.Success -> _healthStatus.value = res.data
                is Result.Error -> {}
                Result.Loading -> {}
            }

            _isLoading.value = false
        }
    }
}
