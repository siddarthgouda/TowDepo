package com.example.towdepo.repository

import com.example.towdepo.api.AddressService
import com.example.towdepo.data.Address
import com.example.towdepo.security.TokenManager
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AddressRepository(
    private val addressApiService: AddressService,
    private val tokenManager: TokenManager
) {

    suspend fun getAddresses(): List<Address> {
        try {
            val response = addressApiService.getAddresses()
            if (response.success && response.data != null) {
                return response.data
            } else {
                throw Exception(response.message ?: "Failed to fetch addresses")
            }
        } catch (e: Exception) {
            println(" DEBUG: [Repository] Get addresses error: ${e.message}")
            throw Exception("Failed to fetch addresses: ${e.message}")
        }
    }

    suspend fun createAddress(address: Address): Address {
        try {
            println(" DEBUG: [Repository] Sending address creation request...")
            val response = addressApiService.createAddress(address)
            println(" DEBUG: [Repository] Address created successfully, ID: ${response.id}")
            return response
        } catch (e: Exception) {
            println(" DEBUG: [Repository] Address creation error: ${e.message}")
            val errorMessage = when (e) {
                is HttpException -> when (e.code()) {
                    400 -> "Invalid address data"
                    409 -> "Address already exists"
                    else -> "Server error: HTTP ${e.code()}"
                }
                is SocketTimeoutException -> "Request timeout. Please try again."
                is ConnectException, is UnknownHostException -> "Cannot connect to server. Check your internet connection."
                else -> "Failed to create address: ${e.message ?: "Unknown error"}"
            }
            throw Exception(errorMessage)
        }
    }

    suspend fun getAddressesByUserId(userId: String): List<Address> {
        try {
            println(" DEBUG: [Repository] Fetching addresses for user: $userId")
            val addresses = addressApiService.getAddressesByUserId(userId)
            println(" DEBUG: [Repository] Found ${addresses.size} addresses")

            addresses.forEachIndexed { index, address ->
                println("   Address $index: ${address.id} - ${address.fullName}")
            }

            return addresses
        } catch (e: Exception) {
            println(" DEBUG: [Repository] Get addresses by user error: ${e.message}")
            // Return empty list instead of throwing for this specific case
            // so the user can still proceed to add new addresses
            return emptyList()
        }
    }

    suspend fun updateAddress(addressId: String, address: Address): Address {
        try {
            println(" DEBUG: [Repository] Updating address: $addressId")
            println(" DEBUG: [Repository] Update data - Name: ${address.fullName}, City: ${address.city}")

            val response = addressApiService.updateAddress(addressId, address)
            println(" DEBUG: [Repository] Address updated successfully: ${response.id}")
            return response
        } catch (e: Exception) {
            println(" DEBUG: [Repository] Update address error: ${e.message}")
            e.printStackTrace()

            val errorMessage = when (e) {
                is HttpException -> when (e.code()) {
                    404 -> "Address not found. It may have been deleted."
                    400 -> "Invalid address data provided"
                    403 -> "You don't have permission to update this address"
                    else -> "Server error: HTTP ${e.code()}"
                }
                is SocketTimeoutException -> "Request timeout. Please try again."
                is ConnectException, is UnknownHostException -> "Cannot connect to server. Check your internet connection."
                else -> "Failed to update address: ${e.message ?: "Unknown error"}"
            }
            throw Exception(errorMessage)
        }
    }

    suspend fun deleteAddress(addressId: String) {
        try {
            println(" DEBUG: [Repository] Deleting address: $addressId")
            val response = addressApiService.deleteAddress(addressId)

            if (response.isSuccessful) {
                println(" DEBUG: [Repository] Address deleted successfully")
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Address not found. It may have been already deleted."
                    403 -> "You don't have permission to delete this address"
                    500 -> "Server error. Please try again later."
                    else -> "Failed to delete address: HTTP ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println(" DEBUG: [Repository] Delete address error: ${e.message}")
            e.printStackTrace()

            val errorMessage = when (e) {
                is SocketTimeoutException -> "Request timeout. Please try again."
                is ConnectException, is UnknownHostException -> "Cannot connect to server. Check your internet connection."
                else -> "Failed to delete address: ${e.message ?: "Unknown error"}"
            }
            throw Exception(errorMessage)
        }
    }
}