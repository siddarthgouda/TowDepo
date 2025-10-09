package com.example.towdepo.api

import com.example.towdepo.data.AddToWishlistRequest
import com.example.towdepo.data.WishlistItem
import com.example.towdepo.data.WishlistItemResponse
import com.example.towdepo.data.WishlistResponse
import retrofit2.Response
import retrofit2.http.*

interface WishlistApiService {
    @GET("/v1/wishlist")
    suspend fun getWishlist(): Response<WishlistResponse>

    @POST("/v1/wishlist")
    suspend fun addToWishlist(@Body request: AddToWishlistRequest): Response<WishlistItem>

    @DELETE("/v1/wishlist/{id}")
    suspend fun removeFromWishlist(@Path("id") id: String):Response<Void>
}

