package com.example.towdepo.ui.theme.screnns

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.towdepo.di.AppContainer
import com.example.towdepo.security.TokenManager
import com.example.towdepo.utils.RazorpayPayment
import com.example.towdepo.viewmodels.PaymentOrderState
import com.example.towdepo.viewmodels.PaymentVerificationState
import com.example.towdepo.viewmodels.PaymentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
// screnns/PaymentScreen.kt
@Composable
fun PaymentScreen(
    tokenManager: TokenManager,
    orderId: String,
    amount: Double,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { PaymentViewModel(AppContainer.paymentRepository) }
    val coroutineScope = rememberCoroutineScope()

    val paymentOrderState = viewModel.paymentOrderState
    val verificationState = viewModel.paymentVerificationState

    var razorpayPayment by remember { mutableStateOf<RazorpayPayment?>(null) }
    val token = remember { tokenManager.getAccessToken() ?: "" }

    // Debug logs
    LaunchedEffect(paymentOrderState) {
        println(" Payment Order State Changed: $paymentOrderState")
    }

    LaunchedEffect(verificationState) {
        println(" Verification State Changed: $verificationState")
    }

    // Step 1: Create payment order when screen loads
    LaunchedEffect(Unit) {
        println(" PaymentScreen launched")
        println("Order ID: $orderId, Amount: $amount, Token: ${token.take(10)}...")

        if (token.isNotEmpty()) {
            println(" Calling createPaymentOrder...")
            viewModel.createPaymentOrder(token, amount, orderId)
        } else {
            onPaymentFailed("User not authenticated")
        }
    }

    // Step 2: Initialize Razorpay when order is created
    LaunchedEffect(paymentOrderState) {
        if (paymentOrderState is PaymentOrderState.Success) {
            val successState = paymentOrderState as PaymentOrderState.Success
            val orderData = successState.data.data

            println(" Payment order created successfully!")
            println("Razorpay Order ID: ${orderData.razorpayOrderId}")
            println("Razorpay Key: ${orderData.key.take(10)}...")
            println("Amount in paise: ${orderData.amount} (₹${orderData.amount / 100})")

            razorpayPayment = RazorpayPayment(
                context = context,
                onSuccess = { razorpayOrderId, razorpayPaymentId, razorpaySignature ->
                    println(" Razorpay payment successful!")
                    println("Order ID: $razorpayOrderId")
                    println("Payment ID: $razorpayPaymentId")
                    println("Signature: $razorpaySignature")

                    coroutineScope.launch {
                        println(" Calling verifyPayment...")
                        viewModel.verifyPayment(
                            token = token,
                            razorpayOrderId = razorpayOrderId,
                            razorpayPaymentId = razorpayPaymentId,
                            razorpaySignature = razorpaySignature,
                            orderId = orderId
                        )
                    }
                },
                onError = { error ->
                    println(" Razorpay error: $error")
                    onPaymentFailed("Payment failed: $error")
                }
            ).apply {
                println(" Initializing Razorpay with key...")
                initialize(orderData.key)
            }
        }
    }

    // Step 3: Handle verification success
    LaunchedEffect(verificationState) {
        if (verificationState is PaymentVerificationState.Success) {
            println(" Payment verified successfully!")
            onPaymentSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (paymentOrderState) {
                is PaymentOrderState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Setting up payment...")
                    }
                }

                is PaymentOrderState.Success -> {
                    val orderData = (paymentOrderState as PaymentOrderState.Success).data.data

                    when (verificationState) {
                        is PaymentVerificationState.Loading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Verifying payment...")
                            }
                        }

                        is PaymentVerificationState.Error -> {
                            val error = (verificationState as PaymentVerificationState.Error).message
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Verification Failed", color = MaterialTheme.colorScheme.error)
                                Text(error, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    viewModel.resetPaymentVerificationState()
                                    razorpayPayment?.startPayment(orderData)
                                }) {
                                    Text("Retry Payment")
                                }
                            }
                        }

                        else -> {
                            // Ready to pay - Show payment button
                            Button(
                                onClick = {
                                    println("🔄 User clicked Pay Now button")
                                    println("Starting Razorpay payment with order: ${orderData.razorpayOrderId}")
                                    razorpayPayment?.startPayment(orderData)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    "Pay Now - ₹${String.format("%.2f", amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is PaymentOrderState.Error -> {
                    val error = (paymentOrderState as PaymentOrderState.Error).message
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Setup Failed", color = MaterialTheme.colorScheme.error)
                        Text(error, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.resetPaymentOrderState()
                            viewModel.createPaymentOrder(token, amount, orderId)
                        }) {
                            Text("Retry Setup")
                        }
                    }
                }

                else -> {
                    Text("Initializing payment...")
                }
            }
        }
    }
}