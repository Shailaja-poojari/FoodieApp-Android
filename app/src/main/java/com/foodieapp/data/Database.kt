package com.foodieapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Entities
@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val foodItemId: String,
    val restaurantId: String,
    val restaurantName: String,
    val foodName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String,
    val isVeg: Boolean
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val targetId: String, // Can be restaurantId or foodItemId
    val type: String // "restaurant" or "food"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val restaurantId: String,
    val restaurantName: String,
    val itemsSummary: String, // e.g., "Pizza x1, Burger x2"
    val totalAmount: Double,
    val status: String, // "Placed", "Preparing", "Out for Delivery", "Delivered"
    val timestamp: Long,
    val deliveryAddress: String
)

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val isLoggedIn: Boolean = false
)

// DAO
@Dao
interface FoodDeliveryDao {
    // Cart operations
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_items")
    suspend fun getCartItems(): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartEntity)

    @Query("DELETE FROM cart_items WHERE foodItemId = :itemId")
    suspend fun deleteCartItemById(itemId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Favorites operations
    @Query("SELECT * FROM favorites")
    fun getFavoritesFlow(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE targetId = :targetId LIMIT 1)")
    fun isFavoriteFlow(targetId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE targetId = :targetId")
    suspend fun removeFavorite(targetId: String)

    // Order operations
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun getOrderFlow(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    // User operations
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(user: UserEntity)
}

// Database abstract holder
@Database(
    entities = [CartEntity::class, FavoriteEntity::class, OrderEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDeliveryDao(): FoodDeliveryDao
}
