package com.example.towdepo

import com.example.towdepo.security.TokenManager


import android.app.Application

class MainApplication : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
    }
}