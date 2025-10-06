package com.example.towdepo.utils

import com.example.towdepo.api.CartApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Add this function to your CartScreen.kt or create a NetworkUtils.kt
private fun createCartApiService(): CartApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3501/") // Use your base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(CartApiService::class.java)
}