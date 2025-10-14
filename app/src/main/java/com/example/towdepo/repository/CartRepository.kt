package com.example.towdepo.repository

import com.example.towdepo.api.CartApiService
import com.example.towdepo.data.*
import com.example.towdepo.security.TokenManager

class CartRepository(
    private val apiService: CartApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getCartItems(): List<CartItem> {
        return try {
            println(" DEBUG: Getting cart items...")
            val token = getValidToken() ?: return emptyList()
            val response = apiService.getCartItems("Bearer $token")

            if (response.isSuccessful) {
                val cartResponse = response.body()
                val items = cartResponse?.results ?: emptyList()
                println(" DEBUG: Cart items fetched successfully: ${items.size} items")

                // Debug IDs
                items.forEachIndexed { index, item ->
                    println(" DEBUG: Item $index - ID: '${item.safeId}', Title: '${item.title}'")
                    if (item.safeId.isEmpty()) {
                        println(" DEBUG:  WARNING: Item $index has empty ID!")
                    }
                }

                items
            } else {
                println(" DEBUG: Failed to get cart items: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            println(" DEBUG: Exception getting cart items: ${e.message}")
            emptyList()
        }
    }

    suspend fun addToCart(product: ApiProduct, quantity: Int): Boolean {
        return try {
            println(" DEBUG: Adding to cart: ${product.title}, quantity: $quantity")
            val token = getValidToken() ?: return false

            // Convert your ApiProduct to CartProductRequest
            val cartProduct = CartProductRequest(
                id = product.id,
                title = product.title,
                mrp = product.mrp,
                discount = product.discount,
                brand = extractBrandFromProduct(product) // Use your existing product brand logic
            )

            val request = AddToCartRequest(
                product = cartProduct,
                quantity = quantity
            )

            println(" DEBUG: Add to cart request: $request")
            val response = apiService.addToCart("Bearer $token", request)

            if (response.isSuccessful) {
                println(" DEBUG: Item added to cart successfully")
                true
            } else {
                println(" DEBUG: Failed to add to cart: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            println(" DEBUG: Exception adding to cart: ${e.message}")
            false
        }
    }

    suspend fun updateCartItem(cartId: String, newCount: Int): Boolean {
        return try {
            println(" DEBUG: Updating cart item: $cartId, new count: $newCount")
            val token = getValidToken() ?: return false

            val request = UpdateCartRequest(count = newCount)
            val response = apiService.updateCartItem("Bearer $token", cartId, request)

            if (response.isSuccessful) {
                println(" DEBUG: Cart item updated successfully")
                true
            } else {
                println(" DEBUG: Failed to update cart item: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            println(" DEBUG: Exception updating cart item: ${e.message}")
            false
        }
    }

    suspend fun deleteCartItem(cartId: String): Boolean {
        return try {
            println(" DEBUG: Deleting cart item: $cartId")
            val token = getValidToken() ?: return false

            val response = apiService.deleteCartItem("Bearer $token", cartId)

            if (response.isSuccessful) {
                println(" DEBUG: Cart item deleted successfully")
                true
            } else {
                println(" DEBUG: Failed to delete cart item: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            println(" DEBUG: Exception deleting cart item: ${e.message}")
            false
        }
    }

    private fun getValidToken(): String? {
        return tokenManager.getAccessToken()
    }

    private fun extractBrandFromProduct(product: ApiProduct): String {
        // Use your existing logic to extract brand from ApiProduct
        // This depends on how your ApiProduct is structured
        return when {
            product.brand is String -> product.brand as String
            product.brand != null -> "Extracted Brand" // Use your existing logic
            else -> "N/A"
        }
    }
}