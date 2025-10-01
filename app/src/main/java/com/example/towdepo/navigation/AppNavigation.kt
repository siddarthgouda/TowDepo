package com.example.towdepo.navigation

import com.example.towdepo.ui.theme.screnns.ProductsScreen



import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.towdepo.ui.theme.screnns.AuthScreen
import com.example.towdepo.ui.theme.screnns.ProductDetailScreen
import com.example.towdepo.ui.theme.screnns.SplashScreen

import com.example.towdepo.viewmodels.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                authViewModel = authViewModel,
                onLoggedIn = {
                    navController.navigate("products") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onLoggedOut = {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("products") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("products") {
            ProductsScreen( // Using ProductsScreen here
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                }
            )
        }
        composable("product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}