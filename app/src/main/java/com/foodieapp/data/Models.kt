package com.foodieapp.data

import java.io.Serializable

// Domain Models
data class Category(
    val id: String,
    val name: String,
    val iconRes: String, // Icon name or character representation
    val imageUrl: String
) : Serializable

data class Restaurant(
    val id: String,
    val name: String,
    val imageUrl: String,
    val cuisines: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val deliveryTimeMinutes: Int,
    val distanceKm: Double,
    val averagePriceForTwo: Int,
    val isPureVeg: Boolean,
    val isFeatured: Boolean,
    val address: String
) : Serializable

data class FoodItem(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val category: String,
    val isVeg: Boolean,
    val isBestSeller: Boolean,
    val rating: Double
) : Serializable

// Static Seed Data
object SampleData {
    val categories = listOf(
        Category("cat_1", "Pizza", "🍕", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop&q=60"),
        Category("cat_2", "Burgers", "🍔", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=60"),
        Category("cat_3", "Sushi", "🍣", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop&q=60"),
        Category("cat_4", "Salads", "🥗", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop&q=60"),
        Category("cat_5", "Desserts", "🍰", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=500&auto=format&fit=crop&q=60"),
        Category("cat_6", "Biryani", "🍛", "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop&q=60"),
        Category("cat_7", "Noodles", "🍜", "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=500&auto=format&fit=crop&q=60"),
        Category("cat_8", "Drinks", "🥤", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500&auto=format&fit=crop&q=60")
    )

    val restaurants = listOf(
        Restaurant(
            id = "rest_1",
            name = "The Pizza Planet",
            imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop&q=80",
            cuisines = listOf("Italian", "Pizza", "Desserts"),
            rating = 4.5,
            reviewCount = 1250,
            deliveryTimeMinutes = 25,
            distanceKm = 2.4,
            averagePriceForTwo = 400,
            isPureVeg = false,
            isFeatured = true,
            address = "42 Galleria Mall Road, Sector 5"
        ),
        Restaurant(
            id = "rest_2",
            name = "Burger Bistro",
            imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=600&auto=format&fit=crop&q=80",
            cuisines = listOf("American", "Burgers", "Fast Food"),
            rating = 4.2,
            reviewCount = 820,
            deliveryTimeMinutes = 20,
            distanceKm = 1.8,
            averagePriceForTwo = 300,
            isPureVeg = false,
            isFeatured = false,
            address = "Shop 12, High Street Plaza"
        ),
        Restaurant(
            id = "rest_3",
            name = "Sakura Zen",
            imageUrl = "https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=600&auto=format&fit=crop&q=80",
            cuisines = listOf("Japanese", "Sushi", "Noodles"),
            rating = 4.8,
            reviewCount = 450,
            deliveryTimeMinutes = 35,
            distanceKm = 4.1,
            averagePriceForTwo = 800,
            isPureVeg = false,
            isFeatured = true,
            address = "Penthouse Level, Grand Mall"
        ),
        Restaurant(
            id = "rest_4",
            name = "The Green Garden",
            imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80",
            cuisines = listOf("Healthy", "Salads", "Juices"),
            rating = 4.6,
            reviewCount = 380,
            deliveryTimeMinutes = 18,
            distanceKm = 1.2,
            averagePriceForTwo = 350,
            isPureVeg = true,
            isFeatured = false,
            address = "Block C, Green Park Avenue"
        ),
        Restaurant(
            id = "rest_5",
            name = "Royal Biryani House",
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600&auto=format&fit=crop&q=80",
            cuisines = listOf("Indian", "Biryani", "Mughlai"),
            rating = 4.4,
            reviewCount = 2100,
            deliveryTimeMinutes = 30,
            distanceKm = 3.5,
            averagePriceForTwo = 500,
            isPureVeg = false,
            isFeatured = true,
            address = "Opposite Metro Pillar 203, Central Link"
        ),
        Restaurant(
            id = "rest_6",
            name = "Sweet Treats",
            imageUrl = "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=600&auto=format&fit=crop&q=80",
            cuisines = listOf("Desserts", "Bakery", "Ice Cream"),
            rating = 4.3,
            reviewCount = 590,
            deliveryTimeMinutes = 15,
            distanceKm = 0.8,
            averagePriceForTwo = 250,
            isPureVeg = true,
            isFeatured = false,
            address = "Ground Floor, Sunrise Complex"
        )
    )

    val foodItems = listOf(
        // Restaurant 1: The Pizza Planet
        FoodItem(
            "food_1_1", "rest_1", "The Pizza Planet",
            "Margherita Premium Pizza", 249.0,
            "Classic fresh mozzarella cheese, rich tomato sauce, basil leaves, and a drizzle of extra virgin olive oil on hand-stretched thin crust.",
            "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=500&auto=format&fit=crop&q=60",
            "Pizza", isVeg = true, isBestSeller = true, rating = 4.6
        ),
        FoodItem(
            "food_1_2", "rest_1", "The Pizza Planet",
            "Double Cheese Pepperoni Pizza", 349.0,
            "Loaded with spicy pork pepperoni, extra stringy mozzarella cheese, and a secret Italian herb seasoning base.",
            "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=500&auto=format&fit=crop&q=60",
            "Pizza", isVeg = false, isBestSeller = true, rating = 4.8
        ),
        FoodItem(
            "food_1_3", "rest_1", "The Pizza Planet",
            "Veggie Supreme Feast", 299.0,
            "A colorful load of gold sweetcorn, crisp red onion, black olives, green bell pepper, and fresh button mushrooms.",
            "https://images.unsplash.com/photo-1571066811602-71683a3f680d?w=500&auto=format&fit=crop&q=60",
            "Pizza", isVeg = true, isBestSeller = false, rating = 4.3
        ),

        // Restaurant 2: Burger Bistro
        FoodItem(
            "food_2_1", "rest_2", "Burger Bistro",
            "Classic Grilled Chicken Burger", 149.0,
            "Tender flame-grilled chicken breast patty topped with creamy mayonnaise, crunchy iceberg lettuce, and fresh tomatoes in a toasted sesame bun.",
            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=60",
            "Burgers", isVeg = false, isBestSeller = true, rating = 4.4
        ),
        FoodItem(
            "food_2_2", "rest_2", "Burger Bistro",
            "Ultimate Double Cheese Veggie Burger", 129.0,
            "A crispy, golden mixed vegetable patty sandwiched between double slices of melted cheddar cheese, onion rings, and sweet pickles with spicy mustard.",
            "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=500&auto=format&fit=crop&q=60",
            "Burgers", isVeg = true, isBestSeller = false, rating = 4.1
        ),
        FoodItem(
            "food_2_3", "rest_2", "Burger Bistro",
            "Peri Peri Loaded Crispy Fries", 99.0,
            "Golden skin-on potato fries tossed in a fiery peri-peri seasoning and drizzled with cheese sauce and spring onions.",
            "https://images.unsplash.com/photo-1576107232684-1279f390859f?w=500&auto=format&fit=crop&q=60",
            "Drinks", isVeg = true, isBestSeller = true, rating = 4.5
        ),

        // Restaurant 3: Sakura Zen
        FoodItem(
            "food_3_1", "rest_3", "Sakura Zen",
            "Signature Salmon Roll (8 Pcs)", 499.0,
            "Fresh Atlantic salmon, creamy avocado, cucumber, wrapped with seasoned sushi rice, toasted seaweed, topped with orange masago caviar.",
            "https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=500&auto=format&fit=crop&q=60",
            "Sushi", isVeg = false, isBestSeller = true, rating = 4.9
        ),
        FoodItem(
            "food_3_2", "rest_3", "Sakura Zen",
            "Vegetarian Tempura Maki (8 Pcs)", 399.0,
            "Crispy golden-fried baby corn and asparagus tempura rolled inside, drizzled with sweet unagi sauce and spicy sesame mayo.",
            "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop&q=60",
            "Sushi", isVeg = true, isBestSeller = false, rating = 4.7
        ),
        FoodItem(
            "food_3_3", "rest_3", "Sakura Zen",
            "Spicy Seafood Ramen", 429.0,
            "Springy wheat noodles served in a rich, 12-hour simmered spicy broth, topped with grilled shrimp, squid, soft-boiled egg, and nori sheets.",
            "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500&auto=format&fit=crop&q=60",
            "Noodles", isVeg = false, isBestSeller = true, rating = 4.8
        ),

        // Restaurant 4: The Green Garden
        FoodItem(
            "food_4_1", "rest_4", "The Green Garden",
            "Avocado Quinoa Power Salad", 229.0,
            "Fresh Hass avocado halves, high-protein white quinoa, organic cherry tomatoes, sliced cucumbers, and feta cheese, with a zesty lemon-herb vinaigrette.",
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop&q=60",
            "Salads", isVeg = true, isBestSeller = true, rating = 4.7
        ),
        FoodItem(
            "food_4_2", "rest_4", "The Green Garden",
            "Roasted Beetroot & Goat Cheese Salad", 199.0,
            "Sweet roasted beetroot slices, baby arugula, soft goat cheese crumble, and candied walnuts, tossed in a balsamic glaze dressing.",
            "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500&auto=format&fit=crop&q=60",
            "Salads", isVeg = true, isBestSeller = false, rating = 4.4
        ),

        // Restaurant 5: Royal Biryani House
        FoodItem(
            "food_5_1", "rest_5", "Royal Biryani House",
            "Dum Chicken Biryani (Hyderabadi)", 299.0,
            "Basmati rice slow-cooked on dum with marinated chicken pieces, aromatic Indian spices, saffron strands, and fresh mint leaves. Served with cool cucumber raita.",
            "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500&auto=format&fit=crop&q=60",
            "Biryani", isVeg = false, isBestSeller = true, rating = 4.8
        ),
        FoodItem(
            "food_5_2", "rest_5", "Royal Biryani House",
            "Shahi Paneer Tikka Dum Biryani", 279.0,
            "Richly spiced paneer cubes grilled in a tandoor, layered with long-grain basmati rice, caramelized onions, and ghee. A vegetarian delicacy.",
            "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500&auto=format&fit=crop&q=60",
            "Biryani", isVeg = true, isBestSeller = true, rating = 4.5
        ),

        // Restaurant 6: Sweet Treats
        FoodItem(
            "food_6_1", "rest_6", "Sweet Treats",
            "Fudge Chocolate Brownie with Ice Cream", 119.0,
            "Warm, gooey dark chocolate fudge brownie served with a scoop of premium vanilla bean ice cream and hot chocolate fudge sauce.",
            "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=500&auto=format&fit=crop&q=60",
            "Desserts", isVeg = true, isBestSeller = true, rating = 4.6
        ),
        FoodItem(
            "food_6_2", "rest_6", "Sweet Treats",
            "New York Style Baked Cheesecake", 149.0,
            "A rich, creamy, and smooth baked cream cheese slice over a buttery graham cracker crust, topped with a fresh strawberry compote.",
            "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500&auto=format&fit=crop&q=60",
            "Desserts", isVeg = true, isBestSeller = true, rating = 4.5
        )
    )
}
