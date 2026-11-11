package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PriceTier
import com.example.model.RfqItem
import com.example.model.WholesaleProduct
import com.example.model.WholesaleSupplier

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val subcategory: String,
    val originCountry: String,
    val cityState: String,
    val yearEstablished: Int,
    val isVerified: Boolean,
    val isGoldSupplier: Boolean,
    val hasTradeAssurance: Boolean,
    val rating: Float,
    val reviewCount: Int,
    val responseRatePercent: Int,
    val avgLeadTimeDays: Int,
    val moq: Int,
    val moqUnit: String,
    val samplePrice: Double,
    val certificationsDelimited: String, // comma-separated
    val factorySize: String,
    val staffCount: String,
    val annualExport: String,
    val customizationDelimited: String, // comma-separated
    val paymentTermsDelimited: String, // comma-separated
    val about: String,
    val email: String,
    val phone: String,
    val isBookmarked: Boolean = false
) {
    fun toModel(products: List<WholesaleProduct> = emptyList()): WholesaleSupplier {
        return WholesaleSupplier(
            id = id,
            name = name,
            categoryId = categoryId,
            subcategory = subcategory,
            originCountry = originCountry,
            cityState = cityState,
            yearEstablished = yearEstablished,
            isVerified = isVerified,
            isGoldSupplier = isGoldSupplier,
            hasTradeAssurance = hasTradeAssurance,
            rating = rating,
            reviewCount = reviewCount,
            responseRatePercent = responseRatePercent,
            avgLeadTimeDays = avgLeadTimeDays,
            moq = moq,
            moqUnit = moqUnit,
            samplePrice = samplePrice,
            certifications = if (certificationsDelimited.isBlank()) emptyList() else certificationsDelimited.split(",").map { it.trim() },
            factorySize = factorySize,
            staffCount = staffCount,
            annualExport = annualExport,
            customization = if (customizationDelimited.isBlank()) emptyList() else customizationDelimited.split(",").map { it.trim() },
            paymentTerms = if (paymentTermsDelimited.isBlank()) emptyList() else paymentTermsDelimited.split(",").map { it.trim() },
            about = about,
            email = email,
            phone = phone,
            isBookmarked = isBookmarked,
            products = products
        )
    }

    companion object {
        fun fromModel(model: WholesaleSupplier): SupplierEntity {
            return SupplierEntity(
                id = model.id,
                name = model.name,
                categoryId = model.categoryId,
                subcategory = model.subcategory,
                originCountry = model.originCountry,
                cityState = model.cityState,
                yearEstablished = model.yearEstablished,
                isVerified = model.isVerified,
                isGoldSupplier = model.isGoldSupplier,
                hasTradeAssurance = model.hasTradeAssurance,
                rating = model.rating,
                reviewCount = model.reviewCount,
                responseRatePercent = model.responseRatePercent,
                avgLeadTimeDays = model.avgLeadTimeDays,
                moq = model.moq,
                moqUnit = model.moqUnit,
                samplePrice = model.samplePrice,
                certificationsDelimited = model.certifications.joinToString(","),
                factorySize = model.factorySize,
                staffCount = model.staffCount,
                annualExport = model.annualExport,
                customizationDelimited = model.customization.joinToString(","),
                paymentTermsDelimited = model.paymentTerms.joinToString(","),
                about = model.about,
                email = model.email,
                phone = model.phone,
                isBookmarked = model.isBookmarked
            )
        }
    }
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val supplierId: String,
    val supplierName: String,
    val categoryId: String,
    val subcategory: String,
    val title: String,
    val description: String,
    val moq: Int,
    val moqUnit: String,
    val priceTiersRaw: String, // e.g. "50:199:28.5;200:999:24.0;1000:-1:19.8"
    val leadTimeDays: Int,
    val isReadyToShip: Boolean,
    val sampleAvailable: Boolean,
    val samplePrice: Double,
    val specsRaw: String // key=value;key2=value2
) {
    fun toModel(): WholesaleProduct {
        val tiers = if (priceTiersRaw.isBlank()) emptyList() else {
            priceTiersRaw.split(";").mapNotNull { part ->
                val tokens = part.split(":")
                if (tokens.size >= 3) {
                    val min = tokens[0].toIntOrNull() ?: 1
                    val maxVal = tokens[1].toIntOrNull()
                    val max = if (maxVal == null || maxVal < 0) null else maxVal
                    val price = tokens[2].toDoubleOrNull() ?: 0.0
                    PriceTier(min, max, price)
                } else null
            }
        }
        val specsMap = if (specsRaw.isBlank()) emptyMap() else {
            specsRaw.split(";").mapNotNull {
                val kv = it.split("=")
                if (kv.size == 2) kv[0].trim() to kv[1].trim() else null
            }.toMap()
        }

        return WholesaleProduct(
            id = id,
            supplierId = supplierId,
            supplierName = supplierName,
            categoryId = categoryId,
            subcategory = subcategory,
            title = title,
            description = description,
            moq = moq,
            moqUnit = moqUnit,
            priceTiers = tiers,
            leadTimeDays = leadTimeDays,
            isReadyToShip = isReadyToShip,
            sampleAvailable = sampleAvailable,
            samplePrice = samplePrice,
            specs = specsMap
        )
    }

    companion object {
        fun fromModel(model: WholesaleProduct): ProductEntity {
            val tiersRaw = model.priceTiers.joinToString(";") {
                "${it.minQty}:${it.maxQty ?: -1}:${it.unitPrice}"
            }
            val specsRaw = model.specs.entries.joinToString(";") { "${it.key}=${it.value}" }
            return ProductEntity(
                id = model.id,
                supplierId = model.supplierId,
                supplierName = model.supplierName,
                categoryId = model.categoryId,
                subcategory = model.subcategory,
                title = model.title,
                description = model.description,
                moq = model.moq,
                moqUnit = model.moqUnit,
                priceTiersRaw = tiersRaw,
                leadTimeDays = model.leadTimeDays,
                isReadyToShip = model.isReadyToShip,
                sampleAvailable = model.sampleAvailable,
                samplePrice = model.samplePrice,
                specsRaw = specsRaw
            )
        }
    }
}

