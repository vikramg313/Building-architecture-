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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WholesaleSupplier
import com.example.ui.components.RfqDialog
import com.example.ui.components.SupplierCard
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold
import com.example.viewmodel.WholesaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSuppliersScreen(
    viewModel: WholesaleViewModel,
    modifier: Modifier = Modifier
) {
    val bookmarkedSuppliers by viewModel.bookmarkedSuppliers.collectAsState()
    val comparisonSuppliers by viewModel.comparisonSuppliers.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    var isCompareMode by remember { mutableStateOf(false) }
    var rfqSupplierTarget by remember { mutableStateOf<WholesaleSupplier?>(null) }

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
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Saved & Shortlist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${bookmarkedSuppliers.size} Bookmarked Suppliers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (comparisonSuppliers.size >= 2) {
                        FilledTonalButton(
                            onClick = { isCompareMode = !isCompareMode },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isCompareMode) "Exit Compare" else "Compare (${comparisonSuppliers.size})", fontSize = 12.sp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Comparison Matrix View if active
            if (isCompareMode && comparisonSuppliers.size >= 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Factory Comparison Matrix",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { isCompareMode = false }) {
                                    Text("Close")
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scrollable matrix table
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                // Factory names row
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Criteria", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text(sup.name, modifier = Modifier.width(180.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                                HorizontalDivider()

                                // Rating
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Rating", modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text("${sup.rating} ★ (${sup.reviewCount})", modifier = Modifier.width(180.dp), color = TradeGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                                HorizontalDivider()

                                // MOQ
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Min Order", modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text("${sup.moq} ${sup.moqUnit}", modifier = Modifier.width(180.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                HorizontalDivider()

                                // Lead time
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Lead Time", modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text("${sup.avgLeadTimeDays} days", modifier = Modifier.width(180.dp))
                                    }
                                }
                                HorizontalDivider()

                                // Origin
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Location", modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text("${sup.originCountry}", modifier = Modifier.width(180.dp))
                                    }
                                }
                                HorizontalDivider()

                                // Response
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Response", modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text("${sup.responseRatePercent}%", modifier = Modifier.width(180.dp), color = TradeEmerald, fontWeight = FontWeight.Bold)
                                    }
                                }
                                HorizontalDivider()

                                // Sample Price
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Sample Cost", modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    comparisonSuppliers.forEach { sup ->
                                        Text("$${String.format("%.2f", sup.samplePrice)}", modifier = Modifier.width(180.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (bookmarkedSuppliers.isEmpty()) {
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
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Saved Suppliers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bookmark your favorite suppliers while browsing the directory to compare specifications side-by-side.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(bookmarkedSuppliers, key = { it.id }) { supplier ->
                    SupplierCard(
                        supplier = supplier,
                        currency = selectedCurrency,
                        onViewSupplier = { viewModel.openSupplierDetail(supplier) },
                        onRequestRfq = { rfqSupplierTarget = supplier },
                        onToggleBookmark = { viewModel.toggleBookmark(supplier) }
                    )
                }
            }
        }
    }

    rfqSupplierTarget?.let { supplier ->
        RfqDialog(
            supplier = supplier,
            prefilledBuyerName = userAccount.fullName,
            prefilledBuyerEmail = userAccount.email,
            prefilledDestination = userAccount.shippingDestination,
            onDismiss = { rfqSupplierTarget = null },
            onSubmit = { supplierId, supplierName, productName, categoryName, targetQuantity, targetPrice, unit, notes, customLogo, customPackaging, shippingDestination, buyerName, buyerEmail ->
                viewModel.submitRfq(
                    supplierId,
                    supplierName,
                    productName,
                    categoryName,
                    targetQuantity,
                    targetPrice,
                    unit,
                    notes,
                    customLogo,
                    customPackaging,
                    shippingDestination,
                    buyerName,
                    buyerEmail
                )
            }
        )
    }
}
