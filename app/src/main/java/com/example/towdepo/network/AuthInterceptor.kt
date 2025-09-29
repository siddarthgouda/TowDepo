package com.example.towdepo.network

// network/AuthInterceptor.kt
import com.example.towdepo.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for login/register endpoints
        if (originalRequest.url.encodedPath.contains("auth/login") ||
            originalRequest.url.encodedPath.contains("auth/register") ||
            originalRequest.url.encodedPath.contains("auth/refresh-tokens")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()
        var request = originalRequest

        // Add authorization header if token exists
        if (!accessToken.isNullOrEmpty()) {
            request = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        return chain.proceed(request)
    }
}