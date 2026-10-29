package com.haps.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haps.android.local.PredictionEntity
import com.haps.android.model.ClinicalFormState
import com.haps.android.model.PredictionResponse
import com.haps.android.repository.PredictionRepository
import com.haps.android.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val predictionRepository: PredictionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(ClinicalFormState())
    val formState: StateFlow<ClinicalFormState> = _formState.asStateFlow()

    private val _patientName = MutableStateFlow("Patient 001")
    val patientName: StateFlow<String> = _patientName.asStateFlow()

    private val _predictionResult = MutableStateFlow<Result<PredictionResponse>?>(null)
    val predictionResult: StateFlow<Result<PredictionResponse>?> = _predictionResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _latestCached = MutableStateFlow<PredictionEntity?>(null)
    val latestCached: StateFlow<PredictionEntity?> = _latestCached.asStateFlow()

    init {
        loadLatestPrediction()
    }

    fun updateForm(transform: (ClinicalFormState) -> ClinicalFormState) {
        _formState.value = transform(_formState.value)
    }

    fun setPatientName(name: String) {
        _patientName.value = name
    }

    fun loadSampleHighRisk() {
        _formState.value = ClinicalFormState.sampleHighRisk()
        _patientName.value = "Rajesh Gupta (High Risk)"
    }

    fun loadSampleModerateRisk() {
        _formState.value = ClinicalFormState.sampleModerateRisk()
        _patientName.value = "Sunita Verma (Moderate Risk)"
    }

    fun loadSampleLowRisk() {
        _formState.value = ClinicalFormState.sampleLowRisk()
        _patientName.value = "Aman Sharma (Low Risk)"
    }

    fun clearForm() {
        _formState.value = ClinicalFormState()
        _predictionResult.value = null
        _patientName.value = ""
    }

    fun runPrediction(onSuccess: ((PredictionResponse) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _predictionResult.value = Result.Loading

            val result = predictionRepository.predict(_formState.value, _patientName.value)
            _predictionResult.value = result
            _isLoading.value = false

            if (result is Result.Success) {
                _latestCached.value = predictionRepository.getLatestPrediction()
                onSuccess?.invoke(result.data)
            }
        }
    }

    fun loadLatestPrediction() {
        viewModelScope.launch {
            _latestCached.value = predictionRepository.getLatestPrediction()
        }
    }
}
