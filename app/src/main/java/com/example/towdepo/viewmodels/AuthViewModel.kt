package com.example.towdepo.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.towdepo.api.RetrofitInstance
import com.example.towdepo.data.LoginRequest
import com.example.towdepo.data.LogoutRequest
import com.example.towdepo.data.RegisterRequest
import com.example.towdepo.data.User
import com.example.towdepo.security.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(tokenManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(tokenManager.getUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        RetrofitInstance.initialize(tokenManager)
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.login(LoginRequest(email, password))
                tokenManager.saveAuthData(response)
                _currentUser.value = response.user
                _isLoggedIn.value = true
                _authState.value = AuthState.Success(response.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed. Check your credentials.")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.register(
                    RegisterRequest(
                        name,
                        email,
                        password
                    )
                )
                tokenManager.saveAuthData(response)
                _currentUser.value = response.user
                _isLoggedIn.value = true
                _authState.value = AuthState.Success(response.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed. Email might be already in use.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (!refreshToken.isNullOrEmpty()) {
                    RetrofitInstance.apiService.logout(LogoutRequest(refreshToken))
                }
            } catch (e: Exception) {
                // Continue with logout even if API call fails
            } finally {
                tokenManager.clearAuthData()
                _isLoggedIn.value = false
                _currentUser.value = null
                _authState.value = AuthState.Idle
            }
        }
    }
}

