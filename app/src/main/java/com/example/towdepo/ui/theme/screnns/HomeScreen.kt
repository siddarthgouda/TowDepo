package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.towdepo.R
import com.example.towdepo.data.DrawerItem
import com.example.towdepo.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.Unit

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
    ) {
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
                FloatingActionButton(
                    onClick = { onNavigateToProducts() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.shadow(8.dp, shape = CircleShape)
                ) {
                    Icon(Icons.Default.ShoppingBasket, contentDescription = "Shop")
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
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF6B35),
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White
        ),
        modifier = Modifier
            .height(70.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
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
        DrawerHeader()

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

        DrawerFooter()
    }
}

@Composable
fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.towdepo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(180.dp)
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
            "TowDepo © 2025",
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
                        Color(0xFFFFF8F0),
                        Color(0xFFFFFFFF),
                        Color(0xFFF5F5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            WelcomeSection()
            QuickActionsGrid(onNavigateToProducts = onNavigateToProducts)
            FeaturedProductsPreview(onNavigateToProducts)
            InfoImageSection()
        }
    }
}

@Composable
fun WelcomeSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.group1),
                contentDescription = "Image 1",
                modifier = Modifier
                    .size(100.dp)
                    .shadow(4.dp, shape = CircleShape)
                    .clip(CircleShape)
            )

            Image(
                painter = painterResource(id = R.drawable.group2),
                contentDescription = "Image 2",
                modifier = Modifier
                    .size(90.dp)
                    .shadow(4.dp, shape = CircleShape)
                    .clip(CircleShape)
            )

            Image(
                painter = painterResource(id = R.drawable.group3),
                contentDescription = "Image 3",
                modifier = Modifier
                    .size(100.dp)
                    .shadow(4.dp, shape = CircleShape)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onNavigateToProducts: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Special Offers",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PosterCard(
                imageRes = R.drawable.poster1,
                onClick = onNavigateToProducts
            )

            PosterCard(
                imageRes = R.drawable.poster2,
                onClick = onNavigateToProducts
            )
        }
    }
}

@Composable
fun PosterCard(
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun FeaturedProductsPreview(
    onNavigateToProducts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Featured Products",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )


            TextButton(onClick = { onNavigateToProducts() }) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ProductCard(
                imageRes = R.drawable.safety_tshirt,
                title = "Custom Safety T-Shirt",
                onClick = { onNavigateToProducts() }
            )

            ProductCard(
                imageRes = R.drawable.truck_tires,
                title = "Truck Tires",
                onClick = { onNavigateToProducts() }
            )

            ProductCard(
                imageRes = R.drawable.safety_jackets,
                title = "Safety Jackets",
                onClick = { onNavigateToProducts() }
            )
        }
    }
}

@Composable
fun ProductCard(
    imageRes: Int,
    title: String,

    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(220.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

            }
        }
    }
}

@Composable
fun InfoImageSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InfoCard(
            imageRes = R.drawable.image_left,
            title = "Safety Shirts & Jackets",
            description = "Personal style in fashion is more than just what you wear—it's a visual manifestation of your personality. It's the art of curating outfits that resonate with your inner essence.",
            imageOnLeft = true
        )

        InfoCard(
            imageRes = R.drawable.image_right,
            title = "Truck Tyres",
            description = "With customization options available directly at TOWDEPO, outfitting the team with personalized apparel has never been easier.",
            imageOnLeft = false
        )
    }
}

@Composable
fun InfoCard(
    imageRes: Int,
    title: String,
    description: String,
    imageOnLeft: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (imageOnLeft) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


fun getDrawerItems(): List<DrawerItem> {
    return listOf(
        DrawerItem("Home", Icons.Default.Home, "home"),
        DrawerItem("Products", Icons.Default.ShoppingBasket, "products"),
        DrawerItem("My Cart", Icons.Default.ShoppingCart, "cart"),
        DrawerItem("My Wishlist", Icons.Default.Favorite, "wishlist"),
        DrawerItem("My Profile", Icons.Default.Person, "profile"),
        DrawerItem("Settings", Icons.Default.Settings, "settings"),
        DrawerItem("Help & Support", Icons.AutoMirrored.Filled.Help, "help"),
        DrawerItem("Logout", Icons.AutoMirrored.Filled.ExitToApp, "logout")
    )
}