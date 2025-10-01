package com.example.towdepo.di

import com.example.towdepo.api.ProductApiService

import android.content.Context
import com.example.towdepo.repository.ProductRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppContainer {
    private const val BASE_URL = "http://10.0.2.2:3501/v1/product/" // Update with your actual URL

    lateinit var productApiService: ProductApiService
        private set

    lateinit var productRepository: ProductRepository
        private set

    fun initialize(context: Context) {
        // Initialize Retrofit
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        productApiService = retrofit.create(ProductApiService::class.java)
        productRepository = ProductRepository(productApiService)
    }
}