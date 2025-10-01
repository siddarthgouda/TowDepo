package com.example.towdepo


import android.app.Application
import com.example.towdepo.security.TokenManager

class MainApplication : Application() {

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize your dependencies here
        tokenManager = TokenManager(this)
    }
}