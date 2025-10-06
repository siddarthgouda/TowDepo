package com.example.towdepo.di

import android.annotation.SuppressLint
import android.content.Context
import com.example.towdepo.api.CartApiService
import com.example.towdepo.api.ProductApiService
import com.example.towdepo.repository.CartRepository
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.security.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object AppContainer {
    private const val BASE_URL = "http://10.0.2.2:3501/v1/"

    private lateinit var _context: Context

    // Make these non-nullable with lateinit
    private lateinit var _productApiService: ProductApiService
    private lateinit var _productRepository: ProductRepository
    private lateinit var _cartRepository: CartRepository
    private lateinit var _cartApiService: CartApiService // Add this

    // Accept TokenManager from outside
    fun initialize(context: Context, tokenManager: TokenManager) {
        this._context = context.applicationContext
        initializeProductApi()
        initializeCartApi(tokenManager) // Pass tokenManager to cart initialization
    }

    private fun initializeProductApi() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val productRetrofit = Retrofit.Builder()
            .baseUrl("${BASE_URL}product/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _productApiService = productRetrofit.create(ProductApiService::class.java)
        _productRepository = ProductRepository(_productApiService)
    }

    private fun initializeCartApi(tokenManager: TokenManager) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val cartRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _cartApiService = cartRetrofit.create(CartApiService::class.java) // Store it
        _cartRepository = CartRepository(_cartApiService, tokenManager) // Use the passed TokenManager
    }

    // Non-nullable getters
    val productApiService: ProductApiService
        get() = _productApiService

    val productRepository: ProductRepository
        get() = _productRepository

    val cartRepository: CartRepository
        get() = _cartRepository

    // Add this getter for CartApiService
    val cartApiService: CartApiService
        get() = _cartApiService
}