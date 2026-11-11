package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.CategoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierListingDialog(
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        categoryId: String,
        subcategory: String,
        originCountry: String,
        cityState: String,
        yearEstablished: Int,
        moq: Int,
        moqUnit: String,
        avgLeadTimeDays: Int,
        about: String,
        email: String,
        phone: String,
        productTitle: String?,
        productPrice: Double?
    ) -> Unit
) {
    var factoryTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: categories[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategory by remember { mutableStateOf(selectedCategory.subcategories.firstOrNull() ?: "General Wholesale") }
    var country by remember { mutableStateOf("United States") }
    var cityState by remember { mutableStateOf("Austin, Texas") }
    var yearEstablishedText by remember { mutableStateOf("2018") }
    var moqText by remember { mutableStateOf("50") }
    var moqUnit by remember { mutableStateOf("units") }
    var leadTimeText by remember { mutableStateOf("10") }
    var aboutText by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var productTitle by remember { mutableStateOf("") }
    var productPriceText by remember { mutableStateOf("25.00") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("supplier_listing_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "List Wholesale Supplier",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Register factory or distributor into directory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = factoryTitle,
                    onValueChange = { factoryTitle = it },
                    label = { Text("Company / Factory Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    subcategory = cat.subcategories.firstOrNull() ?: "General"
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subcategory,
                    onValueChange = { subcategory = it },
                    label = { Text("Specialty / Subcategory *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cityState,
                        onValueChange = { cityState = it },
                        label = { Text("City / Region *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = moqText,
                        onValueChange = { moqText = it },
                        label = { Text("Min Order (MOQ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = leadTimeText,
                        onValueChange = { leadTimeText = it },
                        label = { Text("Lead Time (days) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = aboutText,
                    onValueChange = { aboutText = it },
                    label = { Text("About Factory & QC Capabilities *") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("B2B Sales Email *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Phone / WhatsApp") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Flagship Product (Optional):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = productTitle,
                        onValueChange = { productTitle = it },
                        label = { Text("Product Title") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = productPriceText,
                        onValueChange = { productPriceText = it },
                        label = { Text("Unit Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (factoryTitle.isBlank()) {
                            errorText = "Please enter company / factory name."
                            return@Button
                        }
                        if (contactEmail.isBlank()) {
                            errorText = "Please enter contact email."
                            return@Button
                        }
                        val moq = moqText.toIntOrNull() ?: 50
                        val leadDays = leadTimeText.toIntOrNull() ?: 10
                        val price = productPriceText.toDoubleOrNull()

                        onSubmit(
                            factoryTitle,
                            selectedCategory.id,
                            subcategory,
                            country,
                            cityState,
                            yearEstablishedText.toIntOrNull() ?: 2020,
                            moq,
                            moqUnit,
                            leadDays,
                            if (aboutText.isBlank()) "Verified certified wholesale manufacturer and exporter." else aboutText,
                            contactEmail,
                            if (contactPhone.isBlank()) "+1 (800) 000-0000" else contactPhone,
                            if (productTitle.isBlank()) null else productTitle,
                            price
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_supplier_listing_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Domain, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Publish Supplier Listing", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
