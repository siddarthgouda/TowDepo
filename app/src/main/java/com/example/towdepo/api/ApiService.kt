package com.example.towdepo.api

// network/ApiService.kt
import com.example.towdepo.data.ApiMessageResponse
import com.example.towdepo.data.ApiProduct
import com.example.towdepo.data.AuthResponse
import com.example.towdepo.data.LoginRequest
import com.example.towdepo.data.LogoutRequest
import com.example.towdepo.data.ProductApiResponse


import com.example.towdepo.data.RefreshTokenRequest
import com.example.towdepo.data.RegisterRequest
import com.example.towdepo.data.User
import okhttp3.Response
import retrofit2.http.*

interface ApiService {
    @POST("/v1/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse

    @POST("/v1/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): AuthResponse

    @POST("/v1/auth/logout")
    suspend fun logout(@Body logoutRequest: LogoutRequest): ApiMessageResponse

    @POST("/v1/auth/refresh-tokens")
    suspend fun refreshTokens(@Body refreshTokenRequest: RefreshTokenRequest): AuthResponse

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): User

}
// ProductApiService.kt
interface ProductApiService {
    @GET("/v1/product")
    suspend fun getAllProducts(): ProductApiResponse

    @GET("/v1/product/{id}")
    suspend fun getProductById(@Path("id") id: String): ApiProduct
}