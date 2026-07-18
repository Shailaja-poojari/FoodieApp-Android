package com.foodieapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foodieapp.data.CartEntity
import com.foodieapp.data.OrderEntity
import com.foodieapp.ui.theme.BrandOrange
import com.foodieapp.ui.theme.VegGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToCheckout: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

    val deliveryFee = if (subtotal > 0) 30.0 else 0.0
    val taxes = if (subtotal > 0) 15.0 else 0.0
    val totalAmount = subtotal + deliveryFee + taxes

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Shopping Cart", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onNavigateToHome) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cart is empty!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Add food items to place an order.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("Order Food Now", color = Color.White)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Restaurant Tag
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Ordering from", fontSize = 10.sp, color = Color.Gray)
                                    Text(cartItems.first().restaurantName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Cart Items list
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChanged = { q -> viewModel.updateCartQuantity(item.foodItemId, q) }
                        )
                    }

                    // Bill Breakdown Card
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Bill Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                BillRow(label = "Item Subtotal", amount = subtotal)
                                BillRow(label = "Delivery Partner Fee", amount = deliveryFee)
                                BillRow(label = "Taxes & Service Charges", amount = taxes)
                                
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("To Pay", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("$${String.format("%.2f", totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = BrandOrange)
                                }
                            }
                        }
                    }
                }

                // Checkout button block
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Button(
                        onClick = onNavigateToCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp)
                            .testTag("checkout_payment_button")
                    ) {
                        Text("Proceed to Checkout  $${String.format("%.2f", totalAmount)}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun Modifier.size(size: Dp) = this.size(width = size, height = size)

@Composable
fun CartItemRow(
    item: CartEntity,
    onQuantityChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                VegIndicator(isVeg = item.isVeg)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(item.foodName, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("$${item.price} each", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantitySelector(
                    quantity = item.quantity,
                    onIncrement = { onQuantityChanged(item.quantity + 1) },
                    onDecrement = { onQuantityChanged(item.quantity - 1) },
                    modifier = Modifier.width(90.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$${String.format("%.2f", item.price * item.quantity)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(54.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun BillRow(label: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text("$${String.format("%.2f", amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// --- Checkout Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onOrderPlaced: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    
    val deliveryFee = 30.0
    val taxes = 15.0
    val totalAmount = subtotal + deliveryFee + taxes

    val coroutineScope = rememberCoroutineScope()
    var paymentMethod by remember { mutableStateOf("UPI") }
    var deliveryInstructions by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(userProfile?.address ?: "") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Checkout Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        if (isProcessing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandOrange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing Mock Payment...", fontWeight = FontWeight.Bold)
                    Text("Securing details safely", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Address Section
                item {
                    Text("Delivery Address", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandOrange)
                    )
                }

                // Delivery Instructions
                item {
                    Text("Delivery Instructions (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = deliveryInstructions,
                        onValueChange = { deliveryInstructions = it },
                        placeholder = { Text("e.g. Leave at gate, ring doorbell...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandOrange)
                    )
                }

                // Payment Methods Options
                item {
                    Text("Select Payment Method", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    PaymentOptionItem(
                        title = "UPI (Google Pay / PhonePe)",
                        icon = Icons.Default.Payment,
                        selected = paymentMethod == "UPI",
                        onClick = { paymentMethod = "UPI" }
                    )
                    PaymentOptionItem(
                        title = "Credit or Debit Card",
                        icon = Icons.Default.CreditCard,
                        selected = paymentMethod == "Card",
                        onClick = { paymentMethod = "Card" }
                    )
                    PaymentOptionItem(
                        title = "Cash on Delivery",
                        icon = Icons.Default.AttachMoney,
                        selected = paymentMethod == "COD",
                        onClick = { paymentMethod = "COD" }
                    )
                }
            }

            // Pay Button Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (address.isNotBlank()) {
                            isProcessing = true
                            // Simulate small secure latency
                            coroutineScope.launch {
                                delay(2000)
                                isProcessing = false
                                viewModel.checkoutCart(address) { orderId ->
                                    onOrderPlaced(orderId)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                        .testTag("checkout_pay_button")
                ) {
                    Text("Pay & Place Order  $${String.format("%.2f", totalAmount)}", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PaymentOptionItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("payment_option_$title"),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BrandOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) BrandOrange else Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = if (selected) BrandOrange else Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = BrandOrange)
            )
        }
    }
}

// --- Live Order Tracking Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onNavigateToHome: () -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val database = viewModel.orders
    val ordersList by database.collectAsState()
    val order = remember(ordersList, orderId) { ordersList.find { it.orderId == orderId } }
    
    val activeSimulatedStatus by viewModel.activeOrderStatus.collectAsState()

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Active Order Found.")
        }
        return
    }

    // Determine tracking stages
    val stages = listOf("Placed", "Preparing", "Out for Delivery", "Delivered")
    val currentStageIndex = remember(activeSimulatedStatus) {
        stages.indexOf(activeSimulatedStatus).coerceAtLeast(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Track Order", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onNavigateToHome) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Status Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandOrange.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, BrandOrange)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("ID: $orderId", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Est: 25 mins", fontSize = 12.sp, color = BrandOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (activeSimulatedStatus) {
                                "Placed" -> "Waiting for restaurant approval..."
                                "Preparing" -> "Chef is preparing your tasty meal..."
                                "Out for Delivery" -> "Rider is heading to your place!"
                                else -> "Arrived! Enjoy your delicious food!"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Map Simulation block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(bottom = 20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://images.unsplash.com/photo-1524661135-423995f22d0b?w=600&auto=format&fit=crop&q=80")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Map view simulation",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
                        )
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = BrandOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Live Map Simulation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Tracking Milestones Steps
            item {
                Text("Order Status Timeline", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }

            items(stages.size) { index ->
                val stageName = stages[index]
                val isCompleted = index <= currentStageIndex
                val isActive = index == currentStageIndex
                
                TrackingStepRow(
                    stepName = stageName,
                    stepDesc = when (stageName) {
                        "Placed" -> "Order accepted by ${order.restaurantName}"
                        "Preparing" -> "Kitchen preparing fresh gourmet elements"
                        "Out for Delivery" -> "Delivery agent picking up details"
                        else -> "Handed over safely"
                    },
                    isCompleted = isCompleted,
                    isActive = isActive,
                    isLast = index == stages.size - 1
                )
            }
        }

        // Return Home
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp
        ) {
            Button(
                onClick = {
                    viewModel.stopOrderTrackingSimulation()
                    onNavigateToHome()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
            ) {
                Text("Go Back to Home", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun TrackingStepRow(
    stepName: String,
    stepDesc: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) VegGreen else Color.LightGray.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                }
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(
                            if (isCompleted) VegGreen else Color.LightGray.copy(alpha = 0.5f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text(
                text = stepName,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isActive) BrandOrange else if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = stepDesc,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

// --- Order History Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onNavigateToTracking: (String) -> Unit,
    viewModel: FoodDeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("My Orders", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(
                    title = "No Orders Placed Yet",
                    subtitle = "Order some tasty foods from restaurants to see them recorded here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryCard(
                        order = order,
                        onTrackClick = { onNavigateToTracking(order.orderId) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderEntity,
    onTrackClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateStr = remember(order.timestamp) { formatter.format(Date(order.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("order_history_card_${order.orderId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(order.restaurantName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (order.status == "Delivered") VegGreen.copy(alpha = 0.15f)
                            else BrandOrange.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        color = if (order.status == "Delivered") VegGreen else BrandOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Paid: $${String.format("%.2f", order.totalAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (order.status != "Delivered") {
                    Button(
                        onClick = onTrackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Track Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
