package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyMode
import com.example.model.GlobalCountries
import com.example.model.GlobalCurrencies
import com.example.model.WholesaleSupplier
import com.example.ui.theme.TradeBlue
import com.example.ui.theme.TradeEmerald
import com.example.ui.theme.TradeGold
import com.example.util.SupplierContactHelper

@Composable
fun SupplierCard(
    supplier: WholesaleSupplier,
    onViewSupplier: () -> Unit,
    onRequestRfq: () -> Unit,
    onToggleBookmark: () -> Unit,
    currency: CurrencyMode = GlobalCurrencies.defaultCurrency,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("supplier_card_${supplier.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (supplier.isVerified) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TradeEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, TradeEmerald.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = TradeEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "VERIFIED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TradeEmerald
                                )
                            }
                        }
                    }

                    if (supplier.isGoldSupplier) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TradeGold.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, TradeGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = TradeGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "GOLD SUPPLIER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TradeGold
                                )
                            }
                        }
                    }

                    if (supplier.hasTradeAssurance) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TradeBlue.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, TradeBlue.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TradeBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "ASSURED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = TradeBlue
                                )
                            }
                        }
                    }
                }

                // Bookmark icon button
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_button_${supplier.id}")
                ) {
                    Icon(
                        imageVector = if (supplier.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (supplier.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Supplier Name
            Text(
                text = supplier.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subcategory & Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = supplier.subcategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = GlobalCountries.getFlagForCountry(supplier.originCountry),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${supplier.cityState}, ${supplier.originCountry}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Metrics Pill Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = TradeGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.2f", supplier.rating),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${supplier.reviewCount} reviews",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // MOQ
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${supplier.moq} ${supplier.moqUnit}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Min. Order",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Lead Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${supplier.avgLeadTimeDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lead Time",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Response Rate
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${supplier.responseRatePercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TradeEmerald
                    )
                    Text(
                        text = "Response",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customization tags preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                supplier.customization.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Direct Sourcing Sample & Contact Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Sample: ${currency.format(supplier.samplePrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Direct Email Factory
                    IconButton(
                        onClick = {
                            val (subj, body) = SupplierContactHelper.buildRfqEmailContent(
                                supplierName = supplier.name,
                                productName = supplier.products.firstOrNull()?.title ?: "${supplier.subcategory} Inquiry",
                                quantity = supplier.moq,
                                unit = supplier.moqUnit,
                                targetPrice = supplier.products.firstOrNull()?.priceTiers?.firstOrNull()?.unitPrice ?: 20.0,
                                destination = "Direct Destination Port",
                                customLogo = true,
                                customPackaging = true,
                                notes = "Please send full catalog, FOB quotation, and sample dispatch timeline.",
                                buyerName = "Wholesale Procurement Director",
                                buyerEmail = "buyer@enterprise.com",
                                currencySymbol = currency.symbol
                            )
                            SupplierContactHelper.launchEmail(context, supplier.email, subj, body)
                        },
                        modifier = Modifier.size(32.dp).testTag("direct_email_${supplier.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Direct Email Factory",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Direct Phone / Call
                    IconButton(
                        onClick = { SupplierContactHelper.launchPhone(context, supplier.phone) },
                        modifier = Modifier.size(32.dp).testTag("direct_call_${supplier.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Direct Call Factory",
                            tint = TradeEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Direct WhatsApp
                    IconButton(
                        onClick = {
                            val msg = SupplierContactHelper.buildQuickWhatsAppMessage(
                                supplierName = supplier.name,
                                productName = supplier.products.firstOrNull()?.title ?: supplier.subcategory,
                                quantity = supplier.moq,
                                unit = supplier.moqUnit,
                                buyerName = "Procurement Buyer"
                            )
                            SupplierContactHelper.launchWhatsApp(context, supplier.phone, msg)
                        },
                        modifier = Modifier.size(32.dp).testTag("direct_whatsapp_${supplier.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Direct WhatsApp",
                            tint = TradeEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRequestRfq,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rfq_quote_btn_${supplier.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RequestQuote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Get RFQ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onViewSupplier,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("view_supplier_btn_${supplier.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = "View Factory",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
