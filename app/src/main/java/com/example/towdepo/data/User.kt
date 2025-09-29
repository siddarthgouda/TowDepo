package com.example.towdepo.data

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isEmailVerified: Boolean = false,
    val avatar: String? = null
)