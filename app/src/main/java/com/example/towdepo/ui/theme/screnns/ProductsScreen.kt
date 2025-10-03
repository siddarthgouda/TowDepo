package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.towdepo.di.AppContainer
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onProductClick: (String) -> Unit = {}
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

    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = if (searchQuery.isBlank()) {
        products
    } else {
        products.filter { product ->
            product.title.contains(searchQuery, ignoreCase = true) ||
                    product.category.name.contains(searchQuery, ignoreCase = true) ||
                    product.SKU.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        if (products.isEmpty()) {
            productViewModel.loadProducts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Our Products",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (!loading) {
                        IconButton(
                            onClick = { productViewModel.loadProducts() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search products...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // Products Count
            Text(
                text = "${filteredProducts.size} products found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

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
                            "Loading Products...",
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
                            Icons.Default.Refresh,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Oops! Something went wrong",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                filteredProducts.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "No products",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No products found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isNotEmpty()) "Try adjusting your search" else "Check back later for new products",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: com.example.towdepo.data.ApiProduct,
    onProductClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { onProductClick(product.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (product.images.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "http://10.0.2.2:3501/uploads/product/${product.images.first().src}"
                            ),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "No image",
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Product Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$${product.mrp}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (product.discount?.isNotEmpty() == true && product.discount != "0") {
                            Text(
                                text = "$${product.discountedPrice}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Additional Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (product.inStock) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (product.inStock) "In Stock" else "Out of Stock",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (product.inStock) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                // SKU
                Text(
                    text = "SKU: ${product.SKU}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Discount Badge
            if (product.discount?.isNotEmpty() == true && product.discount != "0") {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${product.discount}% OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}