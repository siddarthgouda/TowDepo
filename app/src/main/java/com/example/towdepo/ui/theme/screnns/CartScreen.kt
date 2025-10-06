package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.towdepo.api.CartApiService
import com.example.towdepo.data.CartItem
import com.example.towdepo.repository.CartRepository
import com.example.towdepo.security.TokenManager
import com.example.towdepo.viewmodels.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    tokenManager: TokenManager,
    apiService: CartApiService
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
            TopAppBar(
                title = {
                    Text(
                        "Shopping Cart ${
                            if (cartItems.isNotEmpty()) "(${cartViewModel.getTotalItems()} items)"
                            else ""
                        }"
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
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty() && !isLoading) {
                CartBottomBar(
                    totalAmount = cartViewModel.calculateTotal(),
                    onCheckout = {
                        println("🛒 DEBUG: Proceeding to checkout")
                    }
                )
            }
        }
    ) { paddingValues ->
        // FIX: Add proper padding to avoid cutting
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
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Amount",
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

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .height(48.dp)
                    .width(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
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
            .padding(paddingValues) // Apply the corrected padding
    ) {
        when {
            isLoading && cartItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            cartItems.isEmpty() -> {
                EmptyCartState()
            }
            else -> {
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
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp) // Add some vertical padding
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
            // Check if it's already a full URL or needs base URL
            if (cartItem.productImage!!.startsWith("http")) {
                cartItem.productImage
            } else {
                // Remove leading slash if present and construct full URL
                val cleanPath = cartItem.productImage!!.removePrefix("/")
                "http://10.0.2.2:3501/uploads/product/$cleanPath"
            }
        } else {
            null
        }
    }.also { url ->
        println("🛒 DEBUG: Image URL for ${cartItem.title}: $url")
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove Item") },
            text = { Text("Are you sure you want to remove ${cartItem.title} from your cart?") },
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
            .padding(horizontal = 0.dp, vertical = 4.dp), // Reduced horizontal padding, kept vertical
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product Image - FIXED URL
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = cartItem.title,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp)
                )
            } else {
                // Fallback placeholder if no image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = "No Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // FIX: Show proper brand name instead of ID
                Text(
                    text = "Brand: ${getBrandDisplayName(cartItem.brand)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                val discountedPrice = calculateDiscountedPrice(cartItem.mrp, cartItem.discount)
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
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text(
                        text = "${cartItem.discount}% OFF",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Increase button
                IconButton(
                    onClick = {
                        if (cartItemId.isNotEmpty()) {
                            println("🛒 DEBUG: Increase button clicked for ID: $cartItemId, current count: ${cartItem.count}")
                            onUpdateQuantity(cartItemId, cartItem.count + 1)
                        } else {
                            println("🛒 DEBUG: Cannot increase - empty cart item ID")
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }

                Text(text = cartItem.count.toString())

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
                    enabled = cartItem.count > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
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
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                }
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

@Composable
fun EmptyCartState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.ShoppingCart,
                contentDescription = "Empty Cart",
                modifier = Modifier.size(120.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your cart is empty",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add some items to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

private fun calculateDiscountedPrice(mrp: Double, discount: String): String {
    val discountValue = discount.toDoubleOrNull() ?: 0.0
    val discountedPrice = mrp - (mrp * discountValue / 100)
    return String.format("%.2f", discountedPrice)
}