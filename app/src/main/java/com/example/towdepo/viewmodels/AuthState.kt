package com.example.towdepo.viewmodels

import com.example.towdepo.data.User


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}