@Entity(tableName = "rfq_inquiries")
data class RfqEntity(
    @PrimaryKey val id: String,
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
    val status: String,
    val timestamp: Long,
    val buyerName: String,
    val buyerEmail: String,
    val quotedPrice: Double? = null,
    val supplierNotes: String? = null
) {
    fun toModel(): RfqItem {
        return RfqItem(
            id = id,
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
            status = status,
            timestamp = timestamp,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            quotedPrice = quotedPrice,
            supplierNotes = supplierNotes
        )
    }

    companion object {
        fun fromModel(model: RfqItem): RfqEntity {
            return RfqEntity(
                id = model.id,
                supplierId = model.supplierId,
                supplierName = model.supplierName,
                productName = model.productName,
                categoryName = model.categoryName,
                targetQuantity = model.targetQuantity,
                targetPrice = model.targetPrice,
                unit = model.unit,
                notes = model.notes,
                customLogo = model.customLogo,
                customPackaging = model.customPackaging,
                shippingDestination = model.shippingDestination,
                status = model.status,
                timestamp = model.timestamp,
                buyerName = model.buyerName,
                buyerEmail = model.buyerEmail,
                quotedPrice = model.quotedPrice,
                supplierNotes = model.supplierNotes
            )
        }
    }
}

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val id: String = "active_user",
    val email: String,
    val fullName: String,
    val companyName: String,
    val roleName: String,
    val taxIdOrVat: String,
    val phone: String,
    val shippingDestination: String,
    val preferredCurrency: String,
    val notificationsEnabled: Boolean,
    val quoteAlertsEnabled: Boolean,
    val sampleTrackingEnabled: Boolean,
    val biometricLockEnabled: Boolean,
    val twoFactorAuthEnabled: Boolean,
    val isLoggedIn: Boolean,
    val verificationStatus: String,
    val appTheme: String = "DARK",
    val isSubscribed: Boolean = false,
    val subscriptionPlan: String = "Free Tier",
    val subscriptionExpiryDate: String = "",
    val subscriptionPriceUsd: Double = 1.0,
    val supplierProfileId: String? = null
) {
    fun toModel(): com.example.model.UserAccount {
        val role = try {
            com.example.model.UserRole.valueOf(roleName)
        } catch (_: Exception) {
            com.example.model.UserRole.WHOLESALE_BUYER
        }
        return com.example.model.UserAccount(
            id = id,
            email = email,
            fullName = fullName,
            companyName = companyName,
            role = role,
            taxIdOrVat = taxIdOrVat,
            phone = phone,
            shippingDestination = shippingDestination,
            preferredCurrency = preferredCurrency,
            notificationsEnabled = notificationsEnabled,
            quoteAlertsEnabled = quoteAlertsEnabled,
            sampleTrackingEnabled = sampleTrackingEnabled,
            biometricLockEnabled = biometricLockEnabled,
            twoFactorAuthEnabled = twoFactorAuthEnabled,
            isLoggedIn = isLoggedIn,
            verificationStatus = verificationStatus,
            appTheme = appTheme,
            isSubscribed = isSubscribed,
            subscriptionPlan = subscriptionPlan,
            subscriptionExpiryDate = subscriptionExpiryDate,
            subscriptionPriceUsd = subscriptionPriceUsd,
            supplierProfileId = supplierProfileId
        )
    }

    companion object {
        fun fromModel(model: com.example.model.UserAccount): UserAccountEntity {
            return UserAccountEntity(
                id = "active_user",
                email = model.email,
                fullName = model.fullName,
                companyName = model.companyName,
                roleName = model.role.name,
                taxIdOrVat = model.taxIdOrVat,
                phone = model.phone,
                shippingDestination = model.shippingDestination,
                preferredCurrency = model.preferredCurrency,
                notificationsEnabled = model.notificationsEnabled,
                quoteAlertsEnabled = model.quoteAlertsEnabled,
                sampleTrackingEnabled = model.sampleTrackingEnabled,
                biometricLockEnabled = model.biometricLockEnabled,
                twoFactorAuthEnabled = model.twoFactorAuthEnabled,
                isLoggedIn = model.isLoggedIn,
                verificationStatus = model.verificationStatus,
                appTheme = model.appTheme,
                isSubscribed = model.isSubscribed,
                subscriptionPlan = model.subscriptionPlan,
                subscriptionExpiryDate = model.subscriptionExpiryDate,
                subscriptionPriceUsd = model.subscriptionPriceUsd,
                supplierProfileId = model.supplierProfileId
            )
        }
    }
}
