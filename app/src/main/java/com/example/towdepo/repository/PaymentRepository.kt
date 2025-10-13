package com.example.towdepo.repository


import com.example.towdepo.api.PaymentApiService
import com.example.towdepo.data.PaymentOrderRequest
import com.example.towdepo.data.PaymentOrderResponse
import com.example.towdepo.data.PaymentVerificationRequest
import com.example.towdepo.data.PaymentVerificationResponse


class PaymentRepository(
    private val paymentApiService: PaymentApiService
) {

    suspend fun createPaymentOrder(
        token: String,
        amount: Double,
        orderId: String
    ): Result<PaymentOrderResponse> {
        return try {
            val request = PaymentOrderRequest(amount, orderId)
            val response = paymentApiService.createPaymentOrder("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                // Better error messages
                val errorMessage = when (response.code()) {
                    404 -> "Payment service not found. Please check backend setup."
                    401 -> "Authentication failed. Please login again."
                    500 -> "Server error. Please try again later."
                    else -> "Failed to create payment order: ${response.code()}"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun verifyPayment(
        token: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
        orderId: String
    ): Result<PaymentVerificationResponse> {
        return try {
            val request = PaymentVerificationRequest(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                orderId
            )
            val response = paymentApiService.verifyPayment("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Payment verification service not found."
                    401 -> "Authentication failed."
                    else -> "Payment verification failed: ${response.code()}"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Network error: ${e.message}"))
        }
    }
}
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}