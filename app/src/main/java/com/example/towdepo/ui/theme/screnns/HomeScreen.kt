package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.towdepo.R
import com.example.towdepo.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = remember { DrawerState(initialValue = DrawerValue.Closed) }
    val scope = rememberCoroutineScope()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            NavigationDrawerContent(
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                onNavigateToProducts = {
                    scope.launch {
                        drawerState.close()
                    }
                    onNavigateToProducts()
                },
                onNavigateToCart = {
                    scope.launch {
                        drawerState.close()
                    }
                    onNavigateToCart()
                },
                onNavigateToProfile = {
                    scope.launch {
                        drawerState.close()
                    }
                    onNavigateToProfile()
                },
                onNavigateToSettings = {
                    scope.launch {
                        drawerState.close()
                    }
                    onNavigateToSettings()
                },
                onNavigateToWishlist = {
                    if (isLoggedIn) {
                        navController.navigate("wishlist")
                    } else {
                        navController.navigate("login?returnTo=wishlist")
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                    }
                    onLogout()
                }
            )
        }
    )
    {
        Scaffold(
            topBar = {
                HomeTopAppBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            floatingActionButton = {
                // Optional: Add a quick action button
                FloatingActionButton(
                    onClick = { onNavigateToProducts() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Shop")
                }
            }
        ) { paddingValues ->
            HomeContent(
                modifier = Modifier.padding(paddingValues),
                onNavigateToProducts = onNavigateToProducts
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onMenuClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.towdepo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White // White icon for better visibility
                )
            }
        },
        modifier = Modifier.height(60.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFFFBA6D), // Orange color
            titleContentColor = Color.White, // White title/content
            navigationIconContentColor = Color.White // White navigation icon
        )
    )
}

@Composable
fun NavigationDrawerContent(
    onCloseDrawer: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Drawer Header
        DrawerHeader()

        // Drawer Items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(getDrawerItems()) { item ->
                NavigationDrawerItem(
                    item = item,
                    onClick = {
                        when (item.route) {
                            "products" -> onNavigateToProducts()
                            "cart" -> onNavigateToCart()
                            "profile" -> onNavigateToProfile()
                            "settings" -> onNavigateToSettings()
                            "wishlist" -> onNavigateToWishlist()
                            "logout" -> onLogout()
                        }
                    }
                )
            }
        }

        // App Version
        DrawerFooter()
    }
}

@Composable
fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Your App Logo Image
        Image(
            painter = painterResource(id = R.drawable.towdepo), // Use your logo image
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp) // Adjust size as needed
        )

    }
}

@Composable
fun NavigationDrawerItem(
    item: DrawerItem,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        selected = false,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title
            )
        },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = MaterialTheme.colorScheme.surface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
fun DrawerFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "TowDepo © 2024",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    onNavigateToProducts: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8D086), // Top - Full Orange
                        Color(0xFFFFFFFF), // Middle - Light Orange
                        Color(0xFFFFFFFF)  // Bottom - Full Light/White
                    )
                )
            )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))
            // Welcome Section
            WelcomeSection()

            Spacer(modifier = Modifier.height(2.dp))

            // Posters Section (replaced QuickActionsGrid)
            QuickActionsGrid(onNavigateToProducts = onNavigateToProducts)

            Spacer(modifier = Modifier.height(8.dp))

            // Featured Products Preview
            FeaturedProductsPreview()

            Spacer(modifier = Modifier.height(16.dp))

            // New Info-Image Section
            InfoImageSection()
        }
    }
}
@Composable
fun WelcomeSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically

    ) {
        // Add your images here
        Image(
            painter = painterResource(id = R.drawable.group1),
            contentDescription = "Image 1",
            modifier = Modifier.size(120.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.group2),
            contentDescription = "Image 2",
            modifier = Modifier.size(100.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.group3),
            contentDescription = "Image 3",
            modifier = Modifier.size(125.dp)
        )
        // Add more images as needed
    }
}
@Composable
fun QuickActionsGrid(
    onNavigateToProducts: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        // Poster 1
        Box(modifier = Modifier.width(350.dp)) {
            Image(
                painter = painterResource(id = R.drawable.poster1),
                contentDescription = "Poster 1",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Poster 2
        Box(modifier = Modifier.width(350.dp)) {
            Image(
                painter = painterResource(id = R.drawable.poster2),
                contentDescription = "Poster 2",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}


@Composable
fun FeaturedProductsPreview() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Featured Products",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scrollable product boxes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product 1 - Custom Safety T-Shirt
            ProductBox(
                imageRes = R.drawable.safety_tshirt, // Replace with your image
                title = "Custom Safety T-Shirt",
                modifier = Modifier.width(160.dp),
                onClick = { /* Configure later */ }
            )

            // Product 2 - Affordable Truck Tires
            ProductBox(
                imageRes = R.drawable.truck_tires, // Replace with your image
                title = "Affordable Truck Tires",
                modifier = Modifier.width(160.dp),
                onClick = { /* Configure later */ }
            )

            // Product 3 - Custom Safety Jackets
            ProductBox(
                imageRes = R.drawable.safety_jackets, // Replace with your image
                title = "Custom Safety Jackets",
                modifier = Modifier.width(160.dp),
                onClick = { /* Configure later */ }
            )
        }
    }
}

@Composable
fun ProductBox(
    imageRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun InfoImageSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // First Row - Image Left, Info Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image on Left
            Image(
                painter = painterResource(id = R.drawable.image_left), // Replace with your image
                contentDescription = "Left Image",
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info on Right
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Safety shirts and safety jackets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Personal style in fashion is more than just what you wear—it's a visual manifestation of your personality. It's the art of curating outfits that resonate with your inner essence.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Second Row - Info Left, Image Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info on Left
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Truck Tyres",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "With customization options available directly at TOWDEPO,outfitting the team with personalized apparel has never been easier.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Image on Right
            Image(
                painter = painterResource(id = R.drawable.image_right), // Replace with your image
                contentDescription = "Right Image",
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
            )
        }
    }
}
// Data classes for drawer items
data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

fun getDrawerItems(): List<DrawerItem> {
    return listOf(
        DrawerItem("Home", Icons.Default.Home, "home"),
        DrawerItem("Products", Icons.Default.ShoppingCart, "products"),
        DrawerItem("My Cart", Icons.Default.ShoppingBasket, "cart"),
        DrawerItem("My Wishlist", Icons.Default.Favorite, "wishlist"),
        DrawerItem("My Profile", Icons.Default.Person, "profile"),
        DrawerItem("Settings", Icons.Default.Settings, "settings"),
        DrawerItem("Help & Support", Icons.Default.Help, "help"),
        DrawerItem("Logout", Icons.Default.ExitToApp, "logout")
    )
}