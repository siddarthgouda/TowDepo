package com.example.towdepo.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ApiMessageResponse(
    val message: String
)