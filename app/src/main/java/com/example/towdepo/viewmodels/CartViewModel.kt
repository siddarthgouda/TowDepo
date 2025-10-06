package com.example.towdepo.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.towdepo.data.ApiProduct
import com.example.towdepo.data.CartItem
import com.example.towdepo.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    // Using StateFlow for better Compose integration
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadCartItems() {
        viewModelScope.launch {
            println("🛒 DEBUG: ViewModel - Loading cart items")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val items = cartRepository.getCartItems()
                _cartItems.value = items
                println("🛒 DEBUG: ViewModel - Cart items loaded: ${items.size} items")

                // Debug: Check if items have proper IDs
                items.forEachIndexed { index, item ->
                    println("🛒 DEBUG: ViewModel - Item $index '${item.title}' ID: '${item.safeId}'")
                    if (item.safeId.isEmpty()) {
                        println("🛒 DEBUG: ViewModel - WARNING: Item $index has empty ID!")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cart: ${e.message}"
                println("🛒 DEBUG: ViewModel - Error loading cart: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun addToCart(product: ApiProduct, quantity: Int = 1) {
        viewModelScope.launch {
            println("🛒 DEBUG: ViewModel - Adding ${product.title} to cart")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = cartRepository.addToCart(product, quantity)
                if (success) {
                    println("🛒 DEBUG: ViewModel - Item added successfully, refreshing cart")
                    loadCartItems()
                } else {
                    _errorMessage.value = "Failed to add item to cart"
                    println("🛒 DEBUG: ViewModel - Failed to add item to cart")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add item: ${e.message}"
                println("🛒 DEBUG: ViewModel - Exception adding to cart: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun increaseQuantity(cartItemId: String) {
        if (cartItemId.isEmpty()) {
            println("🛒 DEBUG: ViewModel - Cannot increase - empty cart item ID")
            _errorMessage.value = "Invalid cart item ID"
            return
        }

        viewModelScope.launch {
            println("🛒 DEBUG: ViewModel - Increasing quantity for cart item: $cartItemId")
            _isLoading.value = true
            try {
                val currentItem = _cartItems.value.find { it.safeId == cartItemId }
                if (currentItem != null) {
                    val newCount = currentItem.count + 1
                    val success = cartRepository.updateCartItem(cartItemId, newCount)
                    if (success) {
                        println("🛒 DEBUG: ViewModel - Quantity increased successfully, refreshing cart")
                        loadCartItems()
                    } else {
                        _errorMessage.value = "Failed to increase quantity"
                        println("🛒 DEBUG: ViewModel - Failed to increase quantity")
                    }
                } else {
                    _errorMessage.value = "Cart item not found"
                    println("🛒 DEBUG: ViewModel - Cart item not found: $cartItemId")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to increase quantity: ${e.message}"
                println("🛒 DEBUG: ViewModel - Exception increasing quantity: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun decreaseQuantity(cartItemId: String) {
        if (cartItemId.isEmpty()) {
            println("🛒 DEBUG: ViewModel - Cannot decrease - empty cart item ID")
            _errorMessage.value = "Invalid cart item ID"
            return
        }

        viewModelScope.launch {
            println("🛒 DEBUG: ViewModel - Decreasing quantity for cart item: $cartItemId")
            _isLoading.value = true
            try {
                val currentItem = _cartItems.value.find { it.safeId == cartItemId }
                if (currentItem != null) {
                    if (currentItem.count > 1) {
                        val newCount = currentItem.count - 1
                        val success = cartRepository.updateCartItem(cartItemId, newCount)
                        if (success) {
                            println("🛒 DEBUG: ViewModel - Quantity decreased successfully, refreshing cart")
                            loadCartItems()
                        } else {
                            _errorMessage.value = "Failed to decrease quantity"
                            println("🛒 DEBUG: ViewModel - Failed to decrease quantity")
                        }
                    } else {
                        deleteCartItem(cartItemId)
                    }
                } else {
                    _errorMessage.value = "Cart item not found"
                    println("🛒 DEBUG: ViewModel - Cart item not found: $cartItemId")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to decrease quantity: ${e.message}"
                println("🛒 DEBUG: ViewModel - Exception decreasing quantity: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCartItem(cartId: String) {
        if (cartId.isEmpty()) {
            println("🛒 DEBUG: ViewModel - Cannot delete - empty cart item ID")
            _errorMessage.value = "Invalid cart item ID"
            return
        }

        viewModelScope.launch {
            println("🛒 DEBUG: ViewModel - Deleting cart item $cartId")
            _isLoading.value = true
            try {
                val success = cartRepository.deleteCartItem(cartId)
                if (success) {
                    println("🛒 DEBUG: ViewModel - Cart item deleted successfully, refreshing cart")
                    loadCartItems()
                } else {
                    _errorMessage.value = "Failed to delete item from cart"
                    println("🛒 DEBUG: ViewModel - Failed to delete cart item")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item: ${e.message}"
                println("🛒 DEBUG: ViewModel - Exception deleting cart item: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCartItem(cartId: String, newCount: Int) {
        if (cartId.isEmpty()) {
            println("🛒 DEBUG: ViewModel - Cannot update - empty cart item ID")
            _errorMessage.value = "Invalid cart item ID"
            return
        }

        viewModelScope.launch {
            println("🛒 DEBUG: ViewModel - Updating cart item $cartId to count $newCount")
            _isLoading.value = true
            try {
                val success = cartRepository.updateCartItem(cartId, newCount)
                if (success) {
                    println("🛒 DEBUG: ViewModel - Cart item updated successfully, refreshing cart")
                    loadCartItems()
                } else {
                    _errorMessage.value = "Failed to update item quantity"
                    println("🛒 DEBUG: ViewModel - Failed to update cart item")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item: ${e.message}"
                println("🛒 DEBUG: ViewModel - Exception updating cart item: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun calculateTotal(): Double {
        return _cartItems.value.sumOf { item ->
            val discountValue = item.discount.toDoubleOrNull() ?: 0.0
            val discountedPrice = item.mrp - (item.mrp * discountValue / 100)
            discountedPrice * item.count
        }
    }

    fun getTotalItems(): Int {
        return _cartItems.value.sumOf { it.count }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}