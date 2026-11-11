package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RfqItem
import com.example.model.WholesaleSupplier
import com.example.ui.theme.TradeBlue
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold
import com.example.util.SupplierContactHelper
import com.example.viewmodel.WholesaleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfqManagerScreen(
    viewModel: WholesaleViewModel,
    modifier: Modifier = Modifier
) {
    val rfqs by viewModel.allRfqs.collectAsState()
    val allSuppliers by viewModel.allSuppliers.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val filteredRfqs = remember(rfqs, selectedStatusFilter) {
        if (selectedStatusFilter == "All") rfqs else rfqs.filter { it.status == selectedStatusFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Buyer RFQ Inquiries",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Track quotation requests and supplier offers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Pending Quote", "Quoted", "Sample Dispatched").forEach { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text(status) }
                        )
                    }
                }
            }

            if (filteredRfqs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No RFQ Inquiries Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Browse verified suppliers in any category and tap 'Get RFQ' to request wholesale bulk pricing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredRfqs, key = { it.id }) { rfq ->
                    val matchingSupplier = allSuppliers.find { it.id == rfq.supplierId }
                    RfqItemCard(
                        rfq = rfq,
                        supplier = matchingSupplier,
                        onUpdateStatus = { newStatus -> viewModel.updateRfqStatus(rfq.id, newStatus) },
                        onDelete = { viewModel.deleteRfq(rfq.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RfqItemCard(
    rfq: RfqItem,
    supplier: WholesaleSupplier?,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateString = remember(rfq.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(rfq.timestamp))
    }

    val (badgeColor, statusBg) = when (rfq.status) {
        "Quoted" -> TradeEmerald to TradeEmerald.copy(alpha = 0.12f)
        "Sample Dispatched" -> TradeBlue to TradeBlue.copy(alpha = 0.12f)
        else -> TradeGold to TradeGold.copy(alpha = 0.12f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("rfq_card_${rfq.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Supplier Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rfq.supplierName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Submitted $dateString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = rfq.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product Name
            Text(
                text = rfq.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Order Specs Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${rfq.targetQuantity} ${rfq.unit}", fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Target Unit Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%.2f", rfq.targetPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Target Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%,.2f", rfq.targetQuantity * rfq.targetPrice)}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customization tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rfq.customLogo) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Custom Logo", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.BrandingWatermark, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
                if (rfq.customPackaging) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Custom Packaging", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Shipping destination
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ship to: ${rfq.shippingDestination}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Supplier quote feedback if available
            if (rfq.quotedPrice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TradeEmerald.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, TradeEmerald.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Supplier Quoted: $${String.format("%.2f", rfq.quotedPrice)} / unit",
                                fontWeight = FontWeight.Bold,
                                color = TradeEmerald
                            )
                        }
                        if (!rfq.supplierNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rfq.supplierNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Direct Factory Communication Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Direct Factory Desk",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${supplier?.email ?: "export@factory.com"} • ${supplier?.phone ?: "+1 (800) 555-0199"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val email = supplier?.email ?: "export@factory.com"
                        val phone = supplier?.phone ?: "+1 (800) 555-0199"

                        // Email direct
                        FilledTonalButton(
                            onClick = {
                                val (subj, body) = SupplierContactHelper.buildRfqEmailContent(
                                    supplierName = rfq.supplierName,
                                    productName = rfq.productName,
                                    quantity = rfq.targetQuantity,
                                    unit = rfq.unit,
                                    targetPrice = rfq.targetPrice,
                                    destination = rfq.shippingDestination,
                                    customLogo = rfq.customLogo,
                                    customPackaging = rfq.customPackaging,
                                    notes = rfq.notes,
                                    buyerName = rfq.buyerName,
                                    buyerEmail = rfq.buyerEmail
                                )
                                SupplierContactHelper.launchEmail(context, email, subj, body)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Email", fontSize = 11.sp)
                        }

                        // Call direct
                        FilledTonalButton(
                            onClick = { SupplierContactHelper.launchPhone(context, phone) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Call", fontSize = 11.sp)
                        }

                        // WhatsApp direct
                        IconButton(
                            onClick = {
                                val msg = SupplierContactHelper.buildQuickWhatsAppMessage(
                                    supplierName = rfq.supplierName,
                                    productName = rfq.productName,
                                    quantity = rfq.targetQuantity,
                                    unit = rfq.unit,
                                    buyerName = rfq.buyerName
                                )
                                SupplierContactHelper.launchWhatsApp(context, phone, msg)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = TradeEmerald, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (rfq.status == "Pending Quote") {
                        OutlinedButton(
                            onClick = { onUpdateStatus("Quoted") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Simulate Factory Quote", fontSize = 12.sp)
                        }
                    } else if (rfq.status == "Quoted") {
                        Button(
                            onClick = { onUpdateStatus("Sample Dispatched") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Order Physical Sample", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
