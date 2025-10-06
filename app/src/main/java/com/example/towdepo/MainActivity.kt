package com.example.towdepo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import com.example.towdepo.di.AppContainer
import com.example.towdepo.navigation.AppNavigation
import com.example.towdepo.security.TokenManager
import com.example.towdepo.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val tokenManager = rememberTokenManager()
                val authViewModel: AuthViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AuthViewModel(tokenManager) as T
                        }
                    }
                )

                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun rememberTokenManager(): TokenManager {
    val context = LocalContext.current
    return remember {
        (context.applicationContext as MainApplication).tokenManager
    }
}