package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.WholesaleSeedData
import com.example.data.local.*
import com.example.model.PriceTier
import com.example.model.WholesaleProduct
import com.example.viewmodel.WholesaleViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WholesaleDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.wholesaleDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TradeSource", appName)
    }

    @Test
    fun `insert and retrieve wholesale supplier from Room DAO`() = runBlocking {
        val supplier = SupplierEntity(
            id = "sup_test_01",
            name = "Test Electronics Co.",
            categoryId = "cat_electronics",
            subcategory = "Audio & Headphones",
            originCountry = "Taiwan",
            cityState = "Taipei",
            yearEstablished = 2010,
            isVerified = true,
            isGoldSupplier = true,
            hasTradeAssurance = true,
            rating = 4.9f,
            reviewCount = 150,
            responseRatePercent = 99,
            avgLeadTimeDays = 8,
            moq = 50,
            moqUnit = "pcs",
            samplePrice = 25.0,
            certificationsDelimited = "ISO 9001,CE,RoHS",
            factorySize = "20,000 m²",
            staffCount = "300 staff",
            annualExport = "$20M",
            customizationDelimited = "Custom Logo,OEM/ODM",
            paymentTermsDelimited = "Trade Assurance,T/T",
            about = "High-end audio manufacturer.",
            email = "sales@test.com",
            phone = "+886 2 1234 5678",
            isBookmarked = false
        )

        dao.insertSupplier(supplier)

        val retrieved = dao.getSupplierById("sup_test_01")
        assertNotNull(retrieved)
        assertEquals("Test Electronics Co.", retrieved?.name)
        assertEquals("cat_electronics", retrieved?.categoryId)
        assertEquals(50, retrieved?.moq)

        val all = dao.getAllSuppliers().first()
        assertEquals(1, all.size)

        // Test bookmark toggle
        dao.updateBookmark("sup_test_01", true)
        val bookmarked = dao.getBookmarkedSuppliers().first()
        assertEquals(1, bookmarked.size)
        assertEquals("sup_test_01", bookmarked[0].id)
    }

    @Test
    fun `insert and retrieve product and RFQ inquiry`() = runBlocking {
        val product = ProductEntity(
            id = "prod_test_01",
            supplierId = "sup_test_01",
            supplierName = "Test Electronics Co.",
            categoryId = "cat_electronics",
            subcategory = "Audio & Headphones",
            title = "Noise Cancelling Headphones",
            description = "Studio ANC headphones.",
            moq = 50,
            moqUnit = "units",
            priceTiersRaw = "50:199:25.0;200:-1:20.0",
            leadTimeDays = 10,
            isReadyToShip = true,
            sampleAvailable = true,
            samplePrice = 35.0,
            specsRaw = "Driver=40mm;ANC=-35dB"
        )
        dao.insertProduct(product)

        val prods = dao.getProductsBySupplier("sup_test_01").first()
        assertEquals(1, prods.size)
        val model = prods[0].toModel()
        assertEquals(2, model.priceTiers.size)
        assertEquals(25.0, model.priceTiers[0].unitPrice, 0.01)
        assertEquals("40mm", model.specs["Driver"])

        // Insert RFQ
        val rfq = RfqEntity(
            id = "rfq_test_01",
            supplierId = "sup_test_01",
            supplierName = "Test Electronics Co.",
            productName = "Noise Cancelling Headphones",
            categoryName = "Audio & Headphones",
            targetQuantity = 100,
            targetPrice = 22.0,
            unit = "units",
            notes = "Need custom logo on packaging.",
            customLogo = true,
            customPackaging = true,
            shippingDestination = "Hamburg, Germany",
            status = "Pending Quote",
            timestamp = System.currentTimeMillis(),
            buyerName = "Global Sourcing Ltd",
            buyerEmail = "buyer@globalsourcing.com"
        )
        dao.insertRfq(rfq)

        val rfqs = dao.getAllRfqs().first()
        assertEquals(1, rfqs.size)
        assertEquals("Pending Quote", rfqs[0].status)

        // Update status
        dao.updateRfqStatus("rfq_test_01", "Quoted")
        val updated = dao.getAllRfqs().first()
        assertEquals("Quoted", updated[0].status)
    }

    @Test
    fun `verify seed data has 8 categories and verified suppliers`() {
        assertEquals(8, WholesaleSeedData.categories.size)
        assertTrue(WholesaleSeedData.suppliers.size >= 20)
        assertTrue(WholesaleSeedData.products.size >= 8)

        // Check each category has suppliers
        val categoryIds = WholesaleSeedData.categories.map { it.id }.toSet()
        val supplierCategories = WholesaleSeedData.suppliers.map { it.categoryId }.toSet()
        assertEquals(categoryIds, supplierCategories)
    }

    @Test
    fun `verify tier calculation logic`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = WholesaleViewModel(app)

        val sampleProd = WholesaleProduct(
            id = "test_p",
            supplierId = "sup_test",
            supplierName = "Apex",
            categoryId = "cat_electronics",
            subcategory = "Audio",
            title = "Earbuds",
            description = "Test earbuds",
            moq = 50,
            moqUnit = "units",
            priceTiers = listOf(
                PriceTier(50, 199, 20.0),
                PriceTier(200, 999, 15.0),
                PriceTier(1000, null, 10.0)
            ),
            leadTimeDays = 7,
            isReadyToShip = true
        )

        val result50 = vm.calculateTierCost(sampleProd, 50)
        assertEquals(20.0, result50.unitPrice, 0.01)
        assertEquals(1000.0, result50.subtotal, 0.01)

        val result250 = vm.calculateTierCost(sampleProd, 250)
        assertEquals(15.0, result250.unitPrice, 0.01)
        assertEquals(3750.0, result250.subtotal, 0.01)
        assertTrue(result250.savingsPercent > 0)
        assertTrue(result250.estimatedProfitMargin > 0)

        val result1500 = vm.calculateTierCost(sampleProd, 1500)
        assertEquals(10.0, result1500.unitPrice, 0.01)
        assertEquals(15000.0, result1500.subtotal, 0.01)
    }

    @Test
    fun `insert and retrieve user account settings in Room`() = runBlocking {
        val userEntity = UserAccountEntity(
            id = "active_user",
            email = "procure@globaltrader.com",
            fullName = "Alexander Vance",
            companyName = "Apex Global Imports LLC",
            roleName = "WHOLESALE_BUYER",
            taxIdOrVat = "US-EIN-88492019",
            phone = "+1 (312) 555-0144",
            shippingDestination = "Los Angeles Port, CA, USA",
            preferredCurrency = "USD ($)",
            notificationsEnabled = true,
            quoteAlertsEnabled = true,
            sampleTrackingEnabled = true,
            biometricLockEnabled = false,
            twoFactorAuthEnabled = true,
            isLoggedIn = true,
            verificationStatus = "Verified Importer",
            appTheme = "SYSTEM",
            isSubscribed = true,
            subscriptionPlan = "Annual B2B Pro Pass ($1/Year)",
            subscriptionExpiryDate = "Nov 11, 2027",
            subscriptionPriceUsd = 1.0,
            supplierProfileId = "sup_test_01"
        )

        dao.insertOrUpdateUserAccount(userEntity)
        val loaded = dao.getUserAccount().first()
        assertNotNull(loaded)
        assertEquals("Alexander Vance", loaded?.fullName)
        assertEquals("Apex Global Imports LLC", loaded?.companyName)
        assertEquals("USD ($)", loaded?.preferredCurrency)
        assertEquals("SYSTEM", loaded?.appTheme)
        assertEquals("sup_test_01", loaded?.supplierProfileId)
        assertTrue(loaded?.isLoggedIn == true)
        assertTrue(loaded?.isSubscribed == true)
        assertEquals("Annual B2B Pro Pass ($1/Year)", loaded?.subscriptionPlan)
        assertEquals(1.0, loaded?.subscriptionPriceUsd ?: 0.0, 0.01)

        // Test model conversion
        val model = loaded!!.toModel()
        assertEquals(com.example.model.UserRole.WHOLESALE_BUYER, model.role)
        assertEquals("US-EIN-88492019", model.taxIdOrVat)
        assertEquals("SYSTEM", model.appTheme)
        assertEquals("sup_test_01", model.supplierProfileId)
        assertTrue(model.isSubscribed)
        assertEquals(1.0, model.subscriptionPriceUsd, 0.01)
    }

    @Test
    fun `registered supplier can manually set and update own factory profile in Room`() = runBlocking {
        val repo = com.example.data.WholesaleRepository(db)

        val manualSupplier = com.example.model.WholesaleSupplier(
            id = "sup_manual_01",
            name = "Zenith Precision CNC Ltd",
            categoryId = "cat_machinery",
            subcategory = "CNC Machined Aluminum Components",
            originCountry = "Taiwan",
            cityState = "Taichung Industrial Zone",
            yearEstablished = 2012,
            isVerified = true,
            isGoldSupplier = true,
            hasTradeAssurance = true,
            rating = 5.0f,
            reviewCount = 14,
            responseRatePercent = 100,
            avgLeadTimeDays = 7,
            moq = 25,
            moqUnit = "pcs",
            samplePrice = 45.0,
            certifications = listOf("ISO 9001", "AS9100D", "CE"),
            factorySize = "35,000 m²",
            staffCount = "250-400 staff",
            annualExport = "$25M - $50M",
            customization = listOf("Custom 5-Axis Milling", "Anodized Finishes", "Custom Box"),
            paymentTerms = listOf("Trade Assurance", "T/T Wire"),
            about = "Leading aerospace-grade CNC machine shop delivering rapid tooling and tight-tolerance components.",
            email = "export@zenith-precision.tw",
            phone = "+886 4 2359 8811"
        )

        repo.addNewSupplier(manualSupplier)

        // Verify supplier is persisted and queryable
        val retrieved = db.wholesaleDao().getSupplierById("sup_manual_01")
        assertNotNull(retrieved)
        assertEquals("Zenith Precision CNC Ltd", retrieved?.name)
        assertEquals("Taiwan", retrieved?.originCountry)
        assertEquals("CNC Machined Aluminum Components", retrieved?.subcategory)
        assertEquals(25, retrieved?.moq)
        assertEquals("export@zenith-precision.tw", retrieved?.email)

        // Update profile manually (e.g. changing MOQ and adding certification)
        val updatedSupplier = manualSupplier.copy(
            moq = 10,
            avgLeadTimeDays = 5,
            about = "Expanded plant with new 5-axis Mazak lines."
        )
        repo.addNewSupplier(updatedSupplier)

        val updatedRecord = db.wholesaleDao().getSupplierById("sup_manual_01")
        assertNotNull(updatedRecord)
        assertEquals(10, updatedRecord?.moq)
        assertEquals(5, updatedRecord?.avgLeadTimeDays)
        assertEquals("Expanded plant with new 5-axis Mazak lines.", updatedRecord?.about)
    }

    @Test
    fun `verify annual subscription activation and cancellation in Repository`() = runBlocking {
        val repo = com.example.data.WholesaleRepository(db)
        val initialUser = com.example.model.UserAccount(id = "active_user", email = "test@user.com")
        repo.saveUserAccount(initialUser)

        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.YEAR, 1)
        val expiryStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(calendar.time)

        val subscribed = initialUser.copy(
            isSubscribed = true,
            subscriptionPlan = "Annual B2B Pro Pass ($1/Year)",
            subscriptionExpiryDate = expiryStr,
            subscriptionPriceUsd = 1.0
        )
        repo.saveUserAccount(subscribed)

        val retrieved = repo.userAccountFlow.first()
        assertTrue(retrieved.isSubscribed)
        assertEquals("Annual B2B Pro Pass ($1/Year)", retrieved.subscriptionPlan)
        assertEquals(1.0, retrieved.subscriptionPriceUsd, 0.01)
        assertEquals(expiryStr, retrieved.subscriptionExpiryDate)

        // Cancel subscription
        val cancelled = retrieved.copy(
            isSubscribed = false,
            subscriptionPlan = "Free Tier",
            subscriptionExpiryDate = ""
        )
        repo.saveUserAccount(cancelled)

        val afterCancel = repo.userAccountFlow.first()
        assertFalse(afterCancel.isSubscribed)
        assertEquals("Free Tier", afterCancel.subscriptionPlan)
    }

    @Test
    fun `verify currency mode conversion and formatting across 18 currencies`() {
        val usd = com.example.model.GlobalCurrencies.findByCode("USD")
        val eur = com.example.model.GlobalCurrencies.findByCode("EUR")
        val jpy = com.example.model.GlobalCurrencies.findByCode("JPY")

        assertEquals("$100.00", usd.format(100.0))
        assertEquals("€92.00", eur.format(100.0))
        assertEquals("¥15,200", jpy.format(100.0))

        assertEquals(18, com.example.model.GlobalCurrencies.allCurrencies.size)
        assertTrue(com.example.model.GlobalCountries.allCountries.size >= 21)
    }

    @Test
    fun `verify centralized Material3 typography and shapes`() {
        val typography = com.example.ui.theme.Typography
        assertNotNull(typography.displayLarge)
        assertNotNull(typography.headlineMedium)
        assertNotNull(typography.titleMedium)
        assertNotNull(typography.bodyLarge)
        assertNotNull(typography.labelSmall)

        val shapes = com.example.ui.theme.TradeShapes
        assertNotNull(shapes.medium)
        assertNotNull(shapes.large)
    }
}
