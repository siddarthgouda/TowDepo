package com.example.towdepo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.towdepo.ui.theme.screnns.AuthScreen
import com.example.towdepo.ui.theme.screnns.HomeScreen
import com.example.towdepo.ui.theme.screnns.SplashScreen
import com.example.towdepo.viewmodels.AuthViewModel


@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                authViewModel = authViewModel,
                onLoggedIn = { navController.navigate("home") { popUpTo("splash") { inclusive = true } } },
                onLoggedOut = { navController.navigate("auth") { popUpTo("splash") { inclusive = true } } }
            )
        }
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { navController.navigate("home") { popUpTo("auth") { inclusive = true } } }
            )
        }
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    // Navigate to profile screen
                    navController.navigate("profile")
                }
            )
        }
    }
}