package com.example.towdepo.ui.theme.screnns

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.towdepo.data.Address
import com.example.towdepo.di.AppContainer
import com.example.towdepo.utils.CheckoutViewModelFactory
import com.example.towdepo.viewmodels.CheckoutViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    userId: String,
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    // Get repositories from AppContainer
    val addressRepository = AppContainer.addressRepository
    val cartRepository = AppContainer.cartRepository

    // Create viewmodel with factory
    val viewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(
            addressRepository = addressRepository,
            cartRepository = cartRepository,
            userId = userId
        )
    )

    val state by viewModel.checkoutState.collectAsState()

    // State for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var addressToEdit by remember { mutableStateOf<Address?>(null) }

    // Handle order placed
    if (state.isOrderPlaced) {
        LaunchedEffect(Unit) {
            onOrderPlaced()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Loading indicator
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Error message
            state.error?.let { error ->
                ErrorMessage(error = error, onDismiss = { viewModel.clearError() })
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        "Shipping Information",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Address Selection Section
                item {
                    AddressSelectionSection(
                        addresses = state.addresses,
                        selectedAddress = state.selectedAddress,
                        onAddressSelected = { viewModel.selectAddress(it) },
                        onAddressEdit = { address ->
                            addressToEdit = address
                            showEditDialog = true
                        },
                        onAddressDelete = { address ->
                            viewModel.deleteAddress(address.id)
                        }
                    )
                }

                // Show helpful message when no addresses
                if (state.addresses.isEmpty() && !state.isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "No addresses",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No addresses found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Please add a shipping address to continue",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Collapsible Address Form
                item {
                    CollapsibleAddressForm(
                        userId = userId,
                        onSaveAddress = { viewModel.saveAddress(it) }
                    )
                }

                // Order Summary Section with actual cart data
                item {
                    OrderSummarySection(
                        cartItems = state.cartItems,
                        subtotal = viewModel.calculateSubtotal(),
                        shipping = viewModel.calculateShipping(),
                        tax = viewModel.calculateTax(),
                        total = viewModel.calculateTotal(),
                        totalItems = viewModel.getTotalItems()
                    )
                }

                // Payment Method Section
                item {
                    PaymentMethodSection()
                }
            }

            // Checkout Button
            Button(
                onClick = {
                    // Generate a unique order ID for payment
                    val paymentOrderId = "ORD_${System.currentTimeMillis()}"
                    navController.navigate("payment/${paymentOrderId}/${viewModel.calculateTotal()}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = state.selectedAddress != null && !state.isLoading && state.cartItems.isNotEmpty()
            ) {
                Text("Place Order - $${String.format("%.2f", viewModel.calculateTotal())}")
            }
        }
    }

    // Edit Address Dialog
    if (showEditDialog && addressToEdit != null) {
        EditAddressDialog(
            address = addressToEdit!!,
            onUpdate = { updatedAddress ->
                viewModel.updateAddress(addressToEdit!!.id, updatedAddress)
                showEditDialog = false
                addressToEdit = null
            },
            onDismiss = {
                showEditDialog = false
                addressToEdit = null
            }
        )
    }
}

@Composable
fun CollapsibleAddressForm(
    userId: String,
    onSaveAddress: (Address) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with expand/collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Add New Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expandable content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                AddressFormContent(
                    userId = userId,
                    onSaveAddress = { address ->
                        onSaveAddress(address)
                        isExpanded = false // Collapse after saving
                    },
                    onCancel = { isExpanded = false }
                )
            }
        }
    }
}

