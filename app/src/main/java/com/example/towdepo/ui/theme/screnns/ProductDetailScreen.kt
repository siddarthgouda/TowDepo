package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.outlinedButtonBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.towdepo.di.AppContainer
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.utils.ImageUtils
import com.example.towdepo.utils.WishlistViewModelFactory
import com.example.towdepo.viewmodels.AuthViewModel
import com.example.towdepo.viewmodels.CartViewModel
import com.example.towdepo.viewmodels.ProductViewModel
import com.example.towdepo.viewmodels.WishlistViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onLoginRequired: () -> Unit,
    onNavigateToCart: () -> Unit,
    onWishlistClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    // Get login state from the shared AuthViewModel
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Debug logging
    LaunchedEffect(Unit) {
        println("🔍 DEBUG ProductDetailScreen: Screen launched with productId: $productId")
        println("🔍 DEBUG ProductDetailScreen: Login state = $isLoggedIn")
    }

    val productViewModel: ProductViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = ProductRepository(AppContainer.productApiService)
                return ProductViewModel(repository) as T
            }
        }
    )

    val products by productViewModel.products.collectAsState()
    val loading by productViewModel.loading.collectAsState()
    val error by productViewModel.error.collectAsState()

    val currentProduct = products.find { it.id == productId }

    LaunchedEffect(productId) {
        if (products.isEmpty()) {
            productViewModel.loadProducts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Product Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            println("🔍 DEBUG: Back button clicked in ProductDetailScreen")
                            onBackClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Cart Icon
                    IconButton(
                        onClick = {
                            if (isLoggedIn) {
                                onNavigateToCart()
                            } else {
                                onLoginRequired()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share Icon
                    IconButton(
                        onClick = { /* Share product */ },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (currentProduct != null) {
                ProductActionBar(
                    product = currentProduct,
                    onLoginRequired = onLoginRequired,
                    isLoggedIn = isLoggedIn,
                    onWishlistClick = onWishlistClick
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading Product Details...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Failed to load product",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { productViewModel.loadProducts() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Try Again", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Go Back", fontSize = 16.sp)
                        }
                    }
                }

                currentProduct == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Not found",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Product not found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The product you're looking for doesn't exist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Go Back", fontSize = 16.sp)
                        }
                    }
                }

                else -> {
                    ProductDetailContent(product = currentProduct)
                }
            }
        }
    }
}

@Composable
fun ProductDetailContent(product: com.example.towdepo.data.ApiProduct) {
    // Debug: Print image information
    LaunchedEffect(product) {
        println("=== PRODUCT IMAGES DEBUG ===")
        println("Product ID: ${product.id}")
        println("Product Title: ${product.title}")
        println("Number of images: ${product.images.size}")
        product.images.forEachIndexed { index, image ->
            println("Image $index: ${image.src}")
            val imageUrl = ImageUtils.getProductImageUrl(image.src)
            println("Full URL $index: $imageUrl")
        }
        println("============================")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Product Images Gallery
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            if (product.images.isNotEmpty()) {
                val firstImage = product.images.first()
                val imageUrl = ImageUtils.getProductImageUrl(firstImage.src)

                ProductImageLoader(
                    imageUrl = imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "No Image Available",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Image Available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Product Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title and Category
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "$${product.mrp}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (product.discount?.isNotEmpty() == true && product.discount != "0") {
                        Text(
                            text = "$${product.discountedPrice}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Discount Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${product.discount}% OFF",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stock Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stock Status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (product.inStock) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (product.inStock) "✅ In Stock" else "❌ Out of Stock",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (product.inStock) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    // SKU
                    Text(
                        text = "SKU: ${product.SKU}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                if (!product.description.isNullOrEmpty()) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Additional Images
                if (product.images.size > 1) {
                    Text(
                        text = "Gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${product.images.size - 1} more images available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun buildImageUrl(imageSrc: String): String {
    return ImageUtils.getProductImageUrl(imageSrc)
}

@Composable
fun ProductImageLoader(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (!hasError) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    onError = {
                        isLoading = false
                        hasError = true
                        println("❌ Failed to load image: $imageUrl")
                    },
                    onSuccess = {
                        isLoading = false
                        println("✅ Successfully loaded image: $imageUrl")
                    }
                ),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (hasError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Failed to load image",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Image not available",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProductActionBar(
    product: com.example.towdepo.data.ApiProduct,
    onLoginRequired: () -> Unit,
    isLoggedIn: Boolean,
    onWishlistClick: () -> Unit,
    wishlistViewModel: WishlistViewModel = viewModel(factory = WishlistViewModelFactory(AppContainer)),
    cartViewModel: CartViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CartViewModel(AppContainer.cartRepository) as T
            }
        }
    )
) {
    var showAddedMessage by remember { mutableStateOf(false) }
    var showWishlistMessage by remember { mutableStateOf(false) }

    // Track wishlist state
    val wishlistItems by wishlistViewModel.wishlistItems.collectAsState()
    val isInWishlist = remember(wishlistItems, product.id) {
        wishlistItems.any { it.product?.id == product.id }
    }

    LaunchedEffect(showAddedMessage) {
        if (showAddedMessage) {
            delay(2000)
            showAddedMessage = false
        }
    }

    LaunchedEffect(showWishlistMessage) {
        if (showWishlistMessage) {
            delay(2000)
            showWishlistMessage = false
        }
    }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            // Success Messages
            if (showAddedMessage) {
                SuccessMessage(" Added to cart!")
            }
            if (showWishlistMessage) {
                SuccessMessage(
                    if (isInWishlist) " Added to wishlist!"
                    else " Removed from wishlist!"
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wishlist Button - IMPROVED
                OutlinedButton(
                    onClick = {
                        println("🎯 DEBUG: Wishlist button clicked for: ${product.id}")
                        println("🎯 DEBUG: Is in wishlist: $isInWishlist")

                        if (isLoggedIn) {
                            if (isInWishlist) {
                                // Remove from wishlist
                                val wishlistItem = wishlistItems.find { it.product?.id == product.id }
                                wishlistItem?.let {
                                    println("🎯 DEBUG: Removing item: ${it.id}")
                                    wishlistViewModel.removeFromWishlist(it.id?.oid ?: "")
                                }
                            } else {
                                // Add to wishlist
                                println("🎯 DEBUG: Adding product to wishlist")
                                wishlistViewModel.addToWishlist(
                                    title = product.title,
                                    productId = product.id,
                                    mrp = product.mrp,
                                    discount = product.discount ?: "0",
                                    brand = product.brand?.name ?: "Unknown Brand",
                                    image = product.images.firstOrNull()?.src ?: ""
                                )
                            }
                            showWishlistMessage = true
                        } else {
                            onLoginRequired()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    val (icon, text) = if (isInWishlist) {
                        Icons.Default.Favorite to "In Wishlist"
                    } else {
                        Icons.Default.FavoriteBorder to "Wishlist"
                    }

                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text)
                }

                // Add to Cart Button
                Button(
                    onClick = {
                        if (isLoggedIn) {
                            cartViewModel.addToCart(product)
                            showAddedMessage = true
                        } else {
                            onLoginRequired()
                        }
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = product.inStock
                ) {
                    Text(
                        text = if (product.inStock) "Add to Cart" else "Out of Stock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}