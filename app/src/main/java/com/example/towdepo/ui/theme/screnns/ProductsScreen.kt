package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
fun ProductsScreen(
    onProductClick: (String) -> Unit = {}
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

    // Load products when screen is first shown
    LaunchedEffect(Unit) {
        if (products.isEmpty()) {
            productViewModel.loadProducts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products (${products.size})") },
                actions = {
                    if (!loading) {
                        IconButton(onClick = { productViewModel.loadProducts() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (error != null) {
                FloatingActionButton(
                    onClick = { productViewModel.clearError() },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text("X", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
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
                        Text("Loading products...")
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

                products.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No products available")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { productViewModel.loadProducts() }) {
                            Text("Refresh")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(products) { product ->
                            ProductItem(
                                product = product,
                                onProductClick = onProductClick
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: com.example.towdepo.data.ApiProduct,
    onProductClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onProductClick(product.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Price: $${product.mrp}")
            Text("Category: ${product.category.name}")
            Text("In Stock: ${if (product.inStock) "Yes" else "No"}")
            Text("SKU: ${product.SKU}")

            if (product.discount?.isNotEmpty() == true && product.discount != "0") {
                Text(
                    text = "Discount: ${product.discount}%",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Discounted Price: $${product.discountedPrice}",
                    color = Color.Green
                )
            }

            if (product.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Images: ${product.images.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}