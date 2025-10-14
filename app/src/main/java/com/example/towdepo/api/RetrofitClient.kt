package com.example.towdepo.api

import com.example.towdepo.di.AppConfig
import com.example.towdepo.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private val BASE_URL = AppConfig.getBaseUrl()

    private lateinit var tokenManager: com.example.towdepo.security.TokenManager

    fun initialize(tokenManager: com.example.towdepo.security.TokenManager) {
        this.tokenManager = tokenManager
        println(" RetrofitInstance initialized with: ${AppConfig.getEnvironmentInfo()}")
        println(" Using Base URL: $BASE_URL")
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }


}