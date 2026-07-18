package com.foodieapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class FoodRepository(private val dao: FoodDeliveryDao) {

    // --- Static Static Data Queries ---
    fun getCategories(): List<Category> {
        return SampleData.categories
    }

    fun getRestaurants(): List<Restaurant> {
        return SampleData.restaurants
    }

    fun getRestaurant(id: String): Restaurant? {
        return SampleData.restaurants.find { it.id == id }
    }

    fun getFoodItems(): List<FoodItem> {
        return SampleData.foodItems
    }

    fun getFoodItemsForRestaurant(restaurantId: String): List<FoodItem> {
        return SampleData.foodItems.filter { it.restaurantId == restaurantId }
    }

    fun getFoodItem(id: String): FoodItem? {
        return SampleData.foodItems.find { it.id == id }
    }

    fun searchFoodItems(query: String): List<FoodItem> {
        if (query.isBlank()) return emptyList()
        return SampleData.foodItems.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    fun searchRestaurants(query: String): List<Restaurant> {
        if (query.isBlank()) return SampleData.restaurants
        return SampleData.restaurants.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.cuisines.any { cuisine -> cuisine.contains(query, ignoreCase = true) }
        }
    }

    // --- Cart Room Database Operations ---
    fun getCartItemsFlow(): Flow<List<CartEntity>> = dao.getCartItemsFlow()

    suspend fun addToCart(foodItem: FoodItem, quantity: Int) {
        val existingItems = dao.getCartItems()
        val existing = existingItems.find { it.foodItemId == foodItem.id }
        
        if (existing != null) {
            dao.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            dao.insertCartItem(
                CartEntity(
                    foodItemId = foodItem.id,
                    restaurantId = foodItem.restaurantId,
                    restaurantName = foodItem.restaurantName,
                    foodName = foodItem.name,
                    price = foodItem.price,
                    quantity = quantity,
                    imageUrl = foodItem.imageUrl,
                    isVeg = foodItem.isVeg
                )
            )
        }
    }

    suspend fun updateCartQuantity(foodItemId: String, quantity: Int) {
        if (quantity <= 0) {
            dao.deleteCartItemById(foodItemId)
        } else {
            val existing = dao.getCartItems().find { it.foodItemId == foodItemId }
            if (existing != null) {
                dao.updateCartItem(existing.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeFromCart(foodItemId: String) {
        dao.deleteCartItemById(foodItemId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    // --- Favorites Room Database Operations ---
    fun getFavoritesFlow(): Flow<List<FavoriteEntity>> = dao.getFavoritesFlow()

    fun isFavoriteFlow(targetId: String): Flow<Boolean> = dao.isFavoriteFlow(targetId)

    suspend fun toggleFavorite(targetId: String, type: String) {
        val favorites = dao.getFavoritesFlow().firstOrNull() ?: emptyList()
        val isFav = favorites.any { it.targetId == targetId }
        if (isFav) {
            dao.removeFavorite(targetId)
        } else {
            dao.addFavorite(FavoriteEntity(targetId, type))
        }
    }

    // --- Orders Room Database Operations ---
    fun getOrdersFlow(): Flow<List<OrderEntity>> = dao.getOrdersFlow()

    fun getOrderFlow(orderId: String): Flow<OrderEntity?> = dao.getOrderFlow(orderId)

    suspend fun placeOrder(
        restaurantId: String,
        restaurantName: String,
        items: List<CartEntity>,
        totalAmount: Double,
        deliveryAddress: String
    ): String {
        val orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val summary = items.joinToString(", ") { "${it.foodName} x${it.quantity}" }
        
        val newOrder = OrderEntity(
            orderId = orderId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            itemsSummary = summary,
            totalAmount = totalAmount,
            status = "Placed",
            timestamp = System.currentTimeMillis(),
            deliveryAddress = deliveryAddress
        )
        
        dao.insertOrder(newOrder)
        dao.clearCart()
        return orderId
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        val order = dao.getOrderById(orderId)
        if (order != null) {
            dao.updateOrder(order.copy(status = status))
        }
    }

    // --- User Profile Room Database Operations ---
    fun getUserProfileFlow(): Flow<UserEntity?> = dao.getUserProfileFlow()

    suspend fun saveUserProfile(
        name: String,
        email: String,
        phone: String,
        address: String,
        isLoggedIn: Boolean
    ) {
        val user = UserEntity(
            name = name,
            email = email,
            phone = phone,
            address = address,
            isLoggedIn = isLoggedIn
        )
        dao.saveUserProfile(user)
    }

    suspend fun initializeDefaultUserIfNeeded() {
        val user = dao.getUserProfile()
        if (user == null) {
            dao.saveUserProfile(
                UserEntity(
                    name = "John Doe",
                    email = "john.doe@example.com",
                    phone = "+1 234 567 8900",
                    address = "123 Main Street, Apt 4B, New York, NY",
                    isLoggedIn = true
                )
            )
        }
    }
}
