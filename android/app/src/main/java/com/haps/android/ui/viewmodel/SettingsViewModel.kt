package com.haps.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haps.android.data.preferences.UserPreferencesManager
import com.haps.android.local.PredictionDao
import com.haps.android.repository.AuthRepository
import com.haps.android.util.NetworkConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val authRepository: AuthRepository,
    private val predictionDao: PredictionDao,
    connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    val theme: StateFlow<String> = preferencesManager.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val serverUrl: StateFlow<String> = preferencesManager.baseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesManager.DEFAULT_BASE_URL)

    val isOnline: StateFlow<Boolean> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), connectivityObserver.isOnline)

    private val _cachedCount = MutableStateFlow(0)
    val cachedCount: StateFlow<Int> = _cachedCount.asStateFlow()

    init {
        refreshCacheCount()
    }

    fun setTheme(newTheme: String) {
        viewModelScope.launch {
            preferencesManager.setTheme(newTheme)
        }
    }

    fun setServerUrl(newUrl: String) {
        viewModelScope.launch {
            preferencesManager.setBaseUrl(newUrl.trim())
        }
    }

    fun refreshCacheCount() {
        viewModelScope.launch {
            _cachedCount.value = predictionDao.getCount()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            predictionDao.clearAll()
            _cachedCount.value = 0
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