@Composable
fun AddressFormContent(
    userId: String,
    onSaveAddress: (Address) -> Unit,
    onCancel: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val isEmailValid = email.isNotBlank() && email.contains("@")
    val isConfirmEmailValid = confirmEmail.isNotBlank() && confirmEmail == email
    val isFormValid = remember(
        fullName, email, confirmEmail, addressLine1, city, state, postalCode, country, phoneNumber
    ) {
        fullName.isNotBlank() &&
                isEmailValid &&
                isConfirmEmailValid &&
                addressLine1.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                postalCode.isNotBlank() &&
                country.isNotBlank() &&
                phoneNumber.isNotBlank()
    }

    Column {
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = fullName.isBlank()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = email.isNotBlank() && !isEmailValid,
            trailingIcon = {
                if (email.isNotBlank() && !isEmailValid) {
                    Icon(Icons.Default.Error, "Invalid email", tint = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmEmail,
            onValueChange = { confirmEmail = it },
            label = { Text("Confirm Email *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = confirmEmail.isNotBlank() && !isConfirmEmailValid,
            trailingIcon = {
                if (confirmEmail.isNotBlank() && !isConfirmEmailValid) {
                    Icon(Icons.Default.Error, "Emails don't match", tint = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = addressLine1,
            onValueChange = { addressLine1 = it },
            label = { Text("Address Line 1 *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = addressLine2,
            onValueChange = { addressLine2 = it },
            label = { Text("Address Line 2 (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("Postal Code *") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Country *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val newAddress = Address(
                        userId = userId,
                        fullName = fullName,
                        email = email,
                        confirmEmail = confirmEmail,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2,
                        city = city,
                        state = state,
                        postalCode = postalCode,
                        country = country,
                        phoneNumber = phoneNumber
                    )
                    onSaveAddress(newAddress)

                    // Reset form
                    fullName = ""
                    email = ""
                    confirmEmail = ""
                    phoneNumber = ""
                    addressLine1 = ""
                    addressLine2 = ""
                    city = ""
                    state = ""
                    postalCode = ""
                    country = ""
                },
                enabled = isFormValid
            ) {
                Text("Save Address")
            }
        }
    }
}

@Composable
fun AddressSelectionSection(
    addresses: List<Address>,
    selectedAddress: Address?,
    onAddressSelected: (Address) -> Unit,
    onAddressEdit: (Address) -> Unit,
    onAddressDelete: (Address) -> Unit
) {
    Column {
        if (addresses.isNotEmpty()) {
            Text(
                "Select Shipping Address",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            addresses.forEach { address ->
                AddressItem(
                    address = address,
                    isSelected = selectedAddress?.id == address.id,
                    onSelect = { onAddressSelected(address) },
                    onEdit = { onAddressEdit(address) },
                    onDelete = { onAddressDelete(address) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    "Delete Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Are you sure you want to delete this address?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        border = if (isSelected) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary
        ) else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect() }
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        address.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(address.addressLine1, style = MaterialTheme.typography.bodyMedium)
                    if (address.addressLine2.isNotEmpty()) {
                        Text(address.addressLine2, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "${address.city}, ${address.state} ${address.postalCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(address.country, style = MaterialTheme.typography.bodyMedium)
                    Text(address.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                    Text(address.email, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Edit and Delete button row
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Edit button
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit address",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                // Delete button
                TextButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete address",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun EditAddressDialog(
    address: Address,
    onUpdate: (Address) -> Unit,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf(address.fullName) }
    var email by remember { mutableStateOf(address.email) }
    var confirmEmail by remember { mutableStateOf(address.email) }
    var addressLine1 by remember { mutableStateOf(address.addressLine1) }
    var addressLine2 by remember { mutableStateOf(address.addressLine2) }
    var city by remember { mutableStateOf(address.city) }
    var state by remember { mutableStateOf(address.state) }
    var postalCode by remember { mutableStateOf(address.postalCode) }
    var country by remember { mutableStateOf(address.country) }
    var phoneNumber by remember { mutableStateOf(address.phoneNumber) }

    val isEmailValid = email.isNotBlank() && email.contains("@")
    val isConfirmEmailValid = confirmEmail.isNotBlank() && confirmEmail == email
    val isFormValid = remember(
        fullName, email, confirmEmail, addressLine1, city, state, postalCode, country, phoneNumber
    ) {
        fullName.isNotBlank() &&
                isEmailValid &&
                isConfirmEmailValid &&
                addressLine1.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                postalCode.isNotBlank() &&
                country.isNotBlank() &&
                phoneNumber.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Address",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = fullName.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = email.isNotBlank() && !isEmailValid,
                    trailingIcon = {
                        if (email.isNotBlank() && !isEmailValid) {
                            Icon(Icons.Default.Error, "Invalid email", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmEmail,
                    onValueChange = { confirmEmail = it },
                    label = { Text("Confirm Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = confirmEmail.isNotBlank() && !isConfirmEmailValid,
                    trailingIcon = {
                        if (confirmEmail.isNotBlank() && !isConfirmEmailValid) {
                            Icon(Icons.Default.Error, "Emails don't match", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Address Line 1 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2 (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it },
                        label = { Text("Postal Code *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedAddress = address.copy(
                        fullName = fullName,
                        email = email,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2,
                        city = city,
                        state = state,
                        postalCode = postalCode,
                        country = country,
                        phoneNumber = phoneNumber
                    )
                    onUpdate(updatedAddress)
                },
                enabled = isFormValid
            ) {
                Text("Update Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Remove the old AddressForm composable since we're using CollapsibleAddressForm now

@Composable
fun OrderSummarySection(
    cartItems: List<com.example.towdepo.data.CartItem>,
    subtotal: Double,
    shipping: Double,
    tax: Double,
    total: Double,
    totalItems: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Order Summary ($totalItems items)",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Show actual cart items
            if (cartItems.isNotEmpty()) {
                cartItems.forEach { item ->
                    OrderItemRow(cartItem = item)
                }
            } else {
                Text(
                    "Your cart is empty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Pricing breakdown
            PriceRow("Subtotal:", "$${String.format("%.2f", subtotal)}")
            PriceRow("Shipping:", if (shipping == 0.0) "FREE" else "$${String.format("%.2f", shipping)}")
            PriceRow("Tax:", "$${String.format("%.2f", tax)}")

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            PriceRow(
                "Total:",
                "$${String.format("%.2f", total)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            // Free shipping message
            if (shipping == 0.0) {
                Text(
                    "🎉 You've qualified for free shipping!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OrderItemRow(cartItem: com.example.towdepo.data.CartItem) {
    val discountedPrice = calculateDiscountedPrice(cartItem.mrp, cartItem.discount)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cartItem.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Qty: ${cartItem.count}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "$${String.format("%.2f", discountedPrice * cartItem.count)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PriceRow(
    label: String,
    value: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = style, fontWeight = fontWeight)
        Text(value, style = style, fontWeight = fontWeight)
    }
}

@Composable
fun PaymentMethodSection() {
    var selectedPaymentMethod by remember { mutableStateOf("Credit Card") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Payment Method",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            listOf("Credit Card", "Debit Card", "PayPal", "Cash on Delivery").forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPaymentMethod = method }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == method,
                        onClick = { selectedPaymentMethod = method }
                    )
                    Text(
                        method,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                error,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

// Helper function
private fun calculateDiscountedPrice(mrp: Double, discount: String): Double {
    val discountValue = discount.toDoubleOrNull() ?: 0.0
    return mrp - (mrp * discountValue / 100)
}