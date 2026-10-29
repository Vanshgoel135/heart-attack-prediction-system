package com.haps.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haps.android.model.UserProfile
import com.haps.android.repository.ProfileRepository
import com.haps.android.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val res = profileRepository.getUserProfile()) {
                is Result.Success -> _profile.value = res.data
                is Result.Error -> _errorMessage.value = res.message
                Result.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(updated: UserProfile) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _saveSuccess.value = false

            when (val res = profileRepository.updateUserProfile(updated)) {
                is Result.Success -> {
                    _profile.value = res.data
                    _saveSuccess.value = true
                }
                is Result.Error -> {
                    _errorMessage.value = res.message
                }
                Result.Loading -> {}
            }
            _isSaving.value = false
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun logout() {
        viewModelScope.launch {
            profileRepository.logout()
        }
    }
}
