package com.example.towdepo.api

import com.example.towdepo.data.PaymentOrderRequest
import com.example.towdepo.data.PaymentOrderResponse
import com.example.towdepo.data.PaymentVerificationRequest
import com.example.towdepo.data.PaymentVerificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PaymentApiService {
    @POST("payments/create-order")
    suspend fun createPaymentOrder(
        @Header("Authorization") token: String,
        @Body request: PaymentOrderRequest
    ): Response<PaymentOrderResponse>

    @POST("payments/verify")
    suspend fun verifyPayment(
        @Header("Authorization") token: String,
        @Body request: PaymentVerificationRequest
    ): Response<PaymentVerificationResponse>
}