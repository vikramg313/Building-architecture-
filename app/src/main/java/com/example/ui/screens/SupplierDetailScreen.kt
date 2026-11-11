package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyMode
import com.example.model.GlobalCountries
import com.example.model.GlobalCurrencies
import com.example.model.Supplier
import com.example.model.UserRole
import com.example.model.WholesaleProduct
import com.example.ui.components.ProductTierPricingCard
import com.example.ui.components.RfqDialog
import com.example.ui.components.SupplierProfileEditorDialog
import com.example.ui.theme.TradeBlue
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold
import com.example.util.SupplierContactHelper
import com.example.viewmodel.WholesaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(
    supplier: Supplier,
    viewModel: WholesaleViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRfqDialog by remember { mutableStateOf(false) }
    var selectedProductForRfq by remember { mutableStateOf<WholesaleProduct?>(null) }
    var prefilledQty by remember { mutableStateOf<Int?>(null) }
    var prefilledPrice by remember { mutableStateOf<Double?>(null) }

    val comparisonIds by viewModel.comparisonSupplierIds.collectAsState()
    val isCompared = supplier.id in comparisonIds
    val userAccount by viewModel.userAccount.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    var showProfileEditor by remember { mutableStateOf(false) }

    val isProfileOwner = userAccount.role == UserRole.VERIFIED_SUPPLIER ||
            userAccount.supplierProfileId == supplier.id ||
            userAccount.email.equals(supplier.email, ignoreCase = true) ||
            userAccount.companyName.equals(supplier.name, ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = supplier.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isProfileOwner) {
                        IconButton(
                            onClick = { showProfileEditor = true },
                            modifier = Modifier.testTag("edit_own_supplier_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleComparison(supplier.id) }) {
                        Icon(
                            imageVector = if (isCompared) Icons.AutoMirrored.Filled.CompareArrows else Icons.Default.AddChart,
                            contentDescription = "Compare",
                            tint = if (isCompared) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleBookmark(supplier) }) {
                        Icon(
                            imageVector = if (supplier.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (supplier.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Direct Email Sales
                    OutlinedButton(
                        onClick = {
                            val (subj, body) = SupplierContactHelper.buildRfqEmailContent(
                                supplierName = supplier.name,
                                productName = supplier.products.firstOrNull()?.title ?: "${supplier.subcategory} Sourcing",
                                quantity = supplier.moq,
                                unit = supplier.moqUnit,
                                targetPrice = supplier.products.firstOrNull()?.priceTiers?.firstOrNull()?.unitPrice ?: 20.0,
                                destination = userAccount.shippingDestination,
                                customLogo = true,
                                customPackaging = true,
                                notes = "Please send wholesale catalog, lead time, and MOQ tier details.",
                                buyerName = userAccount.fullName,
                                buyerEmail = userAccount.email,
                                currencySymbol = selectedCurrency.symbol
                            )
                            SupplierContactHelper.launchEmail(context, supplier.contact.email, subj, body)
                        },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email", fontSize = 12.sp)
                    }

                    // Direct Phone / Call
                    OutlinedButton(
                        onClick = { SupplierContactHelper.launchPhone(context, supplier.contact.phone) },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    // Send Formal RFQ
                    Button(
                        onClick = {
                            selectedProductForRfq = supplier.products.firstOrNull()
                            prefilledQty = supplier.moq
                            prefilledPrice = null
                            showRfqDialog = true
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("request_rfq_main_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.RequestQuote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send RFQ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier Hero Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Badges Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (supplier.verification.isVerified) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TradeEmerald.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, TradeEmerald.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("VERIFIED FACTORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TradeEmerald)
                                    }
                                }
                            }
                            if (supplier.verification.isGoldSupplier) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TradeGold.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, TradeGold.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Stars, contentDescription = null, tint = TradeGold, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("GOLD SUPPLIER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TradeGold)
                                    }
                                }
                            }
                            if (supplier.verification.hasTradeAssurance) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TradeBlue.copy(alpha = 0.12f),
                                    border = BorderStroke(0.5.dp, TradeBlue.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = TradeBlue, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("TRADE ASSURED", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TradeBlue)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = GlobalCountries.getFlagForCountry(supplier.originCountry),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${supplier.cityState}, ${supplier.originCountry}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("•", color = MaterialTheme.colorScheme.outline)
                            Text("Est. ${supplier.yearEstablished}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dossier Stats Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = TradeGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${String.format("%.2f", supplier.rating)} (${supplier.reviewCount})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("Buyer Rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${supplier.moq} ${supplier.moqUnit}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Min. Order (MOQ)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${supplier.avgLeadTimeDays} Days",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Avg. Lead Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${supplier.responseRatePercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TradeEmerald
                                )
                                Text("Response Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Factory Quick Specs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Factory Floor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(supplier.factorySize, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Staff Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(supplier.staffCount, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Annual Export", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(supplier.annualExport, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Catalog & Pricing (${supplier.products.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Verification & QC") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Contact & Logistics") }
                    )
                }
            }

            // Tab 0: Products with Bulk Tier Calculator
            if (selectedTab == 0) {
                if (supplier.products.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Custom Catalog Available On Demand", fontWeight = FontWeight.Bold)
                                Text(
                                    "This manufacturer produces custom OEM/ODM orders per technical drawing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { showRfqDialog = true }) {
                                    Text("Request Custom Product Catalog / RFQ")
                                }
                            }
                        }
                    }
                } else {
                    items(supplier.products, key = { it.id }) { product ->
                        ProductTierPricingCard(
                            product = product,
                            onCalculate = { prod, qty -> viewModel.calculateTierCost(prod, qty, selectedCurrency) },
                            onRequestQuoteForProduct = { prod, qty, targetPrice ->
                                selectedProductForRfq = prod
                                prefilledQty = qty
                                prefilledPrice = targetPrice
                                showRfqDialog = true
                            },
                            currency = selectedCurrency,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Tab 1: Verification Status & Quality Control
            if (selectedTab == 1) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Official Verification Status Dossier Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, TradeEmerald.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Supplier Verification Dossier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TradeEmerald.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Independent On-Site Audit Passed",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TradeEmerald
                                            )
                                            Text(
                                                text = "Audited by ${supplier.verification.auditAgency} (${supplier.verification.onSiteInspectionDate})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                SupplierDetailRow("Audit Report ID", supplier.verification.auditReportNumber)
                                SupplierDetailRow("Business License", supplier.verification.businessLicenseNumber)
                                SupplierDetailRow("Trade Assurance Guarantee", supplier.verification.tradeAssuranceCoverage)
                                SupplierDetailRow("Verification Level", "Grade A Verified Manufacturer")
                            }
                        }

                        // Factory Profile
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Factory Profile & Background", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(supplier.about, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                            }
                        }

                        // Audited Certifications
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = TradeGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Audited Industry Certifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    supplier.certifications.forEach { cert ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = TradeEmerald.copy(alpha = 0.12f),
                                            border = BorderStroke(0.5.dp, TradeEmerald.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(cert, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TradeEmerald)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Customization & Engineering Capabilities
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("OEM/ODM & Customization Capabilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                supplier.customization.forEach { opt ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Contact Details & Direct Logistics
            if (selectedTab == 2) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Contact Details Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Direct Factory Contact Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = TradeEmerald.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = supplier.contact.typicalResponseTime,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TradeEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Contact Person
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(supplier.contact.contactPerson, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Wholesale Export Desk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                // Email Action Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Sales Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(supplier.contact.email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:${supplier.contact.email}")
                                                putExtra(Intent.EXTRA_SUBJECT, "Trade Inquiry: ${supplier.name}")
                                            }
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Compose", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Phone / WhatsApp Action Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Direct Phone / WhatsApp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(supplier.contact.phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${supplier.contact.phone}")
                                            }
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Call", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Website
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = TradeBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Official Website", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(supplier.contact.website, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(supplier.contact.website))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Visit", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Operating Hours & Physical Location
                                SupplierDetailRow("Operating Hours", supplier.contact.operatingHours)
                                SupplierDetailRow("Factory Address", supplier.contact.fullAddress)
                            }
                        }

                        // Payment Terms
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Payment Terms & Sourcing Protection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                supplier.paymentTerms.forEach { term ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(term, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        // Sample Policy Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Sample Evaluation Policy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Physical sample available for ${selectedCurrency.format(supplier.samplePrice)} (100% refundable against first production commercial order). Dispatched in 3-5 days via DHL / FedEx Express.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        selectedProductForRfq = supplier.products.firstOrNull()
                                        prefilledQty = 1
                                        prefilledPrice = supplier.samplePrice
                                        showRfqDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Order Evaluation Sample (${selectedCurrency.format(supplier.samplePrice)})")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRfqDialog) {
        RfqDialog(
            supplier = supplier,
            prefilledProductName = selectedProductForRfq?.title,
            prefilledQty = prefilledQty,
            prefilledTargetPrice = prefilledPrice,
            prefilledBuyerName = userAccount.fullName,
            prefilledBuyerEmail = userAccount.email,
            prefilledDestination = userAccount.shippingDestination,
            onDismiss = { showRfqDialog = false },
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

    if (showProfileEditor) {
        SupplierProfileEditorDialog(
            initialSupplier = supplier,
            categories = viewModel.categories,
            defaultCompanyName = userAccount.companyName,
            defaultEmail = userAccount.email,
            defaultPhone = userAccount.phone,
            onDismiss = { showProfileEditor = false },
            onSaveSupplier = { updatedSupplier ->
                viewModel.updateSupplierProfile(updatedSupplier)
                viewModel.openSupplierDetail(updatedSupplier)
            }
        )
    }
}

@Composable
fun SupplierDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
