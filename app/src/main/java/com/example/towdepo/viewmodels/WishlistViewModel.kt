package com.example.towdepo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.towdepo.data.WishlistItem
import com.example.towdepo.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _wishlistItems = MutableStateFlow<List<WishlistItem>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItem>> = _wishlistItems.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadWishlist() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                println("🔄 DEBUG: Loading wishlist...")
                val response = wishlistRepository.getWishlist()
                if (response.isSuccessful) {
                    val items = response.body()?.results ?: emptyList()
                    println("✅ DEBUG: Successfully loaded ${items.size} wishlist items")
                    _wishlistItems.value = items
                } else {
                    val errorMsg = "Failed to load wishlist: ${response.code()}"
                    println("❌ DEBUG: $errorMsg")
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error loading wishlist: ${e.message}"
                println("❌ DEBUG: $errorMsg")
                _error.value = errorMsg
            } finally {
                _loading.value = false
            }
        }
    }

    fun addToWishlist(
        title: String,
        productId: String,
        mrp: Double,
        discount: String,
        brand: String,
        image: String
    ) {
        viewModelScope.launch {
            try {
                println("🔄 DEBUG: Adding to wishlist - product: $productId")
                val response = wishlistRepository.addToWishlist(
                    title = title,
                    productId = productId,
                    mrp = mrp,
                    discount = discount,
                    brand = brand,
                    image = image
                )
                if (response.isSuccessful) {
                    println("✅ DEBUG: Successfully added to wishlist")
                    loadWishlist() // Refresh the list
                } else {
                    println("❌ DEBUG: Failed to add to wishlist - ${response.code()}")
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Exception adding to wishlist: ${e.message}")
            }
        }
    }

    fun removeFromWishlist(itemId: String) {
        viewModelScope.launch {
            try {
                println("🔄 DEBUG: Removing from wishlist - item: $itemId")
                val response = wishlistRepository.removeFromWishlist(itemId)
                if (response.isSuccessful) {
                    println("✅ DEBUG: Successfully removed from wishlist")
                    loadWishlist() // Refresh the list
                } else {
                    println("❌ DEBUG: Failed to remove from wishlist - ${response.code()}")
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Exception removing from wishlist: ${e.message}")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}