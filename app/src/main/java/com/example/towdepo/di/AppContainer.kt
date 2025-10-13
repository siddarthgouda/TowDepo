package com.example.towdepo.di

import android.annotation.SuppressLint
import android.content.Context
import com.example.towdepo.api.AddressService
import com.example.towdepo.api.CartApiService
import com.example.towdepo.api.PaymentApiService
import com.example.towdepo.api.ProductApiService
import com.example.towdepo.api.WishlistApiService
import com.example.towdepo.repository.AddressRepository
import com.example.towdepo.repository.CartRepository
import com.example.towdepo.repository.PaymentRepository
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.repository.WishlistRepository
import com.example.towdepo.security.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object AppContainer {
    // Use dynamic base URL from AppConfig
    private val BASE_URL = AppConfig.getBaseUrl()

    private lateinit var _context: Context

    // Make these non-nullable with lateinit
    private lateinit var _productApiService: ProductApiService
    private lateinit var _productRepository: ProductRepository
    private lateinit var _cartRepository: CartRepository
    private lateinit var _cartApiService: CartApiService
    private lateinit var _wishlistRepository: WishlistRepository

    private lateinit var _addressApiService: AddressService
    private lateinit var _addressRepository: AddressRepository

    private lateinit var _paymentApiService: PaymentApiService
    private lateinit var _paymentRepository: PaymentRepository

    // Accept TokenManager from outside
    fun initialize(context: Context, tokenManager: TokenManager) {
        this._context = context.applicationContext

        // Log which environment we're using
        println("🚀 Initializing AppContainer with: ${AppConfig.getEnvironmentInfo()}")
        println("📡 Base URL: $BASE_URL")

        initializeProductApi()
        initializeCartApi(tokenManager)
        initializeWishlistApi(tokenManager)
        initializeAddressApi(tokenManager)
        initializePaymentApi(tokenManager)
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

        // Use the main BASE_URL for all services
        val productRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // ⬅️ Changed from "${BASE_URL}product/" to just BASE_URL
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

        // Add authentication interceptor for cart
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getAccessToken()

            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            if (token != null && token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
                println("🔐 DEBUG: Adding Authorization header to cart request")
            } else {
                println("⚠️ DEBUG: No token found for cart request")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val cartRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _cartApiService = cartRetrofit.create(CartApiService::class.java)
        _cartRepository = CartRepository(_cartApiService, tokenManager)
    }

    private fun initializeWishlistApi(tokenManager: TokenManager) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Add authentication interceptor
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getAccessToken()

            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            // Add Authorization header if token exists
            if (token != null && token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
                println("🔐 DEBUG: Adding Authorization header to wishlist request")
            } else {
                println("⚠️ DEBUG: No token found for wishlist request")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val wishlistRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val wishlistApiService = wishlistRetrofit.create(WishlistApiService::class.java)
        _wishlistRepository = WishlistRepository(wishlistApiService, tokenManager)
    }

    private fun initializeAddressApi(tokenManager: TokenManager) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getAccessToken()

            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            if (token != null && token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
                println("🔐 DEBUG: Adding Authorization header to address request")
            } else {
                println("⚠️ DEBUG: No token found for address request")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val addressRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _addressApiService = addressRetrofit.create(AddressService::class.java)
        _addressRepository = AddressRepository(_addressApiService, tokenManager)
    }

    private fun initializePaymentApi(tokenManager: TokenManager) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getAccessToken()

            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            if (token != null && token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
                println("🔐 DEBUG: Adding Authorization header to payment request")
            } else {
                println("⚠️ DEBUG: No token found for payment request")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val paymentRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _paymentApiService = paymentRetrofit.create(PaymentApiService::class.java)
        _paymentRepository = PaymentRepository(_paymentApiService)
    }

    // Non-nullable getters
    val productApiService: ProductApiService
        get() = _productApiService

    val productRepository: ProductRepository
        get() = _productRepository

    val cartRepository: CartRepository
        get() = _cartRepository

    val cartApiService: CartApiService
        get() = _cartApiService

    val wishlistRepository: WishlistRepository
        get() = _wishlistRepository

    val addressApiService: AddressService
        get() = _addressApiService

    val addressRepository: AddressRepository
        get() = _addressRepository

    val paymentApiService: PaymentApiService
        get() = _paymentApiService

    val paymentRepository: PaymentRepository
        get() = _paymentRepository
}