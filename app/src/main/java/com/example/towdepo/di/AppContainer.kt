package com.example.towdepo.di

import android.content.Context
import com.example.towdepo.api.RetrofitInstance
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.security.TokenManager
import com.example.towdepo.viewmodels.HomeViewModel

object AppContainer {
    private lateinit var productRepository: ProductRepository

    fun initialize(context: Context) {
        val tokenManager = TokenManager(context)
        RetrofitInstance.initialize(tokenManager)

        productRepository = ProductRepository(RetrofitInstance.productApi)
    }

    fun provideHomeViewModel(): HomeViewModel {
        if (!::productRepository.isInitialized) {
            throw IllegalStateException("AppContainer not initialized. Call initialize() first.")
        }
        return HomeViewModel(productRepository)
    }
}