package com.example.towdepo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.towdepo.data.Address
import com.example.towdepo.data.CheckoutState
import com.example.towdepo.repository.AddressRepository
import com.example.towdepo.repository.CartRepository
import com.example.towdepo.security.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val tokenManager: TokenManager.Companion,
    private val userId: String
) : ViewModel() {
    private val _checkoutState = MutableStateFlow(CheckoutState())
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()
    

    init {
        loadUserAddresses()
        loadCartItems()
    }

    fun loadUserAddresses() {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                val addresses = addressRepository.getAddressesByUserId(userId)
                _checkoutState.update {
                    it.copy(
                        addresses = addresses,
                        selectedAddress = addresses.firstOrNull(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load addresses"
                    )
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                addressRepository.deleteAddress(addressId)

                val currentAddresses = _checkoutState.value.addresses.toMutableList()
                currentAddresses.removeAll { it.id == addressId }

                _checkoutState.update {
                    it.copy(
                        addresses = currentAddresses,
                        isLoading = false,
                        selectedAddress = if (it.selectedAddress?.id == addressId) {
                            currentAddresses.firstOrNull()
                        } else {
                            it.selectedAddress
                        }
                    )
                }
            } catch (e: Exception) {
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete address"
                    )
                }
            }
        }
    }

    fun updateAddress(addressId: String, updatedAddress: Address) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = addressRepository.updateAddress(addressId, updatedAddress)

                val currentAddresses = _checkoutState.value.addresses.toMutableList()
                val index = currentAddresses.indexOfFirst { it.id == addressId }

                if (index != -1) {
                    currentAddresses[index] = result
                    _checkoutState.update {
                        it.copy(
                            addresses = currentAddresses,
                            isLoading = false,
                            selectedAddress = if (it.selectedAddress?.id == addressId) result else it.selectedAddress
                        )
                    }
                } else {
                    loadUserAddresses()
                }
            } catch (e: Exception) {
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update address"
                    )
                }
            }
        }
    }

    fun saveAddress(address: Address) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                val addressWithUserId = address.copy(userId = userId)
                val savedAddress = addressRepository.createAddress(addressWithUserId)

                val currentAddresses = _checkoutState.value.addresses.toMutableList()
                currentAddresses.add(savedAddress)
                _checkoutState.update {
                    it.copy(
                        addresses = currentAddresses,
                        isLoading = false,
                        selectedAddress = savedAddress
                    )
                }
            } catch (e: Exception) {
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save address"
                    )
                }
            }
        }
    }

    fun loadCartItems() {
        viewModelScope.launch {
            try {
                val cartItems = cartRepository.getCartItems()
                _checkoutState.update {
                    it.copy(cartItems = cartItems)
                }
            } catch (e: Exception) {
                // Don't show error for cart items to avoid blocking checkout
            }
        }
    }

    // Calculate order totals
    fun calculateSubtotal(): Double {
        return _checkoutState.value.cartItems.sumOf { item ->
            val discountedPrice = calculateDiscountedPrice(item.mrp, item.discount)
            discountedPrice.toDouble() * item.count
        }
    }

    fun calculateShipping(): Double {
        val subtotal = calculateSubtotal()
        return if (subtotal > 50.0) 0.0 else 5.99
    }

    fun calculateTax(): Double {
        val subtotal = calculateSubtotal()
        return subtotal * 0.08
    }

    fun calculateTotal(): Double {
        return calculateSubtotal() + calculateShipping() + calculateTax()
    }

    fun getTotalItems(): Int {
        return _checkoutState.value.cartItems.sumOf { it.count }
    }

    private fun calculateDiscountedPrice(mrp: Double, discount: String): Double {
        val discountValue = discount.toDoubleOrNull() ?: 0.0
        return mrp - (mrp * discountValue / 100)
    }

    fun selectAddress(address: Address) {
        _checkoutState.update { it.copy(selectedAddress = address) }
    }

    fun clearError() {
        _checkoutState.update { it.copy(error = null) }
    }

    fun placeOrder(userId: String) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                // Validate required fields before placing order
                if (_checkoutState.value.selectedAddress == null) {
                    throw Exception("Please select a shipping address")
                }

                if (_checkoutState.value.cartItems.isEmpty()) {
                    throw Exception("Your cart is empty")
                }

                println(" DEBUG: [ViewModel] Placing order...")
                // Here you would call your order service
                // For now, we'll just simulate success
                _checkoutState.update { it.copy(isOrderPlaced = true, isLoading = false) }
                println(" DEBUG: [ViewModel] Order placed successfully")

            } catch (e: Exception) {
                println(" DEBUG: [ViewModel] Failed to place order: ${e.message}")
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to place order"
                    )
                }
            }
        }
    }
}