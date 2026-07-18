package com.foodieapp.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foodieapp.R
import com.foodieapp.ui.theme.BrandAmber
import com.foodieapp.ui.theme.BrandOrange
import com.foodieapp.ui.theme.VegGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToFood: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val filteredRestaurants by viewModel.filteredRestaurants.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val isPureVegOnly by viewModel.isPureVegFilter.collectAsState()
    
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val cartSubtotal by viewModel.cartSubtotal.collectAsState()

    val favRestaurantIds = remember(favorites) {
        favorites.filter { it.type == "restaurant" }.map { it.targetId }.toSet()
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. App Bar Header
            HeaderBar(
                address = userProfile?.address ?: "Set delivery location...",
                userName = userProfile?.name ?: "User",
                onCartClick = onNavigateToCart,
                cartItemCount = cartItemCount
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // 2. Search Trigger Bar
                item {
                    SearchTriggerSection(onClick = onNavigateToSearch)
                }

                // 3. Featured Banner
                item {
                    HeroPromoBanner()
                }

                // 4. Categories Section Title
                item {
                    SectionHeader(
                        title = "What's on your mind?",
                        actionText = "See All",
                        onActionClick = onNavigateToCategories
                    )
                }

                // 5. Category Horizontal List
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CategoryItemCircle(
                                categoryName = "All",
                                emoji = "✨",
                                isSelected = selectedCategory == null,
                                onClick = { viewModel.setCategoryFilter(null) }
                            )
                        }
                        items(viewModel.categories) { category ->
                            CategoryItemCircle(
                                categoryName = category.name,
                                emoji = category.iconRes,
                                isSelected = selectedCategory == category.id,
                                onClick = { viewModel.setCategoryFilter(category.id) }
                            )
                        }
                    }
                }

                // 6. Restaurant Section Header and Filters
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Restaurants near you",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Pure Veg Toggle
                        FilterChip(
                            selected = isPureVegOnly,
                            onClick = { viewModel.togglePureVegFilter() },
                            label = { Text("Pure Veg 🥦", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VegGreen.copy(alpha = 0.2f),
                                selectedLabelColor = VegGreen
                            )
                        )
                    }
                }

                // 7. Restaurants List
                if (filteredRestaurants.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(
                            title = "No Restaurants Found",
                            subtitle = "Try resetting your filters or searching for something else."
                        )
                    }
                } else {
                    items(filteredRestaurants) { rest ->
                        RestaurantItemCard(
                            restaurant = rest,
                            isFavorite = favRestaurantIds.contains(rest.id),
                            onToggleFavorite = { viewModel.toggleFavorite(rest.id, "restaurant") },
                            onClick = { onNavigateToRestaurant(rest.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Cart Sticky Footer
        CartSummaryStickyBar(
            itemCount = cartItemCount,
            subtotal = cartSubtotal,
            onViewCartClick = onNavigateToCart,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HeaderBar(
    address: String,
    userName: String,
    onCartClick: () -> Unit,
    cartItemCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = BrandOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Deliver to",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )
            }
            Text(
                text = address,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onCartClick,
            modifier = Modifier.testTag("home_cart_button")
        ) {
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge(containerColor = BrandOrange) {
                            Text(cartItemCount.toString(), color = Color.White)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun SearchTriggerSection(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("search_bar_trigger")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search icon",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Search for food, cuisines or desserts...",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun HeroPromoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("promo_banner"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_food_banner),
                contentDescription = "Culinary Header Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Elegant linear overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(220.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandAmber)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "50% OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gourmet Feasts at Home",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp
                )
                Text(
                    text = "Free delivery on your first order!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = actionText,
            color = BrandOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun CategoryItemCircle(
    categoryName: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("category_pill_$categoryName")
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) BrandOrange.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) BrandOrange else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = categoryName,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStatePlaceholder(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

// --- Search Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToRestaurant: (String) -> Unit,
    onNavigateToFood: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val restaurants = viewModel.allRestaurants
    val foodItems = viewModel.allFoodItems
    val cartItems by viewModel.cartItems.collectAsState()

    val searchResultsRest = remember(query) {
        if (query.isBlank()) emptyList()
        else restaurants.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.cuisines.any { cuisine -> cuisine.contains(query, ignoreCase = true) }
        }
    }

    val searchResultsFood = remember(query) {
        if (query.isBlank()) emptyList()
        else foodItems.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    val cartQuantities = remember(cartItems) {
        cartItems.associate { it.foodItemId to it.quantity }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Input TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
            }
            
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search pizza, burger, sushi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOrange,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (query.isBlank()) {
                // Quick Search Keywords
                item {
                    Text(
                        text = "Popular Cuisines",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val tags = listOf("Pizza", "Biryani", "Burger", "Noodles", "Desserts", "Sushi")
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { query = tag },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            } else {
                // Display results
                if (searchResultsRest.isEmpty() && searchResultsFood.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(
                            title = "No results found for \"$query\"",
                            subtitle = "Check your spelling or search for something else."
                        )
                    }
                } else {
                    if (searchResultsRest.isNotEmpty()) {
                        item {
                            Text(
                                text = "Restaurants Matching",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(searchResultsRest) { rest ->
                            RestaurantItemCard(
                                restaurant = rest,
                                isFavorite = false,
                                onToggleFavorite = {},
                                onClick = { onNavigateToRestaurant(rest.id) }
                            )
                        }
                    }

                    if (searchResultsFood.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Dishes Matching",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(searchResultsFood) { dish ->
                            FoodItemRow(
                                foodItem = dish,
                                cartQuantity = cartQuantities[dish.id] ?: 0,
                                onAddToCart = { viewModel.addToCart(dish) },
                                onQuantityChanged = { q -> viewModel.updateCartQuantity(dish.id, q) },
                                onClick = { onNavigateToFood(dish.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Categories Grid Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateToHomeAndFilter: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("All Categories", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.categories) { category ->
                Card(
                    onClick = {
                        viewModel.setCategoryFilter(category.id)
                        onNavigateToHomeAndFilter(category.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("category_grid_${category.name}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(category.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = category.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = category.iconRes,
                                fontSize = 20.sp
                            )
                            Text(
                                text = category.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Restaurant Details Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    restaurantId: String,
    onNavigateToFood: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val restaurant = remember(restaurantId) { viewModel.allRestaurants.find { it.id == restaurantId } }
    val foodItems = remember(restaurantId) { viewModel.allFoodItems.filter { it.restaurantId == restaurantId } }
    val cartItems by viewModel.cartItems.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val isFav = remember(favorites, restaurantId) {
        favorites.any { it.targetId == restaurantId && it.type == "restaurant" }
    }

    val cartQuantities = remember(cartItems) {
        cartItems.associate { it.foodItemId to it.quantity }
    }

    var menuSearchQuery by remember { mutableStateOf("") }
    var showVegOnly by remember { mutableStateOf(false) }

    val filteredMenu = remember(menuSearchQuery, showVegOnly, foodItems) {
        var menu = foodItems
        if (menuSearchQuery.isNotBlank()) {
            menu = menu.filter { it.name.contains(menuSearchQuery, ignoreCase = true) }
        }
        if (showVegOnly) {
            menu = menu.filter { it.isVeg }
        }
        menu
    }

    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val cartSubtotal by viewModel.cartSubtotal.collectAsState()

    if (restaurant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Restaurant Not Found")
        }
        return
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Image Box with back/favorite overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(restaurant.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlaid buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavorite(restaurant.id, "restaurant") },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) BrandOrange else Color.White
                        )
                    }
                }
            }

            // Info Card Overlay
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-30).dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = restaurant.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = restaurant.cuisines.joinToString(", "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍  ${restaurant.address}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RatingBadge(rating = restaurant.rating)
                            Text("${restaurant.reviewCount} reviews", fontSize = 9.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${restaurant.deliveryTimeMinutes} mins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Delivery Time", fontSize = 9.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$${restaurant.averagePriceForTwo}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("For Two", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Search and Filters for Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = menuSearchQuery,
                    onValueChange = { menuSearchQuery = it },
                    placeholder = { Text("Search dishes in menu", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                FilterChip(
                    selected = showVegOnly,
                    onClick = { showVegOnly = !showVegOnly },
                    label = { Text("Veg Only 🥦", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VegGreen.copy(alpha = 0.15f),
                        selectedLabelColor = VegGreen
                    ),
                    modifier = Modifier.height(34.dp)
                )
            }

            // Menu List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = (-10).dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Full Menu (${filteredMenu.size} items)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (filteredMenu.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(
                            title = "No menu items match your filter",
                            subtitle = "Try resetting your veggie flag or search query."
                        )
                    }
                } else {
                    items(filteredMenu) { dish ->
                        FoodItemRow(
                            foodItem = dish,
                            cartQuantity = cartQuantities[dish.id] ?: 0,
                            onAddToCart = { viewModel.addToCart(dish) },
                            onQuantityChanged = { q -> viewModel.updateCartQuantity(dish.id, q) },
                            onClick = { onNavigateToFood(dish.id) }
                        )
                    }
                }
            }
        }

        // Bottom Cart Sticky
        CartSummaryStickyBar(
            itemCount = cartItemCount,
            subtotal = cartSubtotal,
            onViewCartClick = { viewModel.clearCart() }, // Temporary simple checkout redirect or onViewCartClick trigger
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// --- Food Details Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    foodId: String,
    onBack: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val foodItem = remember(foodId) { viewModel.allFoodItems.find { it.id == foodId } }
    val cartItems by viewModel.cartItems.collectAsState()

    val cartQuantity = remember(cartItems, foodId) {
        cartItems.find { it.foodItemId == foodId }?.quantity ?: 0
    }

    if (foodItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Food Item Not Found")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(foodItem.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = foodItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VegIndicator(isVeg = foodItem.isVeg)
                Spacer(modifier = Modifier.width(8.dp))
                RatingBadge(rating = foodItem.rating)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = foodItem.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "From ${foodItem.restaurantName}",
                fontSize = 14.sp,
                color = BrandOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "$${foodItem.price}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = foodItem.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nutrition Facts Box
            Text(
                text = "Nutrition Information",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionItem(name = "Calories", valStr = "350 kcal")
                NutritionItem(name = "Protein", valStr = "12g")
                NutritionItem(name = "Carbs", valStr = "45g")
                NutritionItem(name = "Fat", valStr = "8g")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add/Edit Quantity control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Price", fontSize = 11.sp, color = Color.Gray)
                    val count = if (cartQuantity > 0) cartQuantity else 1
                    Text(
                        text = "$${String.format("%.2f", foodItem.price * count)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandOrange
                    )
                }

                if (cartQuantity > 0) {
                    QuantitySelector(
                        quantity = cartQuantity,
                        onIncrement = { viewModel.updateCartQuantity(foodItem.id, cartQuantity + 1) },
                        onDecrement = { viewModel.updateCartQuantity(foodItem.id, cartQuantity - 1) },
                        modifier = Modifier.width(120.dp).height(44.dp)
                    )
                } else {
                    Button(
                        onClick = { viewModel.addToCart(foodItem) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(140.dp)
                            .height(44.dp)
                    ) {
                        Text("Add to Cart", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionItem(name: String, valStr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, fontSize = 10.sp, color = Color.Gray)
        Text(text = valStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// --- Favorites Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToRestaurant: (String) -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val restaurants = viewModel.allRestaurants

    val favRestaurants = remember(favorites) {
        val favIds = favorites.filter { it.type == "restaurant" }.map { it.targetId }.toSet()
        restaurants.filter { it.id in favIds }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("My Favorites", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        if (favRestaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(
                    title = "No Favorites Yet",
                    subtitle = "Tap the heart icon on any restaurant card to save it here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(favRestaurants) { rest ->
                    RestaurantItemCard(
                        restaurant = rest,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavorite(rest.id, "restaurant") },
                        onClick = { onNavigateToRestaurant(rest.id) }
                    )
                }
            }
        }
    }
}
