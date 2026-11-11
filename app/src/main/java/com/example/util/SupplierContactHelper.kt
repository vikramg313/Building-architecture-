package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SupplierContactHelper {

    fun launchEmail(
        context: Context,
        recipientEmail: String,
        subject: String,
        body: String
    ) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(intent, "Select Email App"))
        } catch (e: Exception) {
            Toast.makeText(context, "No email client found: $recipientEmail", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchPhone(
        context: Context,
        phoneNumber: String
    ) {
        try {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to dial $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchWhatsApp(
        context: Context,
        phoneNumber: String,
        message: String = ""
    ) {
        try {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
            val encodedMsg = Uri.encode(message)
            val url = if (encodedMsg.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?phone=$cleanPhone"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to phone dial
            launchPhone(context, phoneNumber)
        }
    }

    fun buildRfqEmailContent(
        supplierName: String,
        productName: String,
        quantity: Int,
        unit: String,
        targetPrice: Double,
        destination: String,
        customLogo: Boolean,
        customPackaging: Boolean,
        notes: String,
        buyerName: String,
        buyerEmail: String,
        currencySymbol: String = "$"
    ): Pair<String, String> {
        val subject = "Wholesale Sourcing RFQ: $productName ($quantity $unit) - $buyerName"
        val body = buildString {
            appendLine("Dear ${supplierName} International Sourcing Team,")
            appendLine()
            appendLine("We would like to request an official wholesale commercial quotation for the following product:")
            appendLine("--------------------------------------------------")
            appendLine("• Product / Specification: $productName")
            appendLine("• Required Order Volume: $quantity $unit")
            appendLine("• Target Price: $currencySymbol${String.format("%.2f", targetPrice)} / $unit")
            appendLine("• Est. Order Value: $currencySymbol${String.format("%,.2f", quantity * targetPrice)}")
            appendLine("• Port of Delivery: $destination")
            appendLine("• Custom OEM Branding: ${if (customLogo) "YES (Custom Logo Required)" else "Standard"}")
            appendLine("• Retail Packaging: ${if (customPackaging) "YES (Custom Barcode / Box Required)" else "Standard Export Carton"}")
            appendLine("--------------------------------------------------")
            appendLine("Additional Requirements / Technical Specs:")
            appendLine(notes.ifBlank { "Please provide lead time, sample dispatch availability, and FOB/CIF pricing." })
            appendLine()
            appendLine("Buyer Procurement Contact:")
            appendLine("Name: $buyerName")
            appendLine("Email: $buyerEmail")
            appendLine("Platform: TradeSource B2B Verified Directory")
            appendLine()
            appendLine("We look forward to your proforma quotation and sample dispatch timeline.")
            appendLine("Best regards,")
            appendLine(buyerName)
        }
        return Pair(subject, body)
    }

    fun buildQuickWhatsAppMessage(
        supplierName: String,
        productName: String,
        quantity: Int,
        unit: String,
        buyerName: String
    ): String {
        return "Hello $supplierName Export Desk, this is $buyerName via TradeSource Directory. We are inquiring about sourcing $quantity $unit of $productName. Please share catalog, lead time, and MOQ terms."
    }
}
