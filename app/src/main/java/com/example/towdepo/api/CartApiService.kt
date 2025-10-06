package com.example.towdepo.api

import com.example.towdepo.data.AddToCartRequest
import com.example.towdepo.data.CartApiResponse
import com.example.towdepo.data.CartItem
import com.example.towdepo.data.UpdateCartRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface CartApiService {

    @GET("/v1/cart")
    suspend fun getCartItems(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<CartApiResponse>

    @POST("/v1/cart")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body cartRequest: AddToCartRequest
    ): Response<CartItem>

    @PUT("/v1/cart/{cartId}")
    suspend fun updateCartItem(
        @Header("Authorization") token: String,
        @Path("cartId") cartId: String,
        @Body updateRequest: UpdateCartRequest
    ): Response<CartApiResponse>

    @DELETE("/v1/cart/{cartId}")
    suspend fun deleteCartItem(
        @Header("Authorization") token: String,
        @Path("cartId") cartId: String
    ): Response<Unit>
}