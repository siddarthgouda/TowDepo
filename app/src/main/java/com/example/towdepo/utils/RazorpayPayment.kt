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
    private lateinit var checkout: Checkout

    fun initialize(keyId: String) {
        try {
            checkout = Checkout()
            checkout.setKeyID(keyId)
            println("✅ Razorpay initialized with key: ${keyId.take(10)}...")
        } catch (e: Exception) {
            println("❌ Razorpay init failed: ${e.message}")
            onError("Payment initialization failed")
        }
    }

    fun startPayment(orderData: PaymentOrderData) {
        val activity = context as? Activity ?: run {
            onError("Payment requires Activity context")
            return
        }

        try {
            razorpayOrderId = orderData.razorpayOrderId

            println("🔄 User clicked Pay Now button")
            println("💰 Starting Razorpay payment with order: $razorpayOrderId")
            println("💵 Amount: ${orderData.amount}")

            val options = JSONObject().apply {
                put("name", "TowDepo")
                put("description", "Order #${orderData.orderId}")
                put("image", "https://your-logo-url.com/logo.png")
                put("order_id", razorpayOrderId)
                put("currency", orderData.currency)
                put("amount", orderData.amount)
                put("prefill", JSONObject().apply {
                    put("email", orderData.customerEmail ?: "customer@towdepo.com")
                    put("contact", orderData.customerPhone ?: "9876543210")
                    put("name", orderData.customerName ?: "Customer")
                })
                put("theme", JSONObject().apply {
                    put("color", "#FF6B35")
                })
                put("retry", JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 4)
                })
            }

            println("🎯 Opening Razorpay Payment Gateway...")

            // Make sure we're using the same checkout instance
            checkout.open(activity, options)

        } catch (e: Exception) {
            println("❌ Failed to open Razorpay: ${e.message}")
            e.printStackTrace()
            onError("Payment gateway error: ${e.message}")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        println("🎉 PAYMENT SUCCESSFUL!")
        println("📦 Order ID: $razorpayOrderId")
        println("💳 Payment ID: $razorpayPaymentId")

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
            Checkout.INVALID_OPTIONS -> "Invalid payment options"
            Checkout.PAYMENT_CANCELED -> "Payment was cancelled"
            Checkout.TLS_ERROR -> "Security error. Please update the app"
            else -> "Payment failed: ${response ?: "Unknown error"}"
        }

        onError(userFriendlyError)
    }
}