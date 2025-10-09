package com.example.towdepo.repository

import com.example.towdepo.api.WishlistApiService
import com.example.towdepo.data.AddToWishlistRequest
import com.example.towdepo.data.WishlistItem
import com.example.towdepo.data.WishlistResponse
import com.example.towdepo.security.TokenManager
import retrofit2.Response

class WishlistRepository(
    private val wishlistApiService: WishlistApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getWishlist(): Response<WishlistResponse> {
        return wishlistApiService.getWishlist()
    }

    suspend fun addToWishlist(
        title: String,
        productId: String,
        mrp: Double,
        discount: String,
        brand: String,
        image: String
    ): Response<WishlistItem> {
        val request = AddToWishlistRequest(
            title = title,
            product = productId,
            mrp = mrp,
            discount = discount,
            brand = brand,
            image = image
        )
        return wishlistApiService.addToWishlist(request)
    }

    suspend fun removeFromWishlist(id: String): Response<Void> {
        return wishlistApiService.removeFromWishlist(id)
    }
}


