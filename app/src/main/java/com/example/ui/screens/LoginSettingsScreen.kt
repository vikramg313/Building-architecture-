package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.CurrencyMode
import com.example.model.GlobalCurrencies
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.WholesaleSupplier
import com.example.ui.components.SubscriptionPaywallDialog
import com.example.ui.components.SupplierProfileEditorDialog
import com.example.ui.theme.TradeBlue
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold
import com.example.viewmodel.WholesaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSettingsScreen(
    viewModel: WholesaleViewModel,
    modifier: Modifier = Modifier
) {
    val userAccount by viewModel.userAccount.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val allSuppliers by viewModel.allSuppliers.collectAsState()
    var authTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showSupplierEditor by remember { mutableStateOf(false) }

    // Find supplier profile if registered supplier
    val linkedSupplier = allSuppliers.find { it.id == userAccount.supplierProfileId }
        ?: allSuppliers.find { it.email.equals(userAccount.email, ignoreCase = true) || it.name.equals(userAccount.companyName, ignoreCase = true) }

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
                                    imageVector = if (userAccount.isLoggedIn) Icons.Default.ManageAccounts else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (userAccount.isLoggedIn) "Account & Trade Settings" else "B2B Login & Sourcing Portal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (userAccount.isLoggedIn) "${userAccount.companyName} (${userAccount.role.displayName})" else "Sign in to manage RFQs and factory orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            if (!userAccount.isLoggedIn) {
                // Not logged in: Show Login / Registration View
                item {
                    TabRow(
                        selectedTabIndex = authTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = authTab == 0,
                            onClick = { authTab = 0 },
                            text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = authTab == 1,
                            onClick = { authTab = 1 },
                            text = { Text("Register Business", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.AppRegistration, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                if (authTab == 0) {
                    item {
                        SignInForm(
                            onSignIn = { email, role -> viewModel.login(email, role) },
                            onQuickDemo = { isBuyer -> viewModel.quickDemoLogin(isBuyer) }
                        )
                    }
                } else {
                    item {
                        RegisterForm(
                            onRegister = { name, email, company, phone, destination, role ->
                                viewModel.register(name, email, company, phone, destination, role)
                            }
                        )
                    }
                }
            } else {
                // Logged in: Comprehensive Account Profile & Trade Settings
                item {
                    UserProfileHeaderCard(
                        user = userAccount,
                        onSwitchRole = { newRole -> viewModel.switchUserRole(newRole) },
                        onLogout = { viewModel.logout() }
                    )
                }

                // Registered Supplier Factory Profile Management Card (Manual Setup & Edit)
                item {
                    RegisteredSupplierProfileCard(
                        user = userAccount,
                        linkedSupplier = linkedSupplier,
                        onOpenEditor = { showSupplierEditor = true },
                        onViewLiveDossier = {
                            linkedSupplier?.let { viewModel.openSupplierDetail(it) }
                        }
                    )
                }

                // 1 Dollar Annual Subscription Membership Card
                item {
                    SubscriptionPlanCard(
                        user = userAccount,
                        currency = selectedCurrency,
                        onUpgradeClick = { showSubscriptionDialog = true },
                        onCancelClick = { viewModel.cancelSubscription() }
                    )
                }

                // Application Appearance Theme Mode
                item {
                    AppearanceThemeSettingsCard(
                        currentTheme = themeMode,
                        onThemeSelect = { newTheme -> viewModel.setThemeMode(newTheme) }
                    )
                }

                // Sourcing & Company Profile Settings
                item {
                    CompanyProfileSettingsCard(
                        user = userAccount,
                        onUpdate = { updated -> viewModel.updateUserAccount(updated) }
                    )
                }

                // Trade & Currency Preferences (All 18 Currencies)
                item {
                    TradePreferencesCard(
                        user = userAccount,
                        onCurrencyChange = { curr -> viewModel.setPreferredCurrency(curr) }
                    )
                }

                // Notifications & RFQ Alerts
                item {
                    NotificationSettingsCard(
                        user = userAccount,
                        onToggleNotifications = { viewModel.toggleNotifications(it) }
                    )
                }

                // Security & Authentication Settings
                item {
                    SecuritySettingsCard(
                        user = userAccount,
                        onToggleBiometric = { viewModel.toggleBiometric(it) },
                        onToggleTwoFactor = { viewModel.toggleTwoFactor(it) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }

            // Always display Appearance Theme Setting for easy access
            if (!userAccount.isLoggedIn) {
                item {
                    AppearanceThemeSettingsCard(
                        currentTheme = themeMode,
                        onThemeSelect = { newTheme -> viewModel.setThemeMode(newTheme) }
                    )
                }
            }
        }
    }

    if (showSubscriptionDialog) {
        SubscriptionPaywallDialog(
            userAccount = userAccount,
            currency = selectedCurrency,
            onSubscribeAnnual = {
                viewModel.activateAnnualSubscription()
            },
            onDismiss = { showSubscriptionDialog = false }
        )
    }

    if (showSupplierEditor) {
        SupplierProfileEditorDialog(
            initialSupplier = linkedSupplier,
            categories = viewModel.categories,
            defaultCompanyName = userAccount.companyName,
            defaultEmail = userAccount.email,
            defaultPhone = userAccount.phone,
            onDismiss = { showSupplierEditor = false },
            onSaveSupplier = { updatedSupplier ->
                viewModel.updateSupplierProfile(updatedSupplier)
            }
        )
    }
}

@Composable
fun SignInForm(
    onSignIn: (email: String, role: UserRole) -> Unit,
    onQuickDemo: (isBuyer: Boolean) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.WHOLESALE_BUYER) }
    var rememberMe by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("sign_in_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Welcome to TradeSource",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to access bulk pricing, submit RFQs, and communicate with certified factories.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role selection chips
            Text("I am signing in as:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedRole == UserRole.WHOLESALE_BUYER,
                    onClick = { selectedRole = UserRole.WHOLESALE_BUYER },
                    label = { Text("Wholesale Buyer") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedRole == UserRole.VERIFIED_SUPPLIER,
                    onClick = { selectedRole = UserRole.VERIFIED_SUPPLIER },
                    label = { Text("Manufacturer") },
                    leadingIcon = { Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Work Email") },
                placeholder = { Text("buyer@company.com") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("Remember device", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = {}) {
                    Text("Forgot Password?", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onSignIn(email, selectedRole) },
                modifier = Modifier.fillMaxWidth().testTag("login_submit_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign In to B2B Directory", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  OR 1-TAP DEMO ACCESS  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onQuickDemo(true) },
                    modifier = Modifier.weight(1f).testTag("quick_buyer_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Demo Buyer", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onQuickDemo(false) },
                    modifier = Modifier.weight(1f).testTag("quick_supplier_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = TradeGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Demo Factory", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RegisterForm(
    onRegister: (name: String, email: String, company: String, phone: String, destination: String, role: UserRole) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var officerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("Los Angeles Port, CA, USA") }
    var selectedRole by remember { mutableStateOf(UserRole.WHOLESALE_BUYER) }
    var password by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("register_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Register Your Enterprise",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Join over 25,000 corporate buyers and verified manufacturers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Role selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedRole == UserRole.WHOLESALE_BUYER,
                    onClick = { selectedRole = UserRole.WHOLESALE_BUYER },
                    label = { Text("Wholesale Buyer / Importer") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedRole == UserRole.VERIFIED_SUPPLIER,
                    onClick = { selectedRole = UserRole.VERIFIED_SUPPLIER },
                    label = { Text("Manufacturer / Supplier") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Legal Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = officerName,
                onValueChange = { officerName = it },
                label = { Text("Procurement Officer / Contact Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Corporate Work Email *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone / WhatsApp *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Primary Receiving Destination / Port") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onRegister(
                        if (officerName.isBlank()) "Enterprise Officer" else officerName,
                        if (email.isBlank()) "procurement@company.com" else email,
                        if (companyName.isBlank()) "Global Trade Co." else companyName,
                        if (phone.isBlank()) "+1 (800) 555-0199" else phone,
                        destination,
                        selectedRole
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("register_submit_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Registration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UserProfileHeaderCard(
    user: UserAccount,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TradeEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, TradeEmerald.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = TradeEmerald, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(user.verificationStatus, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TradeEmerald)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch role button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Mode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user.role.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }

                    TextButton(
                        onClick = {
                            val newRole = if (user.role == UserRole.WHOLESALE_BUYER) UserRole.VERIFIED_SUPPLIER else UserRole.WHOLESALE_BUYER
                            onSwitchRole(newRole)
                        }
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch Mode")
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyProfileSettingsCard(
    user: UserAccount,
    onUpdate: (UserAccount) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var companyName by remember(user) { mutableStateOf(user.companyName) }
    var phone by remember(user) { mutableStateOf(user.phone) }
    var destination by remember(user) { mutableStateOf(user.shippingDestination) }
    var taxId by remember(user) { mutableStateOf(user.taxIdOrVat) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Company & Logistics Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = {
                        if (isEditing) {
                            onUpdate(
                                user.copy(
                                    companyName = companyName,
                                    phone = phone,
                                    shippingDestination = destination,
                                    taxIdOrVat = taxId
                                )
                            )
                        }
                        isEditing = !isEditing
                    }
                ) {
                    Text(if (isEditing) "Save" else "Edit")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isEditing) {
                ProfileInfoRow("Company Name", user.companyName)
                ProfileInfoRow("Tax ID / VAT", user.taxIdOrVat)
                ProfileInfoRow("Direct Phone", user.phone)
                ProfileInfoRow("Delivery Port", user.shippingDestination)
            } else {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = taxId,
                    onValueChange = { taxId = it },
                    label = { Text("Tax ID / VAT Registration") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Default Shipping Port / City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AppearanceThemeSettingsCard(
    currentTheme: AppThemeMode,
    onThemeSelect: (AppThemeMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("appearance_theme_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Application Appearance Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Select visual color profile for workspace comfort and high contrast sourcing",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    val isSelected = currentTheme == mode
                    Surface(
                        onClick = { onThemeSelect(mode) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("theme_btn_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = mode.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = mode.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = when (mode) {
                                            AppThemeMode.SYSTEM -> "Follows device light / dark display"
                                            AppThemeMode.LIGHT -> "Crisp high-clarity daylight sourcing"
                                            AppThemeMode.DARK -> "Midnight high-contrast terminal"
                                            AppThemeMode.NAVY -> "Deep oceanic marine executive blue"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TradePreferencesCard(
    user: UserAccount,
    onCurrencyChange: (String) -> Unit
) {
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    val currentCurrency = GlobalCurrencies.findByCode(user.preferredCurrency)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("trade_preferences_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = TradeGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trade & Currency Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Display Currency Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("18 global currencies with real-time rate conversion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box {
                    OutlinedButton(
                        onClick = { currencyMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("${currentCurrency.flag} ${currentCurrency.code} (${currentCurrency.symbol})")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = currencyMenuExpanded,
                        onDismissRequest = { currencyMenuExpanded = false }
                    ) {
                        GlobalCurrencies.allCurrencies.forEach { curr ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(curr.flag, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("${curr.code} - ${curr.name} (${curr.symbol})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("1 USD ≈ ${curr.symbol}${if (curr.rateToUsd >= 100) String.format("%,.0f", curr.rateToUsd) else String.format("%.2f", curr.rateToUsd)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
                                onClick = {
                                    onCurrencyChange("${curr.code} (${curr.symbol})")
                                    currencyMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            ProfileInfoRow("Global Manufacturing Coverage", "21 Verified Country Hubs")
            ProfileInfoRow("Payment Safeguard", "100% Trade Assurance Protected")
            ProfileInfoRow("Inspection Standard", "AQL 2.5 Pre-Shipment Audit")
        }
    }
}

@Composable
fun NotificationSettingsCard(
    user: UserAccount,
    onToggleNotifications: (Boolean) -> Unit
) {
    var quoteAlerts by remember { mutableStateOf(user.quoteAlertsEnabled) }
    var sampleUpdates by remember { mutableStateOf(user.sampleTrackingEnabled) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notifications & RFQ Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("RFQ Quote Counter-Offers", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Instant alert when factory submits quotation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = quoteAlerts,
                    onCheckedChange = { quoteAlerts = it }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sample Tracking & Dispatch", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("DHL / FedEx courier milestones for physical samples", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = sampleUpdates,
                    onCheckedChange = { sampleUpdates = it }
                )
            }
        }
    }
}

@Composable
fun SecuritySettingsCard(
    user: UserAccount,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleTwoFactor: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = TradeBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Security & Access Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biometric App Lock", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Require Face ID / Fingerprint to approve RFQs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = user.biometricLockEnabled,
                    onCheckedChange = onToggleBiometric
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Two-Factor Authentication (2FA)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("SMS / Authenticator verification for commercial orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = user.twoFactorAuthEnabled,
                    onCheckedChange = onToggleTwoFactor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.fillMaxWidth().testTag("logout_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Corporate Account", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SubscriptionPlanCard(
    user: UserAccount,
    currency: CurrencyMode,
    onUpgradeClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubscribed = user.isSubscribed

    Card(
        modifier = modifier.fillMaxWidth().testTag("subscription_plan_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSubscribed) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            }
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSubscribed) TradeGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSubscribed) TradeGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSubscribed) Icons.Default.WorkspacePremium else Icons.Default.CardMembership,
                            contentDescription = null,
                            tint = if (isSubscribed) TradeGold else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "B2B Subscription",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSubscribed) TradeEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (isSubscribed) "PRO ACTIVE" else "FREE TIER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSubscribed) TradeEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isSubscribed) "Annual B2B Pro Pass • Active" else "Upgrade for factory direct privileges",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${currency.format(1.0)}/yr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSubscribed) TradeGold else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isSubscribed) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TradeGold.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, TradeGold.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Plan:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.subscriptionPlan, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TradeGold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Valid Until:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.subscriptionExpiryDate.ifBlank { "1 Year from today" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Annual Price:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${currency.format(1.0)} USD / year", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onUpgradeClick,
                        modifier = Modifier.weight(1f).testTag("renew_subscription_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Manage Plan", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f).testTag("cancel_subscription_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = "Get unrestricted 1-tap direct factory contact (Email, Mobile, WhatsApp), 21 country hubs, live multi-currency converters, and priority RFQs for just $1.00 USD / year.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth().testTag("upgrade_subscription_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlock Annual Pro for ${currency.format(1.0)} / Year",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RegisteredSupplierProfileCard(
    user: UserAccount,
    linkedSupplier: WholesaleSupplier?,
    onOpenEditor: () -> Unit,
    onViewLiveDossier: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("registered_supplier_profile_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TradeEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = TradeEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Supplier Factory Dossier",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TradeEmerald.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (linkedSupplier != null) "LIVE DIRECTORY" else "NOT PUBLISHED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TradeEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (linkedSupplier != null) "Manage your manual factory profile & trade specifications" else "Create and publish your manufacturing plant profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (linkedSupplier != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Factory Name:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(linkedSupplier.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Manufacturing Hub:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${linkedSupplier.originCountry} (${linkedSupplier.cityState})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Category / MOQ:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${linkedSupplier.subcategory} • ${linkedSupplier.moq} ${linkedSupplier.moqUnit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Direct Contact Desk:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${linkedSupplier.phone} | ${linkedSupplier.email}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Certifications:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(linkedSupplier.certifications.joinToString(", "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TradeGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenEditor,
                        modifier = Modifier.weight(1f).testTag("edit_supplier_profile_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Factory Profile", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewLiveDossier,
                        modifier = Modifier.weight(1f).testTag("view_live_dossier_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Live Page", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = "As a verified or registered supplier, you can manually set up your factory profile, customize MOQ, lead times, sample pricing, certifications (ISO, CE, RoHS), and direct inquiry phone/email channels.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenEditor,
                    modifier = Modifier.fillMaxWidth().testTag("setup_supplier_profile_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Up Factory Profile Manually",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


