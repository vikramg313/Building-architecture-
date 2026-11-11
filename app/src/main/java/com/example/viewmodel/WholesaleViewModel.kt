package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WholesaleRepository
import com.example.data.WholesaleSeedData
import com.example.data.local.AppDatabase
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AppScreen {
    data object Directory : AppScreen()
    data object CategoriesOverview : AppScreen()
    data class SupplierDetail(val supplierId: String) : AppScreen()
    data object RfqDashboard : AppScreen()
    data object SavedSuppliers : AppScreen()
    data object AccountSettings : AppScreen()
}

data class TierCalculationResult(
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val savingsPercent: Int,
    val estimatedShipping: Double,
    val totalCost: Double,
    val suggestedRetailPrice: Double,
    val estimatedProfitMargin: Double,
    val formattedUnitPrice: String = "",
    val formattedSubtotal: String = "",
    val formattedTotalCost: String = "",
    val formattedSuggestedRetailPrice: String = "",
    val currencySymbol: String = "$"
)

class WholesaleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WholesaleRepository(AppDatabase.getInstance(application))

    val categories = WholesaleSeedData.categories

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Directory)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>(AppScreen.Directory)

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<WholesaleSupplier?>(null)
    val selectedSupplier: StateFlow<WholesaleSupplier?> = _selectedSupplier.asStateFlow()

    private val _comparisonSupplierIds = MutableStateFlow<Set<String>>(emptySet())
    val comparisonSupplierIds: StateFlow<Set<String>> = _comparisonSupplierIds.asStateFlow()

    val allSuppliers: StateFlow<List<WholesaleSupplier>> = repository.allSuppliersWithProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<WholesaleProduct>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRfqs: StateFlow<List<RfqItem>> = repository.allRfqs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedSuppliers: StateFlow<List<WholesaleSupplier>> = repository.bookmarkedSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userAccount: StateFlow<UserAccount> = repository.userAccountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserAccount())

    private val _selectedCurrency = MutableStateFlow<CurrencyMode>(GlobalCurrencies.defaultCurrency)
    val selectedCurrency: StateFlow<CurrencyMode> = _selectedCurrency.asStateFlow()

    private val _themeMode = MutableStateFlow<AppThemeMode>(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            userAccount.collect { account ->
                val matched = GlobalCurrencies.findByCode(account.preferredCurrency)
                if (_selectedCurrency.value.code != matched.code) {
                    _selectedCurrency.value = matched
                }
                val theme = try {
                    AppThemeMode.valueOf(account.appTheme)
                } catch (_: Exception) {
                    AppThemeMode.DARK
                }
                if (_themeMode.value != theme) {
                    _themeMode.value = theme
                }
            }
        }
    }

    val filteredSuppliers: StateFlow<List<WholesaleSupplier>> = combine(
        allSuppliers,
        _filterState
    ) { suppliers, filters ->
        var list = suppliers

        // Filter by country
        if (filters.selectedCountry != "all") {
            list = list.filter { it.originCountry.equals(filters.selectedCountry, ignoreCase = true) }
        }

        // Filter by category
        if (filters.selectedCategoryId != "all") {
            list = list.filter { it.categoryId.equals(filters.selectedCategoryId, ignoreCase = true) }
        }

        // Filter by subcategory
        if (filters.selectedSubcategory != "all") {
            list = list.filter { it.subcategory.equals(filters.selectedSubcategory, ignoreCase = true) }
        }

        // Filter by search query
        if (filters.searchQuery.isNotBlank()) {
            val q = filters.searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.originCountry.lowercase().contains(q) ||
                it.subcategory.lowercase().contains(q) ||
                it.about.lowercase().contains(q) ||
                it.certifications.any { cert -> cert.lowercase().contains(q) } ||
                it.products.any { p -> p.title.lowercase().contains(q) || p.description.lowercase().contains(q) }
            }
        }

        // Filter flags
        if (filters.verifiedOnly) {
            list = list.filter { it.isVerified }
        }
        if (filters.tradeAssuranceOnly) {
            list = list.filter { it.hasTradeAssurance }
        }
        if (filters.lowMoqOnly) {
            list = list.filter { it.moq <= 50 }
        }
        if (filters.fastLeadTimeOnly) {
            list = list.filter { it.avgLeadTimeDays <= 10 }
        }

        // Sort
        when (filters.sortOption) {
            DirectorySortOption.RECOMMENDED -> list.sortedWith(
                compareByDescending<WholesaleSupplier> { it.isGoldSupplier }
                    .thenByDescending { it.rating }
                    .thenByDescending { it.reviewCount }
            )
            DirectorySortOption.RATING -> list.sortedByDescending { it.rating }
            DirectorySortOption.MOQ_LOW_TO_HIGH -> list.sortedBy { it.moq }
            DirectorySortOption.LEAD_TIME -> list.sortedBy { it.avgLeadTimeDays }
            DirectorySortOption.EXPERIENCE -> list.sortedBy { it.yearEstablished }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val comparisonSuppliers: StateFlow<List<WholesaleSupplier>> = combine(
        allSuppliers,
        _comparisonSupplierIds
    ) { suppliers, ids ->
        suppliers.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation
    fun navigateTo(screen: AppScreen) {
        if (_screenHistory.lastOrNull() != screen) {
            _screenHistory.add(screen)
        }
        _currentScreen.value = screen
    }

    fun handleBackPress(): Boolean {
        if (_screenHistory.size > 1) {
            _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = _screenHistory.last()
            return true
        }
        return false
    }

    fun openSupplierDetail(supplier: WholesaleSupplier) {
        _selectedSupplier.value = supplier
        navigateTo(AppScreen.SupplierDetail(supplier.id))
    }

    fun selectCategoryAndNavigate(categoryId: String) {
        _filterState.update { it.copy(selectedCategoryId = categoryId, selectedSubcategory = "all") }
        navigateTo(AppScreen.Directory)
    }

    // Filters
    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun selectCategoryId(categoryId: String) {
        _filterState.update {
            if (it.selectedCategoryId == categoryId) {
                it.copy(selectedCategoryId = "all", selectedSubcategory = "all")
            } else {
                it.copy(selectedCategoryId = categoryId, selectedSubcategory = "all")
            }
        }
    }

    fun selectSubcategory(subcategory: String) {
        _filterState.update {
            if (it.selectedSubcategory == subcategory) {
                it.copy(selectedSubcategory = "all")
            } else {
                it.copy(selectedSubcategory = subcategory)
            }
        }
    }

    fun toggleVerifiedOnly() {
        _filterState.update { it.copy(verifiedOnly = !it.verifiedOnly) }
    }

    fun toggleTradeAssuranceOnly() {
        _filterState.update { it.copy(tradeAssuranceOnly = !it.tradeAssuranceOnly) }
    }

    fun toggleLowMoqOnly() {
        _filterState.update { it.copy(lowMoqOnly = !it.lowMoqOnly) }
    }

    fun toggleFastLeadTimeOnly() {
        _filterState.update { it.copy(fastLeadTimeOnly = !it.fastLeadTimeOnly) }
    }

    fun setSortOption(sortOption: DirectorySortOption) {
        _filterState.update { it.copy(sortOption = sortOption) }
    }

    fun selectCountry(country: String) {
        _filterState.update {
            if (it.selectedCountry.equals(country, ignoreCase = true)) {
                it.copy(selectedCountry = "all")
            } else {
                it.copy(selectedCountry = country)
            }
        }
    }

    fun setCurrencyMode(currency: CurrencyMode) {
        _selectedCurrency.value = currency
        setPreferredCurrency("${currency.code} (${currency.symbol})")
    }

    fun getSupplierById(id: String): WholesaleSupplier? {
        return allSuppliers.value.find { it.id == id }
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }

    // Actions
    fun toggleBookmark(supplier: WholesaleSupplier) {
        viewModelScope.launch {
            repository.toggleBookmark(supplier.id, supplier.isBookmarked)
            if (_selectedSupplier.value?.id == supplier.id) {
                _selectedSupplier.update { it?.copy(isBookmarked = !supplier.isBookmarked) }
            }
        }
    }

    fun toggleComparison(supplierId: String) {
        _comparisonSupplierIds.update { current ->
            if (supplierId in current) {
                current - supplierId
            } else {
                if (current.size >= 3) {
                    // Maximum 3 for comparison
                    current.drop(1).toSet() + supplierId
                } else {
                    current + supplierId
                }
            }
        }
    }

    fun submitRfq(
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
    ) {
        viewModelScope.launch {
            val rfq = RfqItem(
                id = "rfq_${System.currentTimeMillis()}",
                supplierId = supplierId,
                supplierName = supplierName,
                productName = productName,
                categoryName = categoryName,
                targetQuantity = targetQuantity,
                targetPrice = targetPrice,
                unit = unit,
                notes = notes,
                customLogo = customLogo,
                customPackaging = customPackaging,
                shippingDestination = shippingDestination,
                status = "Pending Quote",
                timestamp = System.currentTimeMillis(),
                buyerName = buyerName,
                buyerEmail = buyerEmail
            )
            repository.submitRfq(rfq)
        }
    }

    fun updateRfqStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateRfqStatus(id, status)
        }
    }

    fun deleteRfq(id: String) {
        viewModelScope.launch {
            repository.deleteRfq(id)
        }
    }

    fun registerNewSupplier(
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
    ) {
        viewModelScope.launch {
            val newId = "sup_custom_${System.currentTimeMillis()}"
            val newSupplier = WholesaleSupplier(
                id = newId,
                name = name,
                categoryId = categoryId,
                subcategory = subcategory,
                originCountry = originCountry,
                cityState = cityState,
                yearEstablished = yearEstablished,
                isVerified = true,
                isGoldSupplier = true,
                hasTradeAssurance = true,
                rating = 5.0f,
                reviewCount = 1,
                responseRatePercent = 100,
                avgLeadTimeDays = avgLeadTimeDays,
                moq = moq,
                moqUnit = moqUnit,
                samplePrice = (productPrice ?: 20.0) * 1.5,
                certifications = listOf("ISO 9001", "Trade Verified"),
                factorySize = "15,000 m²",
                staffCount = "80-150 staff",
                annualExport = "$5M - $10M",
                customization = listOf("Custom Logo", "Custom Packaging"),
                paymentTerms = listOf("Trade Assurance", "T/T Wire"),
                about = about,
                email = email,
                phone = phone,
                isBookmarked = false
            )

            repository.addNewSupplier(newSupplier)

            // Link created supplier profile to active user account
            val updatedUser = userAccount.value.copy(
                supplierProfileId = newId,
                role = UserRole.VERIFIED_SUPPLIER,
                companyName = name,
                phone = phone,
                email = email.ifBlank { userAccount.value.email }
            )
            repository.saveUserAccount(updatedUser)

            if (!productTitle.isNullOrBlank() && productPrice != null) {
                val newProd = WholesaleProduct(
                    id = "prod_custom_${System.currentTimeMillis()}",
                    supplierId = newId,
                    supplierName = name,
                    categoryId = categoryId,
                    subcategory = subcategory,
                    title = productTitle,
                    description = "Manufactured by $name under strict QC standards.",
                    moq = moq,
                    moqUnit = moqUnit,
                    priceTiers = listOf(
                        PriceTier(moq, moq * 3, productPrice),
                        PriceTier(moq * 3 + 1, moq * 10, productPrice * 0.85),
                        PriceTier(moq * 10 + 1, null, productPrice * 0.72)
                    ),
                    leadTimeDays = avgLeadTimeDays,
                    isReadyToShip = true,
                    samplePrice = productPrice * 1.5,
                    specs = mapOf("Origin" to originCountry, "Lead Time" to "$avgLeadTimeDays days")
                )
                repository.addNewProduct(newProd)
            }
        }
    }

    fun updateSupplierProfile(supplier: WholesaleSupplier) {
        viewModelScope.launch {
            repository.addNewSupplier(supplier)
            // Synchronize with user account company name & contacts
            val updatedUser = userAccount.value.copy(
                companyName = supplier.name,
                phone = supplier.phone,
                email = supplier.email,
                supplierProfileId = supplier.id,
                role = UserRole.VERIFIED_SUPPLIER
            )
            repository.saveUserAccount(updatedUser)
        }
    }

    fun calculateTierCost(
        product: WholesaleProduct,
        quantity: Int,
        currency: CurrencyMode = _selectedCurrency.value
    ): TierCalculationResult {
        val qty = quantity.coerceAtLeast(1)
        val matchingTier = product.priceTiers.lastOrNull { qty >= it.minQty }
            ?: product.priceTiers.firstOrNull()

        val basePrice = product.priceTiers.firstOrNull()?.unitPrice ?: 10.0
        val unitPrice = matchingTier?.unitPrice ?: basePrice
        val subtotal = unitPrice * qty
        val baselineTotal = basePrice * qty
        val savingsPercent = if (baselineTotal > 0 && subtotal < baselineTotal) {
            (((baselineTotal - subtotal) / baselineTotal) * 100).toInt()
        } else 0

        // Estimated ocean / air freight
        val estimatedShipping = (subtotal * 0.08).coerceAtLeast(25.0)
        val totalCost = subtotal + estimatedShipping

        // Suggested 2.5x - 3.2x retail markup
        val suggestedRetailUnitPrice = unitPrice * 2.8
        val projectedRevenue = suggestedRetailUnitPrice * qty
        val estimatedProfit = projectedRevenue - totalCost
        val marginPercent = if (projectedRevenue > 0) (estimatedProfit / projectedRevenue) * 100 else 0.0

        return TierCalculationResult(
            quantity = qty,
            unitPrice = unitPrice,
            subtotal = subtotal,
            savingsPercent = savingsPercent,
            estimatedShipping = estimatedShipping,
            totalCost = totalCost,
            suggestedRetailPrice = suggestedRetailUnitPrice,
            estimatedProfitMargin = marginPercent,
            formattedUnitPrice = currency.format(unitPrice),
            formattedSubtotal = currency.format(subtotal),
            formattedTotalCost = currency.format(totalCost),
            formattedSuggestedRetailPrice = currency.format(suggestedRetailUnitPrice),
            currencySymbol = currency.symbol
        )
    }

    // Account & Settings
    fun login(email: String, role: UserRole) {
        viewModelScope.launch {
            val current = userAccount.value
            val updated = current.copy(
                email = email.ifBlank { "buyer@globaltrade.com" },
                role = role,
                isLoggedIn = true
            )
            repository.saveUserAccount(updated)
        }
    }

    fun quickDemoLogin(isBuyer: Boolean) {
        viewModelScope.launch {
            val updated = if (isBuyer) {
                UserAccount(
                    id = "usr_buyer_demo",
                    email = "alexander@apeximports.com",
                    fullName = "Alexander Vance",
                    companyName = "Apex Global Imports LLC",
                    role = UserRole.WHOLESALE_BUYER,
                    shippingDestination = "Los Angeles Port, CA, USA",
                    isLoggedIn = true,
                    verificationStatus = "Verified Importer"
                )
            } else {
                UserAccount(
                    id = "usr_supplier_demo",
                    email = "export@titanforgecnc.com",
                    fullName = "Marcus Schmidt",
                    companyName = "TitanForge CNC Precision Dynamics",
                    role = UserRole.VERIFIED_SUPPLIER,
                    shippingDestination = "Hamburg Port, Germany",
                    isLoggedIn = true,
                    verificationStatus = "Audited Manufacturer (Gold)"
                )
            }
            repository.saveUserAccount(updated)
        }
    }

    fun register(
        fullName: String,
        email: String,
        companyName: String,
        phone: String,
        destination: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            val newAcc = UserAccount(
                id = "usr_${System.currentTimeMillis()}",
                email = email,
                fullName = fullName,
                companyName = companyName,
                phone = phone,
                shippingDestination = destination.ifBlank { "Los Angeles Port, CA, USA" },
                role = role,
                isLoggedIn = true,
                verificationStatus = if (role == UserRole.WHOLESALE_BUYER) "Verified Importer" else "Registered Supplier"
            )
            repository.saveUserAccount(newAcc)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val updated = userAccount.value.copy(isLoggedIn = false)
            repository.saveUserAccount(updated)
        }
    }

    fun updateUserAccount(updated: UserAccount) {
        viewModelScope.launch {
            repository.saveUserAccount(updated)
        }
    }

    fun switchUserRole(role: UserRole) {
        viewModelScope.launch {
            val updated = userAccount.value.copy(role = role)
            repository.saveUserAccount(updated)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userAccount.value.copy(notificationsEnabled = enabled)
            repository.saveUserAccount(updated)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userAccount.value.copy(biometricLockEnabled = enabled)
            repository.saveUserAccount(updated)
        }
    }

    fun toggleTwoFactor(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userAccount.value.copy(twoFactorAuthEnabled = enabled)
            repository.saveUserAccount(updated)
        }
    }

    fun setPreferredCurrency(currency: String) {
        viewModelScope.launch {
            val updated = userAccount.value.copy(preferredCurrency = currency)
            repository.saveUserAccount(updated)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            val updated = userAccount.value.copy(appTheme = mode.name)
            repository.saveUserAccount(updated)
        }
    }

    fun activateAnnualSubscription() {
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.YEAR, 1)
            val expiryStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(calendar.time)

            val updated = userAccount.value.copy(
                isSubscribed = true,
                subscriptionPlan = "Annual B2B Pro Pass ($1/Year)",
                subscriptionExpiryDate = expiryStr,
                subscriptionPriceUsd = 1.0
            )
            repository.saveUserAccount(updated)
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            val updated = userAccount.value.copy(
                isSubscribed = false,
                subscriptionPlan = "Free Tier",
                subscriptionExpiryDate = ""
            )
            repository.saveUserAccount(updated)
        }
    }
}
