package com.example.towdepo.ui.theme.screnns

// ui/screens/AuthScreen.kt
import androidx.compose.runtime.*
import com.example.towdepo.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var isLoginScreen by remember { mutableStateOf(true) }

    if (isLoginScreen) {
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = { isLoginScreen = false }
        )
    } else {
        RegisterScreen(
            viewModel = authViewModel,
            onRegisterSuccess = onLoginSuccess,
            onNavigateToLogin = { isLoginScreen = true }
        )
    }
}