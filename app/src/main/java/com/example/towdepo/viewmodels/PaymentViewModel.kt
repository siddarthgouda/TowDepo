package com.example.towdepo.viewmodels


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.towdepo.data.PaymentOrderResponse
import com.example.towdepo.data.PaymentVerificationResponse
import com.example.towdepo.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    var paymentOrderState by mutableStateOf<PaymentOrderState>(PaymentOrderState.Idle)
        private set

    var paymentVerificationState by mutableStateOf<PaymentVerificationState>(PaymentVerificationState.Idle)
        private set

    fun createPaymentOrder(token: String, amount: Double, orderId: String) {
        viewModelScope.launch {
            paymentOrderState = PaymentOrderState.Loading
            when (val result = paymentRepository.createPaymentOrder(token, amount, orderId)) {
                is com.example.towdepo.repository.Result.Success -> {
                    paymentOrderState = PaymentOrderState.Success(result.data)
                }
                is com.example.towdepo.repository.Result.Error -> {
                    paymentOrderState = PaymentOrderState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }
    }

    fun verifyPayment(
        token: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
        orderId: String
    ) {
        viewModelScope.launch {
            paymentVerificationState = PaymentVerificationState.Loading
            when (val result = paymentRepository.verifyPayment(
                token,
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                orderId
            )) {
                is com.example.towdepo.repository.Result.Success -> {
                    paymentVerificationState = PaymentVerificationState.Success(result.data)
                }
                is com.example.towdepo.repository.Result.Error -> {
                    paymentVerificationState = PaymentVerificationState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }
    }

    fun resetPaymentOrderState() {
        paymentOrderState = PaymentOrderState.Idle
    }

    fun resetPaymentVerificationState() {
        paymentVerificationState = PaymentVerificationState.Idle
    }
}

sealed class PaymentOrderState {
    object Idle : PaymentOrderState()
    object Loading : PaymentOrderState()
    data class Success(val data: PaymentOrderResponse) : PaymentOrderState()
    data class Error(val message: String) : PaymentOrderState()
}

sealed class PaymentVerificationState {
    object Idle : PaymentVerificationState()
    object Loading : PaymentVerificationState()
    data class Success(val data: PaymentVerificationResponse) : PaymentVerificationState()
    data class Error(val message: String) : PaymentVerificationState()
}