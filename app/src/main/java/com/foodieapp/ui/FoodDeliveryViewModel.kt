package com.foodieapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.foodieapp.data.CartEntity
import com.foodieapp.data.Category
import com.foodieapp.data.FavoriteEntity
import com.foodieapp.data.FoodDatabase
import com.foodieapp.data.FoodItem
import com.foodieapp.data.FoodRepository
import com.foodieapp.data.OrderEntity
import com.foodieapp.data.Restaurant
import com.foodieapp.data.UserEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FoodDeliveryViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize Database & Repository safely
    private val database: FoodDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            FoodDatabase::class.java,
            "food_delivery_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val repository: FoodRepository by lazy {
        FoodRepository(database.foodDeliveryDao())
    }

    // --- Core Reactive UI States ---
    val categories: List<Category> = repository.getCategories()
    val allRestaurants: List<Restaurant> = repository.getRestaurants()
    val allFoodItems: List<FoodItem> = repository.getFoodItems()

    // Filter states
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isPureVegFilter = MutableStateFlow(false)
    val isPureVegFilter = _isPureVegFilter.asStateFlow()

    // Computed filtered restaurants
    val filteredRestaurants: StateFlow<List<Restaurant>> = combine(
        _searchQuery,
        _selectedCategoryId,
        _isPureVegFilter
    ) { query, categoryId, pureVeg ->
        var list = repository.getRestaurants()
        
        // Apply search query
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.cuisines.any { cuisine -> cuisine.contains(query, ignoreCase = true) }
            }
        }
        
        // Apply category filter
        if (categoryId != null) {
            val categoryName = categories.find { it.id == categoryId }?.name
            if (categoryName != null) {
                // Find restaurants that offer foods matching this category
                val restaurantIdsWithCategory = allFoodItems
                    .filter { it.category.equals(categoryName, ignoreCase = true) }
                    .map { it.restaurantId }
                    .toSet()
                
                list = list.filter { it.id in restaurantIdsWithCategory || it.cuisines.any { cuisine -> cuisine.contains(categoryName, ignoreCase = true) } }
            }
        }
        
        // Apply pure veg filter
        if (pureVeg) {
            list = list.filter { it.isPureVeg }
        }
        
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allRestaurants)

    // Room state flows
    val cartItems: StateFlow<List<CartEntity>> = repository.getCartItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.getOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserEntity?> = repository.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Cart calculations
    val cartSubtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Simulated Active Order Tracking Flow
    private val _activeTrackingOrderId = MutableStateFlow<String?>(null)
    val activeTrackingOrderId = _activeTrackingOrderId.asStateFlow()

    private val _activeOrderStatus = MutableStateFlow<String>("Placed")
    val activeOrderStatus = _activeOrderStatus.asStateFlow()

    private var trackingJob: Job? = null

    init {
        // Prepare initial dummy user profile if database is empty
        viewModelScope.launch {
            repository.initializeDefaultUserIfNeeded()
        }
    }

    // --- UI Actions ---
    
    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePureVegFilter() {
        _isPureVegFilter.value = !_isPureVegFilter.value
    }

    // Cart modification
    fun addToCart(foodItem: FoodItem, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(foodItem, quantity)
        }
    }

    fun updateCartQuantity(foodItemId: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(foodItemId, quantity)
        }
    }

    fun removeFromCart(foodItemId: String) {
        viewModelScope.launch {
            repository.removeFromCart(foodItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Favorites
    fun toggleFavorite(targetId: String, type: String) {
        viewModelScope.launch {
            repository.toggleFavorite(targetId, type)
        }
    }

    // Checkout and placing order
    fun checkoutCart(deliveryAddress: String, onOrderPlaced: (String) -> Unit) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch
            
            val firstItem = items.first()
            val subtotal = items.sumOf { it.price * it.quantity }
            val total = subtotal + 30.0 + 15.0 // Subtotal + delivery + tax
            
            val orderId = repository.placeOrder(
                restaurantId = firstItem.restaurantId,
                restaurantName = firstItem.restaurantName,
                items = items,
                totalAmount = total,
                deliveryAddress = deliveryAddress
            )
            
            onOrderPlaced(orderId)
            startOrderTrackingSimulation(orderId)
        }
    }

    // Live tracking simulation
    fun startOrderTrackingSimulation(orderId: String) {
        _activeTrackingOrderId.value = orderId
        _activeOrderStatus.value = "Placed"
        
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            // Stage 1: Placed -> Preparing
            delay(8000)
            _activeOrderStatus.value = "Preparing"
            repository.updateOrderStatus(orderId, "Preparing")
            
            // Stage 2: Preparing -> Out for Delivery
            delay(10000)
            _activeOrderStatus.value = "Out for Delivery"
            repository.updateOrderStatus(orderId, "Out for Delivery")
            
            // Stage 3: Out for Delivery -> Delivered
            delay(12000)
            _activeOrderStatus.value = "Delivered"
            repository.updateOrderStatus(orderId, "Delivered")
        }
    }

    fun stopOrderTrackingSimulation() {
        trackingJob?.cancel()
        _activeTrackingOrderId.value = null
    }

    // Profile updates
    fun updateProfile(name: String, email: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.saveUserProfile(name, email, phone, address, isLoggedIn = true)
        }
    }
}
