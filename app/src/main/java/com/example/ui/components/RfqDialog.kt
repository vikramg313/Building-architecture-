package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.WholesaleSupplier
import com.example.ui.theme.TradeEmerald
import com.example.util.SupplierContactHelper

@Composable
fun RfqDialog(
    supplier: WholesaleSupplier,
    prefilledProductName: String? = null,
    prefilledQty: Int? = null,
    prefilledTargetPrice: Double? = null,
    prefilledBuyerName: String? = null,
    prefilledBuyerEmail: String? = null,
    prefilledDestination: String? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        supplierId: String,
        supplierName: String,
        productName: String,
        categoryName: String,
        targetQuantity: Int,
        targetPrice: Double,
        unit: String,
        notes: String,
        customLogo: Boolean,
        customPackaging: Boolean,
        shippingDestination: String,
        buyerName: String,
        buyerEmail: String
    ) -> Unit
) {
    var productName by remember { mutableStateOf(prefilledProductName ?: supplier.products.firstOrNull()?.title ?: "${supplier.subcategory} Bulk Order") }
    var quantityText by remember { mutableStateOf((prefilledQty ?: supplier.moq).toString()) }
    var targetPriceText by remember { mutableStateOf(prefilledTargetPrice?.let { String.format("%.2f", it) } ?: "15.00") }
    var destination by remember { mutableStateOf(prefilledDestination ?: "Los Angeles Port, CA, USA") }
    var customLogo by remember { mutableStateOf(true) }
    var customPackaging by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var notes by remember { mutableStateOf("Please provide CFR/DDP ocean freight quote, sample lead time, and payment terms for first trial order.") }
    var buyerName by remember { mutableStateOf(prefilledBuyerName ?: "Global Sourcing Director") }
    var buyerEmail by remember { mutableStateOf(prefilledBuyerEmail ?: "procure@globaltrader.com") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("rfq_dialog")
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
                            text = "Submit Official RFQ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "To: ${supplier.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct Factory Contact Quick Access
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Direct Supplier Contact",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${supplier.email} • ${supplier.phone}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val (subj, body) = SupplierContactHelper.buildRfqEmailContent(
                                        supplierName = supplier.name,
                                        productName = productName,
                                        quantity = quantityText.toIntOrNull() ?: supplier.moq,
                                        unit = supplier.moqUnit,
                                        targetPrice = targetPriceText.toDoubleOrNull() ?: 15.0,
                                        destination = destination,
                                        customLogo = customLogo,
                                        customPackaging = customPackaging,
                                        notes = notes,
                                        buyerName = buyerName,
                                        buyerEmail = buyerEmail
                                    )
                                    SupplierContactHelper.launchEmail(context, supplier.email, subj, body)
                                },
                                modifier = Modifier.size(32.dp).testTag("dialog_quick_email")
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "Email Factory", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { SupplierContactHelper.launchPhone(context, supplier.phone) },
                                modifier = Modifier.size(32.dp).testTag("dialog_quick_call")
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call Factory", tint = TradeEmerald, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = {
                                    val msg = SupplierContactHelper.buildQuickWhatsAppMessage(
                                        supplierName = supplier.name,
                                        productName = productName,
                                        quantity = quantityText.toIntOrNull() ?: supplier.moq,
                                        unit = supplier.moqUnit,
                                        buyerName = buyerName
                                    )
                                    SupplierContactHelper.launchWhatsApp(context, supplier.phone, msg)
                                },
                                modifier = Modifier.size(32.dp).testTag("dialog_quick_whatsapp")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = TradeEmerald, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Name Field
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product / Inquiry Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Qty & Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Order Quantity (${supplier.moqUnit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = targetPriceText,
                        onValueChange = { targetPriceText = it },
                        label = { Text("Target USD / Unit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Destination
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination Port / Delivery City") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Customization Checkboxes
                Text(
                    text = "Customization Requirements:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = customLogo, onCheckedChange = { customLogo = it })
                    Text(text = "Custom Logo / Private Label", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = customPackaging, onCheckedChange = { customPackaging = it })
                    Text(text = "Custom Retail Box / Barcoding", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Detailed specs notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Specifications, Certifications & Notes") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Buyer Contact info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text("Buyer Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = buyerEmail,
                        onValueChange = { buyerEmail = it },
                        label = { Text("Work Email") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Direct Action: Submit & Send Email Direct to Factory
                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull()
                        val price = targetPriceText.toDoubleOrNull()
                        if (qty == null || qty <= 0) {
                            errorMessage = "Please enter a valid positive quantity."
                            return@Button
                        }
                        if (price == null || price <= 0) {
                            errorMessage = "Please enter a valid target price."
                            return@Button
                        }
                        onSubmit(
                            supplier.id,
                            supplier.name,
                            productName,
                            supplier.subcategory,
                            qty,
                            price,
                            supplier.moqUnit,
                            notes,
                            customLogo,
                            customPackaging,
                            destination,
                            buyerName,
                            buyerEmail
                        )
                        // Launch email directly
                        val (subj, body) = SupplierContactHelper.buildRfqEmailContent(
                            supplierName = supplier.name,
                            productName = productName,
                            quantity = qty,
                            unit = supplier.moqUnit,
                            targetPrice = price,
                            destination = destination,
                            customLogo = customLogo,
                            customPackaging = customPackaging,
                            notes = notes,
                            buyerName = buyerName,
                            buyerEmail = buyerEmail
                        )
                        SupplierContactHelper.launchEmail(context, supplier.email, subj, body)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_rfq_email_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Submit & Direct Email Factory", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Direct Action: Submit & Direct WhatsApp
                FilledTonalButton(
                    onClick = {
                        val qty = quantityText.toIntOrNull()
                        val price = targetPriceText.toDoubleOrNull()
                        if (qty == null || qty <= 0) {
                            errorMessage = "Please enter a valid positive quantity."
                            return@FilledTonalButton
                        }
                        if (price == null || price <= 0) {
                            errorMessage = "Please enter a valid target price."
                            return@FilledTonalButton
                        }
                        onSubmit(
                            supplier.id,
                            supplier.name,
                            productName,
                            supplier.subcategory,
                            qty,
                            price,
                            supplier.moqUnit,
                            notes,
                            customLogo,
                            customPackaging,
                            destination,
                            buyerName,
                            buyerEmail
                        )
                        val msg = SupplierContactHelper.buildQuickWhatsAppMessage(
                            supplierName = supplier.name,
                            productName = productName,
                            quantity = qty,
                            unit = supplier.moqUnit,
                            buyerName = buyerName
                        )
                        SupplierContactHelper.launchWhatsApp(context, supplier.phone, msg)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_rfq_whatsapp_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Submit & WhatsApp Sourcing Desk", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tertiary: Save locally only
                TextButton(
                    onClick = {
                        val qty = quantityText.toIntOrNull()
                        val price = targetPriceText.toDoubleOrNull()
                        if (qty == null || qty <= 0) {
                            errorMessage = "Please enter a valid positive quantity."
                            return@TextButton
                        }
                        if (price == null || price <= 0) {
                            errorMessage = "Please enter a valid target price."
                            return@TextButton
                        }
                        onSubmit(
                            supplier.id,
                            supplier.name,
                            productName,
                            supplier.subcategory,
                            qty,
                            price,
                            supplier.moqUnit,
                            notes,
                            customLogo,
                            customPackaging,
                            destination,
                            buyerName,
                            buyerEmail
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_rfq_btn")
                ) {
                    Text(text = "Save RFQ to Directory Inquiries Only", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
