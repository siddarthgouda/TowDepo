package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.towdepo.di.AppContainer
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit
) {
    val productViewModel: ProductViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
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
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
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
                ProductActionBar(product = currentProduct)
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
            val imageUrl = buildImageUrl(image.src)
            println("Full URL $index: $imageUrl")
        }
        println("============================")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Product Images Gallery - UPDATED PART
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
                val imageUrl = buildImageUrl(firstImage.src)

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

        // Product Info Card - REST REMAINS SAME
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

// Helper function to build image URL
private fun buildImageUrl(imageSrc: String): String {
    return when {
        imageSrc.startsWith("http") -> imageSrc
        imageSrc.startsWith("/") -> "http://10.0.2.2:3501$imageSrc"
        else -> "http://10.0.2.2:3501/uploads/product/$imageSrc"
    }
}

// Enhanced Image Loader with error handling
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
fun ProductActionBar(product: com.example.towdepo.data.ApiProduct) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wishlist Button
            OutlinedButton(
                onClick = { /* Add to wishlist */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Text("Wishlist")
            }

            // Add to Cart Button
            Button(
                onClick = { /* Add to cart */ },
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