package com.example.towdepo.data


data class PaymentOrderRequest(
    val amount: Double,
    val orderId: String
)

data class PaymentOrderResponse(
    val code: Int,
    val message: String,
    val data: PaymentOrderData
)

data class PaymentOrderData(
    val razorpayOrderId: String,
    val amount: Int,
    val currency: String,
    val orderId: String,
    val key: String,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val customerName: String? = null

)

data class PaymentVerificationRequest(
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String,
    val orderId: String
)

data class PaymentVerificationResponse(
    val code: Int,
    val message: String,
    val data: Any? = null // Use Any for now until you know the exact structure
)