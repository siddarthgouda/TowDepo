package com.example.towdepo.data



data class AuthResponse(
    val user: User,
    val tokens: Tokens
)

data class Tokens(
    val access: Token,
    val refresh: Token
)

data class Token(
    val token: String,
    val expires: String
)