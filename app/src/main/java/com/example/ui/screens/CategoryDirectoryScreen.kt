package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppThemeMode
import com.example.model.CurrencyMode
import com.example.model.DirectorySortOption
import com.example.model.GlobalCountries
import com.example.model.GlobalCurrencies
import com.example.model.WholesaleSupplier
import com.example.ui.components.CategoryFilterPill
import com.example.ui.components.RfqDialog
import com.example.ui.components.SubscriptionPaywallDialog
import com.example.ui.components.SupplierCard
import com.example.ui.components.SupplierListingDialog
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold
import com.example.viewmodel.WholesaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDirectoryScreen(
    viewModel: WholesaleViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.filteredSuppliers.collectAsState()
    val allSuppliers by viewModel.allSuppliers.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val categories = viewModel.categories

    var showRfqDialogForSupplier by remember { mutableStateOf<WholesaleSupplier?>(null) }
    var showListingDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSubscriptionPaywall by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val activeCategory = categories.find { it.id == filterState.selectedCategoryId }

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
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TradeSource",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Global B2B Wholesale Directory",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Currency Mode Quick Switcher Pill
                    Surface(
                        onClick = { showCurrencyDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.testTag("currency_mode_top_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedCurrency.flag, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedCurrency.code} ${selectedCurrency.symbol}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Appearance Theme Mode Button
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("theme_mode_top_btn")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.NAVY -> Icons.Default.Waves
                                AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            },
                            contentDescription = "Appearance Theme: ${themeMode.displayName}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showListingDialog = true },
                        modifier = Modifier.testTag("add_supplier_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBusiness,
                            contentDescription = "List Your Factory",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(com.example.viewmodel.AppScreen.AccountSettings) },
                        modifier = Modifier.testTag("account_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account & Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner with Stats
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_wholesale_hero),
                        contentDescription = "Wholesale Logistics Hub",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x770B132B),
                                        Color(0xE60A0F1D)
                                    )
                                )
                            )
                    )

                    // Overlay Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = "B2B FACTORY DIRECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = TradeGold.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = TradeGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Trade Assurance 100%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TradeGold
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Source Verified Manufacturers",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Direct access to ISO certified suppliers across 8 wholesale categories with low MOQs and tier discounts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                maxLines = 2
                            )
                        }

                        // Stats counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• 8 Categories",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• 24+ Global Factories",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Direct RFQs & Samples",
                                style = MaterialTheme.typography.labelSmall,
                                color = TradeEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Annual Subscription $1 Promotion Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("subscription_promo_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userAccount.isSubscribed) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (userAccount.isSubscribed) TradeGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (userAccount.isSubscribed) TradeGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (userAccount.isSubscribed) Icons.Default.WorkspacePremium else Icons.Default.Diamond,
                                    contentDescription = null,
                                    tint = if (userAccount.isSubscribed) TradeGold else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (userAccount.isSubscribed) "Pro Pass Active" else "TradeSource Pro Pass",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (userAccount.isSubscribed) TradeEmerald.copy(alpha = 0.15f) else TradeGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (userAccount.isSubscribed) "1 YEAR ACCESS" else "${selectedCurrency.format(1.0)} / YEAR",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (userAccount.isSubscribed) TradeEmerald else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (userAccount.isSubscribed) "Valid until ${userAccount.subscriptionExpiryDate.ifBlank { "1 Year" }} • Unlimited factory contacts" else "Direct email, phone & WhatsApp to all 24+ factories worldwide for $1/yr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        FilledTonalButton(
                            onClick = { showSubscriptionPaywall = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("subscribe_banner_btn")
                        ) {
                            Text(
                                text = if (userAccount.isSubscribed) "View Pass" else "Get $1 Pass",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("directory_search_input"),
                    placeholder = { Text("Search suppliers, materials, products, ISO...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Categories Horizontal Pill Bar
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Browse by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (filterState.selectedCategoryId != "all") {
                            TextButton(
                                onClick = { viewModel.selectCategoryId("all") },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Show All Categories")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CategoryFilterPill(
                                title = "All Categories",
                                icon = Icons.Default.GridView,
                                isSelected = filterState.selectedCategoryId == "all",
                                count = 24,
                                onClick = { viewModel.selectCategoryId("all") }
                            )
                        }

                        items(categories) { cat ->
                            CategoryFilterPill(
                                title = cat.shortName,
                                icon = getCategoryIcon(cat.id),
                                isSelected = filterState.selectedCategoryId == cat.id,
                                count = cat.supplierCount,
                                onClick = { viewModel.selectCategoryId(cat.id) }
                            )
                        }
                    }
                }
            }

            // Origin Country Hubs
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Global Country Hubs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "21 Hubs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (filterState.selectedCountry != "all") {
                            TextButton(
                                onClick = { viewModel.selectCountry("all") },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Clear (${filterState.selectedCountry})")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isSelected = filterState.selectedCountry == "all"
                            Surface(
                                onClick = { viewModel.selectCountry("all") },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🌐", fontSize = 13.sp)
                                    Text(
                                        text = "All Countries",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        items(GlobalCountries.allCountries) { country ->
                            val isSelected = filterState.selectedCountry.equals(country.name, ignoreCase = true)
                            val count = allSuppliers.count { it.originCountry.equals(country.name, ignoreCase = true) }
                            Surface(
                                onClick = { viewModel.selectCountry(country.name) },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = country.flag, fontSize = 14.sp)
                                    Text(
                                        text = country.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (count > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Subcategories chips if a category is selected
            if (activeCategory != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeCategory.name} Subcategories",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${activeCategory.supplierCount} Verified Factories",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            InputChip(
                                selected = filterState.selectedSubcategory == "all",
                                onClick = { viewModel.selectSubcategory("all") },
                                label = { Text("All (${activeCategory.supplierCount})") }
                            )
                            activeCategory.subcategories.forEach { sub ->
                                InputChip(
                                    selected = filterState.selectedSubcategory == sub,
                                    onClick = { viewModel.selectSubcategory(sub) },
                                    label = { Text(sub) }
                                )
                            }
                        }
                    }
                }
            }

            // Filter Toggles & Sort Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort dropdown button
                    Box {
                        OutlinedButton(
                            onClick = { sortMenuExpanded = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = filterState.sortOption.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DirectorySortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        sortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (filterState.sortOption == option) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    FilterChip(
                        selected = filterState.verifiedOnly,
                        onClick = { viewModel.toggleVerifiedOnly() },
                        label = { Text("Verified Only") },
                        leadingIcon = {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = TradeEmerald)
                        }
                    )

                    FilterChip(
                        selected = filterState.tradeAssuranceOnly,
                        onClick = { viewModel.toggleTradeAssuranceOnly() },
                        label = { Text("Trade Assurance") },
                        leadingIcon = {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )

                    FilterChip(
                        selected = filterState.lowMoqOnly,
                        onClick = { viewModel.toggleLowMoqOnly() },
                        label = { Text("Low MOQ (≤50)") }
                    )

                    FilterChip(
                        selected = filterState.fastLeadTimeOnly,
                        onClick = { viewModel.toggleFastLeadTimeOnly() },
                        label = { Text("Fast Lead (≤10d)") }
                    )
                }
            }

            // Results Counter Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wholesale Suppliers (${suppliers.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val isFiltered = filterState.searchQuery.isNotBlank() ||
                            filterState.selectedCategoryId != "all" ||
                            filterState.verifiedOnly ||
                            filterState.tradeAssuranceOnly ||
                            filterState.lowMoqOnly ||
                            filterState.fastLeadTimeOnly

                    if (isFiltered) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            }

            // Empty State
            if (suppliers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Suppliers Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search criteria, MOQ threshold, or category filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearFilters() }) {
                            Text("Reset All Filters")
                        }
                    }
                }
            } else {
                items(suppliers, key = { it.id }) { supplier ->
                    SupplierCard(
                        supplier = supplier,
                        onViewSupplier = { viewModel.openSupplierDetail(supplier) },
                        onRequestRfq = { showRfqDialogForSupplier = supplier },
                        onToggleBookmark = { viewModel.toggleBookmark(supplier) },
                        currency = selectedCurrency,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // RFQ Dialog
    showRfqDialogForSupplier?.let { targetSupplier ->
        RfqDialog(
            supplier = targetSupplier,
            prefilledBuyerName = userAccount.fullName,
            prefilledBuyerEmail = userAccount.email,
            prefilledDestination = userAccount.shippingDestination,
            onDismiss = { showRfqDialogForSupplier = null },
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

    // Add Factory / Supplier Dialog
    if (showListingDialog) {
        SupplierListingDialog(
            categories = categories,
            onDismiss = { showListingDialog = false },
            onSubmit = { name, categoryId, subcategory, originCountry, cityState, yearEstablished, moq, moqUnit, avgLeadTimeDays, about, email, phone, productTitle, productPrice ->
                viewModel.registerNewSupplier(
                    name,
                    categoryId,
                    subcategory,
                    originCountry,
                    cityState,
                    yearEstablished,
                    moq,
                    moqUnit,
                    avgLeadTimeDays,
                    about,
                    email,
                    phone,
                    productTitle,
                    productPrice
                )
            }
        )
    }

    // Currency Selection Modal Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = TradeGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Currency Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Real-time exchange conversion applied across factory catalogs, MOQ tier calculators, and supplier dossiers:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlobalCurrencies.allCurrencies.forEach { curr ->
                        val isCurrSelected = selectedCurrency.code == curr.code
                        Surface(
                            onClick = {
                                viewModel.setCurrencyMode(curr)
                                showCurrencyDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isCurrSelected) 1.5.dp else 0.5.dp,
                                color = if (isCurrSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("currency_option_${curr.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = curr.flag, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${curr.code} - ${curr.name}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Symbol: ${curr.symbol} • 1 USD ≈ ${curr.symbol}${if (curr.rateToUsd >= 100) String.format("%,.0f", curr.rateToUsd) else String.format("%.2f", curr.rateToUsd)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isCurrSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Appearance Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Application Appearance Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Customize the UI visual appearance for optimal visibility in your workspace:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppThemeMode.entries.forEach { mode ->
                        val isModeSelected = themeMode == mode
                        Surface(
                            onClick = {
                                viewModel.setThemeMode(mode)
                                showThemeDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isModeSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isModeSelected) 1.5.dp else 0.5.dp,
                                color = if (isModeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("theme_option_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = mode.icon, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = mode.displayName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = when (mode) {
                                                AppThemeMode.SYSTEM -> "Follows Android system light/dark mode settings"
                                                AppThemeMode.LIGHT -> "Crisp high-readability daylight sourcing palette"
                                                AppThemeMode.DARK -> "Midnight high-contrast terminal interface"
                                                AppThemeMode.NAVY -> "Deep oceanic marine navy executive layout"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isModeSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showSubscriptionPaywall) {
        SubscriptionPaywallDialog(
            userAccount = userAccount,
            currency = selectedCurrency,
            onSubscribeAnnual = {
                viewModel.activateAnnualSubscription()
            },
            onDismiss = { showSubscriptionPaywall = false }
        )
    }
}
