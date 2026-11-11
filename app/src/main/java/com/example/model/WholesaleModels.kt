package com.example.model

data class CategoryItem(
    val id: String,
    val name: String,
    val shortName: String,
    val tagline: String,
    val subcategories: List<String>,
    val supplierCount: Int,
    val productCount: Int,
    val accentColorHex: Long = 0xFF0284C7
)

data class PriceTier(
    val minQty: Int,
    val maxQty: Int?, // null means and up
    val unitPrice: Double
)

data class WholesaleProduct(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val categoryId: String,
    val subcategory: String,
    val title: String,
    val description: String,
    val moq: Int,
    val moqUnit: String,
    val priceTiers: List<PriceTier>,
    val leadTimeDays: Int,
    val isReadyToShip: Boolean,
    val sampleAvailable: Boolean = true,
    val samplePrice: Double = 15.0,
    val specs: Map<String, String> = emptyMap()
)

data class SupplierVerification(
    val isVerified: Boolean = true,
    val isGoldSupplier: Boolean = true,
    val hasTradeAssurance: Boolean = true,
    val auditAgency: String = "TÜV Rheinland / Intertek",
    val auditReportNumber: String = "AUD-2024-8849",
    val tradeAssuranceCoverage: String = "$100,000 Order Guarantee",
    val onSiteInspectionDate: String = "Verified & Inspected 2024",
    val businessLicenseNumber: String = "BLN-9932014"
)

data class SupplierContact(
    val contactPerson: String = "Director of International Accounts",
    val email: String = "contact@factory.com",
    val phone: String = "+1 (800) 555-0199",
    val whatsapp: String = "+1 (800) 555-0199",
    val website: String = "https://www.tradesource.com",
    val fullAddress: String = "Export Processing Zone, Port District",
    val operatingHours: String = "Mon-Fri 08:30 - 18:00",
    val typicalResponseTime: String = "< 2 hours"
)

data class WholesaleSupplier(
    val id: String,
    val name: String,
    val categoryId: String,
    val subcategory: String,
    val originCountry: String,
    val cityState: String,
    val yearEstablished: Int,
    val isVerified: Boolean = true,
    val isGoldSupplier: Boolean = true,
    val hasTradeAssurance: Boolean = true,
    val rating: Float = 4.8f,
    val reviewCount: Int = 120,
    val responseRatePercent: Int = 98,
    val avgLeadTimeDays: Int = 12,
    val moq: Int = 50,
    val moqUnit: String = "pcs",
    val samplePrice: Double = 20.0,
    val certifications: List<String> = listOf("ISO 9001", "CE", "RoHS"),
    val factorySize: String = "25,000 m²",
    val staffCount: String = "150-300 staff",
    val annualExport: String = "$10M - $25M",
    val customization: List<String> = listOf("Custom Logo", "OEM/ODM", "Custom Packaging"),
    val paymentTerms: List<String> = listOf("T/T", "L/C", "Trade Assurance", "PayPal"),
    val about: String = "",
    val email: String = "contact@factory.com",
    val phone: String = "+1 (800) 555-0199",
    val isBookmarked: Boolean = false,
    val products: List<WholesaleProduct> = emptyList(),
    val verification: SupplierVerification = SupplierVerification(
        isVerified = isVerified,
        isGoldSupplier = isGoldSupplier,
        hasTradeAssurance = hasTradeAssurance
    ),
    val contact: SupplierContact = SupplierContact(
        email = email,
        phone = phone,
        fullAddress = "$cityState, $originCountry"
    )
)

typealias Supplier = WholesaleSupplier

data class RfqItem(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val productName: String,
    val categoryName: String,
    val targetQuantity: Int,
    val targetPrice: Double,
    val unit: String,
    val notes: String,
    val customLogo: Boolean,
    val customPackaging: Boolean,
    val shippingDestination: String,
    val status: String = "Pending Quote", // Draft, Pending Quote, Quoted, Sample Dispatched
    val timestamp: Long = System.currentTimeMillis(),
    val buyerName: String = "Global Procurement",
    val buyerEmail: String = "buyer@enterprise.com",
    val quotedPrice: Double? = null,
    val supplierNotes: String? = null
)

enum class DirectorySortOption(val displayName: String) {
    RECOMMENDED("Recommended"),
    RATING("Highest Rating"),
    MOQ_LOW_TO_HIGH("Lowest MOQ"),
    LEAD_TIME("Fastest Lead Time"),
    EXPERIENCE("Most Established")
}

data class FilterState(
    val searchQuery: String = "",
    val selectedCategoryId: String = "all",
    val selectedSubcategory: String = "all",
    val selectedCountry: String = "all",
    val verifiedOnly: Boolean = false,
    val tradeAssuranceOnly: Boolean = false,
    val lowMoqOnly: Boolean = false, // <= 50 pcs
    val fastLeadTimeOnly: Boolean = false, // <= 10 days
    val sortOption: DirectorySortOption = DirectorySortOption.RECOMMENDED
)

enum class UserRole(val displayName: String) {
    WHOLESALE_BUYER("Wholesale Buyer / Importer"),
    VERIFIED_SUPPLIER("Manufacturer / Supplier")
}

enum class AppThemeMode(val displayName: String, val icon: String) {
    SYSTEM("System Default", "⚙️"),
    LIGHT("Light Mode", "☀️"),
    DARK("Deep Dark", "🌙"),
    NAVY("Trade Midnight", "⚓")
}

data class UserAccount(
    val id: String = "usr_procure_01",
    val email: String = "procure@globaltrader.com",
    val fullName: String = "Alexander Vance",
    val companyName: String = "Apex Global Imports LLC",
    val role: UserRole = UserRole.WHOLESALE_BUYER,
    val taxIdOrVat: String = "US-EIN-88492019",
    val phone: String = "+1 (312) 555-0144",
    val shippingDestination: String = "Los Angeles Port, CA, USA",
    val preferredCurrency: String = "USD ($)",
    val notificationsEnabled: Boolean = true,
    val quoteAlertsEnabled: Boolean = true,
    val sampleTrackingEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val twoFactorAuthEnabled: Boolean = true,
    val isLoggedIn: Boolean = true,
    val verificationStatus: String = "Verified Importer",
    val appTheme: String = "DARK",
    val isSubscribed: Boolean = false,
    val subscriptionPlan: String = "Free Tier",
    val subscriptionExpiryDate: String = "",
    val subscriptionPriceUsd: Double = 1.0,
    val supplierProfileId: String? = null
)

