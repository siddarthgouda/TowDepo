package com.example.towdepo.data
// Address.kt
data class Address(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val confirmEmail: String = "",
    val addressType: String = "Shipping",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "",
    val phoneNumber: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

// ApiResponse.kt
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val status: Int = 200
)

// CheckoutState.kt
data class CheckoutState(
    val addresses: List<Address> = emptyList(),
    val selectedAddress: Address? = null,
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOrderPlaced: Boolean = false,
    val showPaymentScreen: Boolean = false,


)

// Result.kt
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}