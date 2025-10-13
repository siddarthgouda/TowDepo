package com.example.towdepo.ui.theme.screnns

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.towdepo.data.WishlistItem
import com.example.towdepo.utils.ImageUtils
import com.example.towdepo.utils.WishlistViewModelFactory
import com.example.towdepo.viewmodels.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    wishlistViewModel: WishlistViewModel = viewModel(
        factory = WishlistViewModelFactory(com.example.towdepo.di.AppContainer)
    )
) {
    BackHandler(onBack = onBackClick)
    val wishlistItems by wishlistViewModel.wishlistItems.collectAsState()
    val loading by wishlistViewModel.loading.collectAsState()
    val error by wishlistViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        println("🚀 DEBUG WishlistScreen: Composable launched - loading wishlist")
        wishlistViewModel.loadWishlist()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Wishlist (${wishlistItems.size})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading your wishlist...")
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { wishlistViewModel.loadWishlist() }) {
                            Text("Try Again")
                        }
                    }
                }

                wishlistItems.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = "Empty Wishlist",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your wishlist is empty",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add products you love to your wishlist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(wishlistItems, key = { it.id?.oid ?: it.product.id }) { item ->
                            WishlistItemCard(
                                item = item,
                                onProductClick = onProductClick,
                                onRemoveClick = {
                                    println("🗑️ DEBUG: Removing item: ${item.id?.oid}")
                                    item.id?.oid?.let { wishlistViewModel.removeFromWishlist(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    item: WishlistItem,
    onProductClick: (String) -> Unit,
    onRemoveClick: () -> Unit
) {

    val imageUrl = remember(item) {
        when {
            !item.image.isNullOrEmpty() -> {
                // Use ImageUtils for the main item image
                ImageUtils.getSafeProductImageUrl(item.image)
            }
            item.product.images.isNotEmpty() -> {
                // Use ImageUtils for product images
                ImageUtils.getSafeProductImageUrl(item.product.images.first().src)
            }
            else -> null
        }
    }.also { url ->
        println("️ DEBUG Wishlist: Image URL for '${item.title}': $url")
        println(" DEBUG Wishlist: Using environment: ${com.example.towdepo.di.AppConfig.getEnvironmentInfo()}")
    }

    Card(
        onClick = {
            println(" DEBUG: Clicked wishlist item: ${item.title}")
            onProductClick(item.product.id)
        },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image - Using ImageUtils
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                // ✅ Use placeholder from ImageUtils
                AsyncImage(
                    model = ImageUtils.getPlaceholderImageUrl(),
                    contentDescription = "No Image Available",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Product Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Brand: ${getWishlistBrandDisplayName(item.brand)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category
                Text(
                    text = "Category: ${item.product.category.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Discount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "₹${item.mrp}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (item.discount.isNotEmpty() && item.discount != "0") {
                        val discountInt = try {
                            item.discount.takeIf { it.isNotBlank() }?.toInt() ?: 0
                        } catch (e: NumberFormatException) {
                            0
                        }

                        if (discountInt > 0) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$discountInt% OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Stock Status
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (item.product.inStock) "✅ In Stock" else "❌ Out of Stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.product.inStock) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Remove Button
            IconButton(
                onClick = {
                    println("🗑️ DEBUG: Remove button clicked for: ${item.title}")
                    onRemoveClick()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from wishlist",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper function for brand display
private fun getWishlistBrandDisplayName(brand: String): String {
    return when {
        brand == "Unknown Brand" -> "N/A"
        brand == "default_brand_id" -> "N/A"
        brand.contains("default") -> "N/A"
        brand.isNotEmpty() -> brand
        else -> "N/A"
    }
}