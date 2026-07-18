package com.foodieapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foodieapp.ui.theme.BrandOrange
import com.foodieapp.ui.theme.MyApplicationTheme

// Bottom Navigation items
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    object History : Screen("history", "History", Icons.Default.History)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun MainAppLayout() {
    var isDarkTheme by remember { mutableStateOf(false) }
    
    MyApplicationTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val viewModel: FoodDeliveryViewModel = viewModel()
        
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Determine if bottom navigation should be visible (only on primary tabs)
        val tabRoutes = listOf(
            Screen.Home.route,
            Screen.Favorites.route,
            Screen.Cart.route,
            Screen.History.route,
            Screen.Profile.route
        )
        val showBottomBar = currentRoute in tabRoutes

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    NavigationBar {
                        val items = listOf(
                            Screen.Home,
                            Screen.Favorites,
                            Screen.Cart,
                            Screen.History,
                            Screen.Profile
                        )
                        items.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(innerPadding)
            ) {
                // Splash Screen
                composable("splash") {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate("onboarding") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                // Onboarding Screen
                composable("onboarding") {
                    OnboardingScreen(
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                // Login Screen
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToSignup = {
                            navController.navigate("signup")
                        },
                        viewModel = viewModel
                    )
                }

                // Signup Screen
                composable("signup") {
                    SignupScreen(
                        onSignupSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo("signup") { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        },
                        viewModel = viewModel
                    )
                }

                // --- Bottom Tab Screens ---
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToRestaurant = { restId ->
                            navController.navigate("restaurant/$restId")
                        },
                        onNavigateToFood = { foodId ->
                            navController.navigate("food/$foodId")
                        },
                        onNavigateToSearch = {
                            navController.navigate("search")
                        },
                        onNavigateToCategories = {
                            navController.navigate("categories")
                        },
                        onNavigateToCart = {
                            navController.navigate(Screen.Cart.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                            }
                        },
                        viewModel = viewModel
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        onNavigateToRestaurant = { restId ->
                            navController.navigate("restaurant/$restId")
                        },
                        viewModel = viewModel
                    )
                }

                composable(Screen.Cart.route) {
                    CartScreen(
                        onNavigateToCheckout = {
                            navController.navigate("checkout")
                        },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        viewModel = viewModel
                    )
                }

                composable(Screen.History.route) {
                    OrderHistoryScreen(
                        onNavigateToTracking = { orderId ->
                            navController.navigate("tracking/$orderId")
                        },
                        viewModel = viewModel
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            // Simple clear profile + logout redirect
                            viewModel.clearCart()
                            navController.navigate("login") {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        viewModel = viewModel
                    )
                }

                // --- Secondary Detailed Screens ---
                composable("search") {
                    SearchScreen(
                        onNavigateToRestaurant = { restId ->
                            navController.navigate("restaurant/$restId")
                        },
                        onNavigateToFood = { foodId ->
                            navController.navigate("food/$foodId")
                        },
                        onBack = { navController.navigateUp() },
                        viewModel = viewModel
                    )
                }

                composable("categories") {
                    CategoriesScreen(
                        onNavigateToHomeAndFilter = { catId ->
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onBack = { navController.navigateUp() },
                        viewModel = viewModel
                    )
                }

                composable(
                    route = "restaurant/{restaurantId}",
                    arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val restId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                    RestaurantDetailsScreen(
                        restaurantId = restId,
                        onNavigateToFood = { foodId ->
                            navController.navigate("food/$foodId")
                        },
                        onBack = { navController.navigateUp() },
                        viewModel = viewModel
                    )
                }

                composable(
                    route = "food/{foodId}",
                    arguments = listOf(navArgument("foodId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                    FoodDetailsScreen(
                        foodId = foodId,
                        onBack = { navController.navigateUp() },
                        viewModel = viewModel
                    )
                }

                composable("checkout") {
                    CheckoutScreen(
                        onOrderPlaced = { orderId ->
                            navController.navigate("tracking/$orderId") {
                                popUpTo("checkout") { inclusive = true }
                            }
                        },
                        onBack = { navController.navigateUp() },
                        viewModel = viewModel
                    )
                }

                composable(
                    route = "tracking/{orderId}",
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderTrackingScreen(
                        orderId = orderId,
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        viewModel = viewModel
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onDarkThemeChange = { isDarkTheme = it }
                    )
                }
            }
            
            // Floating icon overlay for settings accessible from Home or Profile
            if (showBottomBar && currentRoute == Screen.Profile.route) {
                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = BrandOrange)
                }
            }
        }
    }
}
