package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.towdepo.api.CartApiService
import com.example.towdepo.data.CartItem
import com.example.towdepo.di.AppConfig
import com.example.towdepo.repository.CartRepository
import com.example.towdepo.security.TokenManager
import com.example.towdepo.utils.ImageUtils
import com.example.towdepo.viewmodels.CartViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    tokenManager: TokenManager,
    apiService: CartApiService,
    userId: String
) {
    println("🛒 DEBUG: CartScreen composable called")

    // Create ViewModel with proper coroutine scope to avoid ANR
    val cartViewModel: CartViewModel = remember {
        println("🛒 DEBUG: Creating CartRepository and CartViewModel")
        val cartRepository = CartRepository(apiService, tokenManager)
        CartViewModel(cartRepository)
    }

    // Collect StateFlow values with proper error handling
    val cartItems by cartViewModel.cartItems.collectAsState()
    val isLoading by cartViewModel.isLoading.collectAsState()
    val errorMessage by cartViewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if any
    if (errorMessage != null && errorMessage!!.isNotEmpty()) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage!!)
            cartViewModel.clearError()
        }
    }

    // Load cart items only once when screen is first shown
    LaunchedEffect(Unit) {
        println("🛒 DEBUG: CartScreen LaunchedEffect - loading cart items")
        cartViewModel.loadCartItems()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Shopping Cart",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            println("🛒 DEBUG: Back button clicked")
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty() && !isLoading) {
                CartBottomBar(
                    totalAmount = cartViewModel.calculateTotal(),
                    totalItems = cartViewModel.getTotalItems(),
                    onCheckout = {
                        println("🛒 DEBUG: Proceeding to checkout")
                        navController.navigate("checkout")
                    }
                )
            }
        }
    ) { paddingValues ->
        CartContent(
            cartItems = cartItems,
            isLoading = isLoading,
            cartViewModel = cartViewModel,
            paddingValues = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            )
        )
    }
}

@Composable
fun CartBottomBar(
    totalAmount: Double,
    totalItems: Int,
    onCheckout: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    // Move LaunchedEffect to composable scope
    if (isLoading) {
        LaunchedEffect(Unit) {
            // Simulate brief processing for better UX
            delay(500)
            onCheckout()
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total ($totalItems items)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Enhanced Checkout button
                Button(
                    onClick = {
                        isLoading = true
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .width(140.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Processing...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "Checkout",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartContent(
    cartItems: List<CartItem>,
    isLoading: Boolean,
    cartViewModel: CartViewModel,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            isLoading && cartItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Loading your cart...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            cartItems.isEmpty() -> {
                EmptyCartState()
            }
            else -> {
                // Cart header
                Text(
                    text = "Your Items (${cartViewModel.getTotalItems()})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                CartItemList(
                    cartItems = cartItems,
                    onUpdateQuantity = { cartItemId, newQuantity ->
                        println("🛒 DEBUG: CartScreen - Updating quantity for: $cartItemId to $newQuantity")
                        if (newQuantity > 0) {
                            cartViewModel.updateCartItem(cartItemId, newQuantity)
                        } else {
                            cartViewModel.deleteCartItem(cartItemId)
                        }
                    },
                    onRemoveItem = { cartItemId ->
                        println("🛒 DEBUG: CartScreen - Deleting item: $cartItemId")
                        cartViewModel.deleteCartItem(cartItemId)
                    }
                )
            }
        }
    }
}

@Composable
fun CartItemList(
    cartItems: List<CartItem>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(cartItems) { item ->
            CartItemCard(
                cartItem = item,
                onUpdateQuantity = onUpdateQuantity,
                onRemoveItem = onRemoveItem
            )
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Safely get the cart item ID with debug logging
    val cartItemId = remember(cartItem) {
        val id = cartItem.safeId
        println("🛒 DEBUG CartItemCard: Cart item '${cartItem.title}' ID: '$id'")
        id
    }

    // FIXED: Proper image URL construction
    val imageUrl = remember(cartItem) {
        if (!cartItem.productImage.isNullOrEmpty()) {
            // Use the safe image URL builder that handles AWS vs local environments
            ImageUtils.getSafeProductImageUrl(cartItem.productImage)
        } else {
            null
        }
    }.also { url ->
        println("🛒 DEBUG: Image URL for ${cartItem.title}: $url")
        println("🛒 DEBUG: Using environment: ${AppConfig.getEnvironmentInfo()}")
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    "Remove Item",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Are you sure you want to remove \"${cartItem.title}\" from your cart?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        if (cartItemId.isNotEmpty()) {
                            println("🛒 DEBUG: Remove button clicked for ID: $cartItemId")
                            onRemoveItem(cartItemId)
                        } else {
                            println("🛒 DEBUG: Cannot remove - empty cart item ID")
                        }
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image

            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = cartItem.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                // ✅ Use placeholder from ImageUtils
                AsyncImage(
                    model = ImageUtils.getPlaceholderImageUrl(),
                    contentDescription = "No Image Available",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }


            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cartItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Brand: ${getBrandDisplayName(cartItem.brand)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val discountedPrice = calculateDiscountedPrice(cartItem.mrp, cartItem.discount)

                // Price row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "₹$discountedPrice",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (cartItem.discount.isNotEmpty() && cartItem.discount != "0") {
                        Text(
                            text = "₹${cartItem.mrp}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${cartItem.discount}% OFF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Quantity controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quantity display with background
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cartItem.count.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Control buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrease button
                    IconButton(
                        onClick = {
                            if (cartItemId.isNotEmpty() && cartItem.count > 1) {
                                println("🛒 DEBUG: Decrease button clicked for ID: $cartItemId, current count: ${cartItem.count}")
                                onUpdateQuantity(cartItemId, cartItem.count - 1)
                            } else {
                                println("🛒 DEBUG: Cannot decrease - empty cart item ID or count too low")
                            }
                        },
                        modifier = Modifier.size(36.dp),
                        enabled = cartItem.count > 1
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = if (cartItem.count > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Increase button
                    IconButton(
                        onClick = {
                            if (cartItemId.isNotEmpty()) {
                                println("🛒 DEBUG: Increase button clicked for ID: $cartItemId, current count: ${cartItem.count}")
                                onUpdateQuantity(cartItemId, cartItem.count + 1)
                            } else {
                                println("🛒 DEBUG: Cannot increase - empty cart item ID")
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = {
                        if (cartItemId.isNotEmpty()) {
                            println("🛒 DEBUG: Delete button clicked for ID: $cartItemId")
                            showDeleteConfirmation = true
                        } else {
                            println("🛒 DEBUG: Cannot delete - empty cart item ID")
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCartState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                Icons.Outlined.ShoppingCart,
                contentDescription = "Empty Cart",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Your cart is empty",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Add some items to get started",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { /* Navigate to products */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "Browse Products",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// Helper function to get proper brand display name
private fun getBrandDisplayName(brand: String): String {
    return when {
        brand == "default_brand_id" -> "N/A"
        brand.contains("default") -> "N/A"
        brand.isNotEmpty() -> brand
        else -> "N/A"
    }
}

private fun calculateDiscountedPrice(mrp: Double, discount: String): String {
    val discountValue = discount.toDoubleOrNull() ?: 0.0
    val discountedPrice = mrp - (mrp * discountValue / 100)
    return String.format("%.2f", discountedPrice)
}