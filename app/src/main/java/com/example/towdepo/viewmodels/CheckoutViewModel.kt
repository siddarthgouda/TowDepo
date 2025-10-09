package com.example.towdepo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.towdepo.data.Address
import com.example.towdepo.data.CheckoutState
import com.example.towdepo.repository.AddressRepository
import com.example.towdepo.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
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
                println("🟢 DEBUG: [ViewModel] Loaded ${addresses.size} addresses for user: $userId")
            } catch (e: Exception) {
                println("🔴 DEBUG: [ViewModel] Failed to load addresses: ${e.message}")
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load addresses"
                    )
                }
            }
        }
    }

    // DELETE Address with improved state management
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                println("🟡 DEBUG: [ViewModel] Deleting address: $addressId")
                addressRepository.deleteAddress(addressId)
                println("🟢 DEBUG: [ViewModel] Address deleted successfully")

                // Remove from local list immediately for better UX
                val currentAddresses = _checkoutState.value.addresses.toMutableList()
                val deletedAddress = currentAddresses.find { it.id == addressId }
                currentAddresses.removeAll { it.id == addressId }

                _checkoutState.update {
                    it.copy(
                        addresses = currentAddresses,
                        isLoading = false,
                        // Clear selected address if it was the one deleted
                        selectedAddress = if (it.selectedAddress?.id == addressId) {
                            currentAddresses.firstOrNull()
                        } else {
                            it.selectedAddress
                        }
                    )
                }

                println("🟢 DEBUG: [ViewModel] Local state updated after deletion")

            } catch (e: Exception) {
                println("🔴 DEBUG: [ViewModel] Failed to delete address: ${e.message}")
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete address"
                    )
                }
            }
        }
    }

    // EDIT/UPDATE Address with enhanced error handling
    fun updateAddress(addressId: String, updatedAddress: Address) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                println("🟡 DEBUG: [ViewModel] Updating address: $addressId")
                println("🟡 DEBUG: [ViewModel] New address data: ${updatedAddress.fullName}, ${updatedAddress.city}")

                val result = addressRepository.updateAddress(addressId, updatedAddress)
                println("🟢 DEBUG: [ViewModel] Address updated successfully")

                // Update the address in the local list
                val currentAddresses = _checkoutState.value.addresses.toMutableList()
                val index = currentAddresses.indexOfFirst { it.id == addressId }

                if (index != -1) {
                    currentAddresses[index] = result
                    _checkoutState.update {
                        it.copy(
                            addresses = currentAddresses,
                            isLoading = false,
                            // Update selected address if it was the one edited
                            selectedAddress = if (it.selectedAddress?.id == addressId) result else it.selectedAddress
                        )
                    }
                    println("🟢 DEBUG: [ViewModel] Local state updated after edit")
                } else {
                    println("⚠️ DEBUG: [ViewModel] Address not found in local list, reloading all addresses")
                    // If address not found in local list, reload all addresses
                    loadUserAddresses()
                }

            } catch (e: Exception) {
                println("🔴 DEBUG: [ViewModel] Failed to update address: ${e.message}")
                _checkoutState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update address"
                    )
                }
            }
        }
    }

    // CREATE/Save New Address
    fun saveAddress(address: Address) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            try {
                println("🟡 DEBUG: [ViewModel] Saving new address for user: $userId")
                // Include userId in the address before saving
                val addressWithUserId = address.copy(userId = userId)
                val savedAddress = addressRepository.createAddress(addressWithUserId)
                println("🟢 DEBUG: [ViewModel] Address saved successfully: ${savedAddress.id}")

                // Add to local list immediately for better UX
                val currentAddresses = _checkoutState.value.addresses.toMutableList()
                currentAddresses.add(savedAddress)
                _checkoutState.update {
                    it.copy(
                        addresses = currentAddresses,
                        isLoading = false,
                        // Auto-select the newly created address
                        selectedAddress = savedAddress
                    )
                }

            } catch (e: Exception) {
                println("🔴 DEBUG: [ViewModel] Failed to save address: ${e.message}")
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
                println("🟡 DEBUG: [ViewModel] Loading cart items")
                val cartItems = cartRepository.getCartItems()
                _checkoutState.update {
                    it.copy(cartItems = cartItems)
                }
                println("🟢 DEBUG: [ViewModel] Loaded ${cartItems.size} cart items")
            } catch (e: Exception) {
                println("⚠️ DEBUG: [ViewModel] Failed to load cart items: ${e.message}")
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
        // Free shipping for orders above $50, otherwise $5.99
        return if (subtotal > 50.0) 0.0 else 5.99
    }

    fun calculateTax(): Double {
        val subtotal = calculateSubtotal()
        // Assume 8% tax rate
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
        println("🟡 DEBUG: [ViewModel] Selecting address: ${address.id} - ${address.fullName}")
        _checkoutState.update { it.copy(selectedAddress = address) }
    }

    fun clearError() {
        _checkoutState.update { it.copy(error = null) }
    }

    fun placeOrder() {
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

                println("🟡 DEBUG: [ViewModel] Placing order...")
                // Here you would call your order service
                // For now, we'll just simulate success
                _checkoutState.update { it.copy(isOrderPlaced = true, isLoading = false) }
                println("🟢 DEBUG: [ViewModel] Order placed successfully")

            } catch (e: Exception) {
                println("🔴 DEBUG: [ViewModel] Failed to place order: ${e.message}")
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