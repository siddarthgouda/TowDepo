package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.towdepo.di.AppContainer
import com.example.towdepo.repository.ProductRepository
import com.example.towdepo.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit
) {
    // Create ViewModel with custom factory
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

    // Find the specific product by ID
    val currentProduct = products.find { it.id == productId }

    // Load products if not loaded
    LaunchedEffect(productId) {
        if (products.isEmpty()) {
            productViewModel.loadProducts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading product details...")
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { productViewModel.loadProducts() }) {
                            Text("Try Again")
                        }
                    }
                }

                currentProduct == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Product not found")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = product.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Basic Info
        Text("Basic Information", style = MaterialTheme.typography.headlineSmall)
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow("Price", "$${product.mrp}")
        InfoRow("Category", product.category.name)
        InfoRow("In Stock", if (product.inStock) "Yes" else "No")
        InfoRow("SKU", product.SKU)
        product.description?.let { InfoRow("Description", it) }
        product.discount?.takeIf { it != "0" }?.let {
            InfoRow("Discount", "$it%")
            InfoRow("Discounted Price", "$${product.discountedPrice}")
        }

        // Images
        if (product.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Images", style = MaterialTheme.typography.headlineSmall)
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            product.images.forEach { image ->
                Text("• ${image.src}")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text("$label:", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
        Text(value)
    }
}