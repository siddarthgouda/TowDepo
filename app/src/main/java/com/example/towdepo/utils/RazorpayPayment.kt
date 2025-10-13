package com.example.towdepo.utils

import android.app.Activity
import android.content.Context
import com.example.towdepo.data.PaymentOrderData
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class RazorpayPayment(
    private val context: Context,
    private val onSuccess: (String, String, String) -> Unit,
    private val onError: (String) -> Unit
) : PaymentResultListener {

    private var razorpayOrderId: String = ""

    fun initialize(keyId: String) {
        try {
            Checkout().setKeyID(keyId)
            println("✅ Razorpay initialized")
        } catch (e: Exception) {
            println("⚠️ Razorpay init note: ${e.message}")
        }
    }

    fun startPayment(orderData: PaymentOrderData) {
        val activity = context as? Activity ?: run {
            onError("Payment requires Activity")
            return
        }

        try {
            razorpayOrderId = orderData.razorpayOrderId

            val options = JSONObject().apply {
                put("name", "TowDepo")
                put("description", "Order #${orderData.orderId}")
                put("image", "https://your-logo-url.com/logo.png") // Add your logo
                put("order_id", orderData.razorpayOrderId)
                put("currency", orderData.currency)
                put("amount", orderData.amount)
                put("prefill", JSONObject().apply {
                    put("email", "customer@towdepo.com") // Better email
                    put("contact", "9876543210") // Better phone
                })
                put("theme", JSONObject().apply {
                    put("color", "#FF6B35")
                })
                // Add retry for better UX
                put("retry", JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 4)
                })
            }

            println("🎯 Opening Razorpay Payment Gateway...")

            val checkout = Checkout()
            checkout.setKeyID(orderData.key) // Set key again for safety
            checkout.open(activity, options)

        } catch (e: Exception) {
            println("❌ Failed to open Razorpay: ${e.message}")
            onError("Payment gateway error: ${e.message}")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        println("🎉 PAYMENT SUCCESSFUL!")
        println("📦 Order: $razorpayOrderId")
        println("💳 Payment: $razorpayPaymentId")

        if (razorpayPaymentId != null) {
            onSuccess(razorpayOrderId, razorpayPaymentId, "signature_placeholder")
        } else {
            onError("Payment completed but no ID received")
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        println("💥 Payment Failed - Code: $code, Response: $response")

        val userFriendlyError = when (code) {
            Checkout.NETWORK_ERROR -> "Please check your internet connection"
            Checkout.PAYMENT_CANCELED -> "Payment was cancelled"
            Checkout.TLS_ERROR -> "Please update the app"
            else -> "Payment failed: ${response ?: "Please try again"}"
        }

        onError(userFriendlyError)
    }
}