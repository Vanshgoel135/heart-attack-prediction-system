package com.haps.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haps.android.model.AuthResponse
import com.haps.android.repository.AuthRepository
import com.haps.android.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _loginState = MutableStateFlow<Result<AuthResponse>?>(null)
    val loginState: StateFlow<Result<AuthResponse>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Result<AuthResponse>?>(null)
    val registerState: StateFlow<Result<AuthResponse>?> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<Result<String>?>(null)
    val forgotPasswordState: StateFlow<Result<String>?> = _forgotPasswordState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, password: String, rememberMe: Boolean = true) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _loginState.value = Result.Loading

            val result = authRepository.login(email, password, rememberMe)
            _loginState.value = result
            _isLoading.value = false

            if (result is Result.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            _errorMessage.value = "All fields are required"
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _registerState.value = Result.Loading

            val result = authRepository.register(email, password, fullName)
            _registerState.value = result
            _isLoading.value = false

            if (result is Result.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your registered email"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _forgotPasswordState.value = Result.Loading

            val result = authRepository.forgotPassword(email)
            _forgotPasswordState.value = result
            _isLoading.value = false

            if (result is Result.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    fun loginDemo() {
        login("doctor@haps.health", "Doctor@123", true)
    }

    fun clearErrors() {
        _errorMessage.value = null
        _loginState.value = null
        _registerState.value = null
        _forgotPasswordState.value = null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
