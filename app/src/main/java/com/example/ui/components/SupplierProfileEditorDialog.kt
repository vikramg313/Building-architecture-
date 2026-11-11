package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CategoryItem
import com.example.model.GlobalCountries
import com.example.model.SupplierContact
import com.example.model.SupplierVerification
import com.example.model.WholesaleSupplier
import com.example.ui.theme.TradeBlue
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierProfileEditorDialog(
    initialSupplier: WholesaleSupplier?,
    categories: List<CategoryItem>,
    defaultCompanyName: String,
    defaultEmail: String,
    defaultPhone: String,
    onDismiss: () -> Unit,
    onSaveSupplier: (WholesaleSupplier) -> Unit,
    modifier: Modifier = Modifier
) {
    val existing = initialSupplier

    var factoryTitle by remember { mutableStateOf(existing?.name ?: defaultCompanyName.ifBlank { "My Manufacturing Co." }) }
    var selectedCategory by remember {
        mutableStateOf(
            categories.find { it.id == existing?.categoryId } ?: categories.firstOrNull() ?: categories[0]
        )
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategory by remember { mutableStateOf(existing?.subcategory ?: selectedCategory.subcategories.firstOrNull() ?: "General Wholesale") }
    var originCountry by remember { mutableStateOf(existing?.originCountry ?: "Taiwan") }
    var countryExpanded by remember { mutableStateOf(false) }
    var cityState by remember { mutableStateOf(existing?.cityState ?: "Taipei Industrial Park") }
    var yearEstablishedText by remember { mutableStateOf((existing?.yearEstablished ?: 2018).toString()) }

    var moqText by remember { mutableStateOf((existing?.moq ?: 50).toString()) }
    var moqUnit by remember { mutableStateOf(existing?.moqUnit ?: "pcs") }
    var avgLeadTimeText by remember { mutableStateOf((existing?.avgLeadTimeDays ?: 10).toString()) }
    var samplePriceText by remember { mutableStateOf((existing?.samplePrice ?: 25.0).toString()) }

    var factorySize by remember { mutableStateOf(existing?.factorySize ?: "18,000 m²") }
    var staffCount by remember { mutableStateOf(existing?.staffCount ?: "120-250 staff") }
    var annualExport by remember { mutableStateOf(existing?.annualExport ?: "$10M - $25M") }

    var contactPerson by remember { mutableStateOf(existing?.contact?.contactPerson ?: "Export Sales Director") }
    var email by remember { mutableStateOf(existing?.email ?: defaultEmail.ifBlank { "sales@factory.com" }) }
    var phone by remember { mutableStateOf(existing?.phone ?: defaultPhone.ifBlank { "+886 2 8849 2019" }) }
    var whatsapp by remember { mutableStateOf(existing?.contact?.whatsapp ?: phone) }
    var fullAddress by remember { mutableStateOf(existing?.contact?.fullAddress ?: "$cityState, $originCountry") }
    var website by remember { mutableStateOf(existing?.contact?.website ?: "https://www.factory-direct.com") }

    var certIso by remember { mutableStateOf(existing?.certifications?.contains("ISO 9001") ?: true) }
    var certCe by remember { mutableStateOf(existing?.certifications?.contains("CE") ?: true) }
    var certRohs by remember { mutableStateOf(existing?.certifications?.contains("RoHS") ?: true) }
    var certFda by remember { mutableStateOf(existing?.certifications?.contains("FDA Registered") ?: false) }

    var customLogo by remember { mutableStateOf(existing?.customization?.contains("Custom Logo") ?: true) }
    var customPackaging by remember { mutableStateOf(existing?.customization?.contains("Custom Packaging") ?: true) }
    var oemOdm by remember { mutableStateOf(existing?.customization?.contains("OEM/ODM") ?: true) }

    var aboutText by remember {
        mutableStateOf(
            existing?.about ?: "Specialized manufacturer equipped with high-precision automated machinery, ISO-certified cleanrooms, and stringent QC inspection before export."
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("supplier_profile_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(TradeGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Factory,
                                    contentDescription = null,
                                    tint = TradeGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (existing == null) "Setup Supplier Profile" else "Edit Factory Profile",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Manage your wholesale listing manually",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_editor_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Section 1: Basic Company & Hub Information
                    Text(
                        text = "1. Factory & Manufacturing Hub",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = factoryTitle,
                        onValueChange = { factoryTitle = it },
                        label = { Text("Factory / Company Legal Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("supplier_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Primary Industry Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
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
                        label = { Text("Specific Subcategory / Specialty") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Country Hub Selector (21 Countries)
                    ExposedDropdownMenuBox(
                        expanded = countryExpanded,
                        onExpandedChange = { countryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${GlobalCountries.getFlagForCountry(originCountry)} $originCountry",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Manufacturing Country Hub") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = countryExpanded,
                            onDismissRequest = { countryExpanded = false }
                        ) {
                            GlobalCountries.allCountries.forEach { hub ->
                                DropdownMenuItem(
                                    text = { Text("${hub.flag} ${hub.name} (${hub.region})") },
                                    onClick = {
                                        originCountry = hub.name
                                        countryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cityState,
                            onValueChange = { cityState = it },
                            label = { Text("City / Industrial Zone") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = yearEstablishedText,
                            onValueChange = { yearEstablishedText = it },
                            label = { Text("Est. Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 2: Wholesale Trade Terms & MOQ
                    Text(
                        text = "2. Wholesale Terms & Capacity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = moqText,
                            onValueChange = { moqText = it },
                            label = { Text("Min. Order (MOQ)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = moqUnit,
                            onValueChange = { moqUnit = it },
                            label = { Text("Unit (pcs/units/kg)") },
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
                            value = avgLeadTimeText,
                            onValueChange = { avgLeadTimeText = it },
                            label = { Text("Avg Lead Time (Days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = samplePriceText,
                            onValueChange = { samplePriceText = it },
                            label = { Text("Sample Price ($ USD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            value = factorySize,
                            onValueChange = { factorySize = it },
                            label = { Text("Factory Size") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = staffCount,
                            onValueChange = { staffCount = it },
                            label = { Text("Staff Count") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = annualExport,
                        onValueChange = { annualExport = it },
                        label = { Text("Annual Export Revenue (e.g. $10M - $25M)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 3: Direct Factory Contact Details (Email & Mobile)
                    Text(
                        text = "3. Direct Factory Contacts (For Buyer Queries)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Contact Person / Head of Export") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Inquiry Email Address *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("supplier_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile / Phone *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("supplier_phone_input")
                        )
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp Desk") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        label = { Text("Physical Factory Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Corporate Website") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 4: Certifications & OEM Capabilities
                    Text(
                        text = "4. Verified Certifications & Customization",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = certIso, onCheckedChange = { certIso = it })
                            Text("ISO 9001", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = certCe, onCheckedChange = { certCe = it })
                            Text("CE Mark", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = certRohs, onCheckedChange = { certRohs = it })
                            Text("RoHS", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = customLogo, onCheckedChange = { customLogo = it })
                            Text("Custom Logo", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = customPackaging, onCheckedChange = { customPackaging = it })
                            Text("Packaging", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = oemOdm, onCheckedChange = { oemOdm = it })
                            Text("OEM/ODM", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = aboutText,
                        onValueChange = { aboutText = it },
                        label = { Text("Factory Overview & Manufacturing Dossier") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (factoryTitle.isBlank()) {
                                errorMessage = "Please enter factory or company name."
                                return@Button
                            }
                            if (email.isBlank() || phone.isBlank()) {
                                errorMessage = "Email and Phone are required for direct buyer queries."
                                return@Button
                            }

                            val moqVal = moqText.toIntOrNull() ?: 50
                            val leadTimeVal = avgLeadTimeText.toIntOrNull() ?: 10
                            val samplePriceVal = samplePriceText.toDoubleOrNull() ?: 20.0
                            val estYearVal = yearEstablishedText.toIntOrNull() ?: 2018

                            val certs = mutableListOf<String>()
                            if (certIso) certs.add("ISO 9001")
                            if (certCe) certs.add("CE")
                            if (certRohs) certs.add("RoHS")
                            if (certFda) certs.add("FDA Registered")
                            if (certs.isEmpty()) certs.add("Trade Verified")

                            val customList = mutableListOf<String>()
                            if (customLogo) customList.add("Custom Logo")
                            if (customPackaging) customList.add("Custom Packaging")
                            if (oemOdm) customList.add("OEM/ODM")

                            val supplierId = existing?.id ?: "sup_registered_${System.currentTimeMillis()}"

                            val updated = WholesaleSupplier(
                                id = supplierId,
                                name = factoryTitle.trim(),
                                categoryId = selectedCategory.id,
                                subcategory = subcategory.trim(),
                                originCountry = originCountry.trim(),
                                cityState = cityState.trim(),
                                yearEstablished = estYearVal,
                                isVerified = true,
                                isGoldSupplier = true,
                                hasTradeAssurance = true,
                                rating = existing?.rating ?: 5.0f,
                                reviewCount = (existing?.reviewCount ?: 0).coerceAtLeast(1),
                                responseRatePercent = existing?.responseRatePercent ?: 99,
                                avgLeadTimeDays = leadTimeVal,
                                moq = moqVal,
                                moqUnit = moqUnit.trim(),
                                samplePrice = samplePriceVal,
                                certifications = certs,
                                factorySize = factorySize.trim(),
                                staffCount = staffCount.trim(),
                                annualExport = annualExport.trim(),
                                customization = customList,
                                paymentTerms = listOf("Trade Assurance", "T/T Wire", "L/C"),
                                about = aboutText.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                isBookmarked = existing?.isBookmarked ?: false,
                                products = existing?.products ?: emptyList(),
                                verification = SupplierVerification(
                                    isVerified = true,
                                    isGoldSupplier = true,
                                    hasTradeAssurance = true,
                                    businessLicenseNumber = "BLN-REG-${supplierId.takeLast(6).uppercase()}"
                                ),
                                contact = SupplierContact(
                                    contactPerson = contactPerson.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    whatsapp = whatsapp.trim(),
                                    website = website.trim(),
                                    fullAddress = fullAddress.trim()
                                )
                            )

                            onSaveSupplier(updated)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_supplier_profile_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existing == null) "Publish Factory Profile" else "Save Changes & Update Dossier",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
