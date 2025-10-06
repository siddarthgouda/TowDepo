package com.example.towdepo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.towdepo.MainApplication
import com.example.towdepo.di.AppContainer
import com.example.towdepo.ui.theme.screnns.CartScreen
import com.example.towdepo.ui.theme.screnns.LoginScreen
import com.example.towdepo.ui.theme.screnns.ProductDetailScreen
import com.example.towdepo.ui.theme.screnns.ProductsScreen
import com.example.towdepo.ui.theme.screnns.RegisterScreen
import com.example.towdepo.ui.theme.screnns.SplashScreen
import com.example.towdepo.viewmodels.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Get TokenManager from Application context
    val context = LocalContext.current
    val tokenManager = remember {
        (context.applicationContext as MainApplication).tokenManager
    }

    // Get CartApiService from AppContainer
    val cartApiService = remember {
        AppContainer.cartApiService
    }

    println("🔐 DEBUG AppNavigation: Got TokenManager: $tokenManager")
    println("🔐 DEBUG AppNavigation: Got CartApiService: $cartApiService")

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Splash Screen
        composable("splash") {
            SplashScreen(
                authViewModel = authViewModel,
                onLoggedIn = {
                    navController.navigate("productList") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onLoggedOut = {
                    navController.navigate("productList") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Product List Screen
        composable("productList") {
            ProductsScreen(
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                }
            )
        }

        // Product Detail Screen
        composable("product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onLoginRequired = {
                    navController.navigate("login?returnTo=product/$productId")
                },
                onNavigateToCart = {
                    navController.navigate("cart")
                },
                authViewModel = authViewModel
            )
        }

        // Login Screen
        composable("login") { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo") ?: "productList"

            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(returnTo) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("signup?returnTo=$returnTo")
                }
            )
        }

        // Signup Screen
        composable("signup") { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo") ?: "productList"

            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(returnTo) {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login?returnTo=$returnTo") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // Cart Screen
        composable("cart") {
            if (isLoggedIn) {
                println("🔐 DEBUG AppNavigation: User is logged in, showing CartScreen")

                CartScreen(
                    navController = navController,
                    tokenManager = tokenManager,
                    apiService = cartApiService
                )
            } else {
                println("🔐 DEBUG AppNavigation: User not logged in, redirecting to login")
                LaunchedEffect(Unit) {
                    navController.navigate("login?returnTo=cart") {
                        popUpTo("cart") { inclusive = true }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}