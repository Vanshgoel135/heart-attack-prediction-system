package com.haps.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haps.android.model.PredictionResponse
import com.haps.android.repository.HistoryRepository
import com.haps.android.repository.PredictionRepository
import com.haps.android.util.NetworkConnectivityObserver
import com.haps.android.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val predictionRepository: PredictionRepository,
    connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), connectivityObserver.isOnline)

    private val _rawList = MutableStateFlow<List<PredictionResponse>>(emptyList())
    private val _filterRisk = MutableStateFlow("ALL")
    val filterRisk: StateFlow<String> = _filterRisk.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDetail = MutableStateFlow<PredictionResponse?>(null)
    val selectedDetail: StateFlow<PredictionResponse?> = _selectedDetail.asStateFlow()

    val filteredList: StateFlow<List<PredictionResponse>> = combine(
        _rawList,
        _filterRisk,
        _searchQuery
    ) { list, filter, query ->
        list.filter { item ->
            val matchesFilter = when (filter) {
                "ALL" -> true
                else -> item.riskLevel.equals(filter, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                item.riskLevel.contains(query, ignoreCase = true) ||
                (item.id?.toString()?.contains(query) == true) ||
                (item.modelVersion?.contains(query, ignoreCase = true) == true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = historyRepository.getPredictions(_filterRisk.value)
            when (result) {
                is Result.Success -> {
                    _rawList.value = result.data
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
                Result.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun setRiskFilter(filter: String) {
        _filterRisk.value = filter
        loadHistory()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            historyRepository.deletePrediction(id, id)
            _rawList.value = _rawList.value.filter { it.id != id }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            _rawList.value = emptyList()
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            // check in raw list first
            val found = _rawList.value.find { it.id == id }
            if (found != null) {
                _selectedDetail.value = found
            } else {
                val result = predictionRepository.getPredictionById(id)
                if (result is Result.Success) {
                    _selectedDetail.value = result.data
                }
            }
            _isLoading.value = false
        }
    }
}
