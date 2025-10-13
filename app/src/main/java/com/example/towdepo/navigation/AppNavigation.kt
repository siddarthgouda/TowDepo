package com.example.towdepo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.towdepo.MainApplication
import com.example.towdepo.di.AppContainer
import com.example.towdepo.ui.theme.screnns.CartScreen
import com.example.towdepo.ui.theme.screnns.CheckoutScreen
import com.example.towdepo.ui.theme.screnns.FeaturedProductsPreview
import com.example.towdepo.ui.theme.screnns.HomeScreen
import com.example.towdepo.ui.theme.screnns.LoginScreen

import com.example.towdepo.ui.theme.screnns.PaymentScreen
import com.example.towdepo.ui.theme.screnns.ProductDetailScreen
import com.example.towdepo.ui.theme.screnns.ProductsScreen
import com.example.towdepo.ui.theme.screnns.RegisterScreen
import com.example.towdepo.ui.theme.screnns.WishlistScreen
import com.example.towdepo.viewmodels.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val context = LocalContext.current
    val tokenManager = remember {
        (context.applicationContext as MainApplication).tokenManager
    }

    val cartApiService = remember {
        AppContainer.cartApiService
    }

    // Get the actual user ID from auth viewmodel
    val currentUserId = remember(currentUser) {
        currentUser?.id ?: tokenManager.getUserId() ?: ""
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home Screen
        composable("home") {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                onNavigateToProducts = {
                    navController.navigate("productList")
                },
                onNavigateToCart = {
                    if (isLoggedIn) {
                        navController.navigate("cart")
                    } else {
                        navController.navigate("login?returnTo=cart")
                    }
                },
                onNavigateToProfile = {
                    if (isLoggedIn) {
                        navController.navigate("profile")
                    } else {
                        navController.navigate("login?returnTo=profile")
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToWishlist = {
                    if (isLoggedIn) {
                        navController.navigate("wishlist")
                    } else {
                        navController.navigate("login?returnTo=wishlist")
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }


        // Product List Screen
        composable("productList") {
            ProductsScreen(
                onProductClick = { productId ->
                    navController.navigate("product/$productId") {
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // Product Detail Screen
        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            ProductDetailScreen(
                productId = productId,
                onBackClick = {
                    navController.navigate("productList") {
                        popUpTo("productList") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onLoginRequired = {
                    navController.navigate("login?returnTo=product/$productId")
                },
                onNavigateToCart = {
                    if (isLoggedIn) {
                        navController.navigate("cart")
                    } else {
                        navController.navigate("login?returnTo=cart")
                    }
                },
                onWishlistClick = {
                    if (isLoggedIn) {
                        navController.navigate("wishlist")
                    } else {
                        navController.navigate("login?returnTo=wishlist")
                    }
                },
                authViewModel = authViewModel
            )
        }

        // Login Screen
        composable(
            route = "login?returnTo={returnTo}",
            arguments = listOf(
                navArgument("returnTo") {
                    type = NavType.StringType
                    defaultValue = "home"
                }
            )
        ) { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo") ?: "home"

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
        composable(
            route = "signup?returnTo={returnTo}",
            arguments = listOf(
                navArgument("returnTo") {
                    type = NavType.StringType
                    defaultValue = "home"
                }
            )
        ) { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo") ?: "home"

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
            if (isLoggedIn && currentUserId.isNotEmpty()) {
                CartScreen(
                    navController = navController,
                    tokenManager = tokenManager,
                    apiService = cartApiService,
                    userId = currentUserId // Use actual user ID
                )
            } else {
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
        // Add this after the Cart Screen composable
        composable("checkout") {
            if (isLoggedIn && currentUserId.isNotEmpty()) {
                // You'll need to create this CheckoutScreen or use a placeholder
                CheckoutScreen(
                    navController = navController,
                    userId = currentUserId,
                    onBackClick = { navController.popBackStack() },
                    onOrderPlaced = {
                        navController.navigate("orderConfirmation") {
                            popUpTo("checkout") { inclusive = true }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login?returnTo=checkout") {
                        popUpTo("checkout") { inclusive = true }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        // Wishlist Screen
        composable("wishlist") {
            if (isLoggedIn) {
                WishlistScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate("product/$productId")
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login?returnTo=wishlist") {
                        popUpTo("wishlist") { inclusive = true }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        // In your AppNavigation.kt, add the payment route:
        composable(
            route = "payment/{orderId}/{amount}",
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0

            PaymentScreen(
                tokenManager = tokenManager,
                orderId = orderId,
                amount = amount,
                onBackClick = { navController.popBackStack() },
                onPaymentSuccess = {
                    navController.navigate("orderConfirmation") {
                        popUpTo("payment") { inclusive = true }
                    }
                },
                onPaymentFailed = { error ->
                    // Show error and navigate back
                    navController.popBackStack()
                }
            )
        }




        // Profile Screen (Placeholder)
        composable("profile") {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Profile Screen - Coming Soon",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Settings Screen (Placeholder)
        composable("settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Settings Screen - Coming Soon",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}