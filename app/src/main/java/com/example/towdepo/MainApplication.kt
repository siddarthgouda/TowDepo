package com.example.towdepo

import android.app.Application
import com.example.towdepo.api.CartApiService
import com.example.towdepo.di.AppContainer
import com.example.towdepo.security.TokenManager

class MainApplication : Application() {

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize TokenManager first
        tokenManager = TokenManager(this)

        // Then initialize AppContainer
        AppContainer.initialize(this,tokenManager)


    }


}