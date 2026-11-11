package com.example.model

data class CurrencyMode(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToUsd: Double,
    val flag: String
) {
    fun convert(usdAmount: Double): Double = usdAmount * rateToUsd

    fun format(usdAmount: Double): String {
        val converted = convert(usdAmount)
        return when {
            rateToUsd >= 1000 -> "$symbol${String.format("%,.0f", converted)}"
            rateToUsd >= 100 -> "$symbol${String.format("%,.0f", converted)}"
            else -> "$symbol${String.format("%,.2f", converted)}"
        }
    }
}

object GlobalCurrencies {
    val allCurrencies = listOf(
        CurrencyMode("USD", "US Dollar", "$", 1.0, "🇺🇸"),
        CurrencyMode("EUR", "Euro", "€", 0.92, "🇪🇺"),
        CurrencyMode("GBP", "British Pound", "£", 0.78, "🇬🇧"),
        CurrencyMode("JPY", "Japanese Yen", "¥", 152.0, "🇯🇵"),
        CurrencyMode("CNY", "Chinese Yuan", "¥", 7.24, "🇨🇳"),
        CurrencyMode("CAD", "Canadian Dollar", "C$", 1.38, "🇨🇦"),
        CurrencyMode("AUD", "Australian Dollar", "A$", 1.52, "🇦🇺"),
        CurrencyMode("INR", "Indian Rupee", "₹", 83.5, "🇮🇳"),
        CurrencyMode("TWD", "New Taiwan Dollar", "NT$", 32.2, "🇹🇼"),
        CurrencyMode("VND", "Vietnamese Dong", "₫", 25400.0, "🇻🇳"),
        CurrencyMode("KRW", "South Korean Won", "₩", 1380.0, "🇰🇷"),
        CurrencyMode("CHF", "Swiss Franc", "CHF ", 0.89, "🇨🇭"),
        CurrencyMode("MXN", "Mexican Peso", "Mex$", 18.2, "🇲🇽"),
        CurrencyMode("BRL", "Brazilian Real", "R$", 5.45, "🇧🇷"),
        CurrencyMode("AED", "UAE Dirham", "AED ", 3.67, "🇦🇪"),
        CurrencyMode("SGD", "Singapore Dollar", "S$", 1.35, "🇸🇬"),
        CurrencyMode("THB", "Thai Baht", "฿", 36.8, "🇹🇭"),
        CurrencyMode("PLN", "Polish Zloty", "zł ", 3.98, "🇵🇱")
    )

    val defaultCurrency = allCurrencies[0] // USD

    fun findByCode(code: String): CurrencyMode {
        return allCurrencies.find { it.code.equals(code, ignoreCase = true) || code.contains(it.code) } ?: defaultCurrency
    }
}

data class CountryHub(
    val code: String,
    val name: String,
    val flag: String,
    val region: String,
    val defaultCurrencyCode: String,
    val keySpecialties: List<String>
)

object GlobalCountries {
    val allCountries = listOf(
        CountryHub("TW", "Taiwan", "🇹🇼", "Asia-Pacific", "TWD", listOf("Semiconductors", "Precision Fasteners", "Ergonomic Chairs", "Audio Electronics")),
        CountryHub("VN", "Vietnam", "🇻🇳", "Southeast Asia", "VND", listOf("Eco Packaging", "Activewear Apparel", "GaN Power Tech", "Footwear OEM")),
        CountryHub("DE", "Germany", "🇩🇪", "Europe", "EUR", listOf("Stainless Cookware", "Precision Machinery", "Industrial Automation")),
        CountryHub("US", "United States", "🇺🇸", "North America", "USD", listOf("Aerospace 5-Axis CNC", "Reinforced Shipping Packaging", "Specialty Fasteners")),
        CountryHub("IN", "India", "🇮🇳", "South Asia", "INR", listOf("Single-Origin Spices", "Organic Cotton Denim", "Natural Textiles")),
        CountryHub("KR", "South Korea", "🇰🇷", "East Asia", "KRW", listOf("K-Beauty Bio Serums", "EV Level 2 Chargers", "Smart Home IoT")),
        CountryHub("JP", "Japan", "🇯🇵", "East Asia", "JPY", listOf("Ceramic Brake Pads", "Titanium Wearable Biosensors", "Robotics Hardware")),
        CountryHub("IT", "Italy", "🇮🇹", "Europe", "EUR", listOf("Luxury Cosmetic Packaging", "Design Tableware", "Fine Leather Shoes")),
        CountryHub("TH", "Thailand", "🇹🇭", "Southeast Asia", "THB", listOf("Stoneware Ceramics", "Natural Rubber", "Packaged Agro Foods")),
        CountryHub("MX", "Mexico", "🇲🇽", "Latin America", "MXN", listOf("Luxury Rigid Boxes", "Near-shore Electronics", "Auto Harnesses")),
        CountryHub("PL", "Poland", "🇵🇱", "Europe", "PLN", listOf("Heavy Duty Industrial PPE", "Protective Workwear", "Metal Fabrications")),
        CountryHub("PT", "Portugal", "🇵🇹", "Europe", "EUR", listOf("Handcrafted Footwear", "Cork Materials", "Premium Knitwear")),
        CountryHub("GR", "Greece", "🇬🇷", "Europe", "EUR", listOf("Extra Virgin Olive Oils", "Mediterranean Agro", "Specialty Olives")),
        CountryHub("CO", "Colombia", "🇨🇴", "South America", "USD", listOf("Specialty Arabica Coffee", "Cacao", "Exotic Fruits")),
        CountryHub("MA", "Morocco", "🇲🇦", "North Africa", "EUR", listOf("Pure Organic Argan Oil", "Cold Pressed Seed Oils", "Handmade Pottery")),
        CountryHub("CN", "China", "🇨🇳", "East Asia", "CNY", listOf("Consumer Electronics", "Smart Gadgets", "Plastic Injection")),
        CountryHub("GB", "United Kingdom", "🇬🇧", "Europe", "GBP", listOf("Specialty Chemicals", "Gourmet Beverages", "Craft Cosmetics")),
        CountryHub("AU", "Australia", "🇦🇺", "Oceania", "AUD", listOf("Wool & Merino", "Botanical Extracts", "Dairy & Honey")),
        CountryHub("CA", "Canada", "🇨🇦", "North America", "CAD", listOf("Eco Forestry Paper", "Cold Climate Gear", "Specialty Agri")),
        CountryHub("AE", "United Arab Emirates", "🇦🇪", "Middle East", "AED", listOf("Global Trade Hub", "Perfumes & Oud", "Re-export Logistics")),
        CountryHub("BR", "Brazil", "🇧🇷", "South America", "BRL", listOf("Raw Green Coffee", "Soybean Agro", "Footwear Components"))
    )

    fun getFlagForCountry(countryName: String): String {
        return allCountries.find { it.name.equals(countryName, ignoreCase = true) }?.flag ?: "🌐"
    }
}
