package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.payment.PaymentSettings
import com.example.ui.components.ChaiLogo
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun SubscriptionDialog(
    paymentSettings: PaymentSettings = PaymentSettings(),
    onDismiss: () -> Unit,
    onBuyPremium: (plan: String, paymentMethod: String, senderNumber: String, trxId: String, amount: String, screenshotNote: String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current

    // Payment method: "bKash", "Nagad", or "Crypto"
    var selectedMethod by remember { mutableStateOf("bKash") }
    // Plan: "Weekly" or "Yearly"
    var selectedPlan by remember { mutableStateOf("Weekly") }

    // Form states
    var senderNumber by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var additionalNote by remember { mutableStateOf("") }

    // Photo picker for crypto screenshot
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            screenshotUri = uri
            Toast.makeText(context, "✅ পেমেন্ট স্ক্রিনশট যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
        }
    }

    val isCrypto = selectedMethod == "Crypto"
    val isWeekly = selectedPlan == "Weekly"

    // Pricing calculation based on currency and settings
    val currentPriceLabel = if (isCrypto) {
        if (isWeekly) paymentSettings.cryptoWeeklyPrice else paymentSettings.cryptoYearlyPrice
    } else {
        if (isWeekly) "৳${paymentSettings.bdtWeeklyPrice}" else "৳${paymentSettings.bdtYearlyPrice}"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChaiBlack)
                .testTag("subscription_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Close 'X' and Chai Logo
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("close_subscription_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    ChaiLogo(
                        modifier = Modifier.align(Alignment.Center),
                        iconWidth = 34.dp,
                        iconHeight = 18.dp,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Premium Badge Header
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE50914), Color(0xFFFF5252))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chai Ultra & Premium",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "আনলিমিটেড মেসেজ ও সুপারফাস্ট রেসপন্স",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "পেমেন্ট মেথড নির্বাচন করে সহজে প্রিমিয়াম আনলক করুন",
                    color = ChaiTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                // ==========================================
                // 1. PAYMENT METHOD TABS (bKash, Nagad, Crypto)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF14141C))
                        .border(1.dp, Color(0xFF262636), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // bKash Tab
                    val isBkash = selectedMethod == "bKash"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBkash) Color(0xFFE2136E) else Color.Transparent)
                            .clickable { selectedMethod = "bKash" }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "bKash বিকাশ",
                            color = if (isBkash) Color.White else Color(0xFFB0B0C0),
                            fontWeight = if (isBkash) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    // Nagad Tab
                    val isNagad = selectedMethod == "Nagad"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isNagad) Color(0xFFF7941D) else Color.Transparent)
                            .clickable { selectedMethod = "Nagad" }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nagad নগদ",
                            color = if (isNagad) Color.White else Color(0xFFB0B0C0),
                            fontWeight = if (isNagad) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }

                    // Crypto Tab
                    val isCryptoSelected = selectedMethod == "Crypto"
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCryptoSelected) {
                                    Brush.horizontalGradient(listOf(Color(0xFF8247E5), Color(0xFF26A17B)))
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { selectedMethod = "Crypto" }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Crypto USDT",
                                color = if (isCryptoSelected) Color.White else Color(0xFFB0B0C0),
                                fontWeight = if (isCryptoSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 2. PLAN SELECTOR (Weekly vs Yearly)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Weekly Plan Card
                    val weeklyPriceText = if (isCrypto) paymentSettings.cryptoWeeklyPrice else "৳${paymentSettings.bdtWeeklyPrice}"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isWeekly) Color(0xFF221518) else Color(0xFF191922))
                            .border(
                                width = if (isWeekly) 2.dp else 1.dp,
                                color = if (isWeekly) ChaiRed else ChaiBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedPlan = "Weekly" }
                            .padding(12.dp)
                            .testTag("plan_weekly_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Weekly Plan",
                                color = ChaiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = weeklyPriceText,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৭ দিন আনলিমিটেড",
                                color = if (isWeekly) ChaiRed else ChaiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Yearly Plan Card
                    val isYearly = selectedPlan == "Yearly"
                    val yearlyPriceText = if (isCrypto) paymentSettings.cryptoYearlyPrice else "৳${paymentSettings.bdtYearlyPrice}"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isYearly) Color(0xFF221518) else Color(0xFF191922))
                            .border(
                                width = if (isYearly) 2.dp else 1.dp,
                                color = if (isYearly) ChaiRed else ChaiBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedPlan = "Yearly" }
                            .testTag("plan_yearly_button")
                    ) {
                        // Badge Save 83%
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 8.dp, topEnd = 14.dp))
                                .background(Color(0xFFFFB300))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Save 83%",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Yearly Plan",
                                color = ChaiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = yearlyPriceText,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৩৬৫ দিন আনলিমিটেড",
                                color = if (isYearly) ChaiRed else ChaiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // 3. PAYMENT DETAILS CARD (DYNAMIC: bKash/Nagad vs Crypto)
                // =========================================================================
                if (!isCrypto) {
                    // ----------------------------------------------------
                    // A. bKash / Nagad Payment Page
                    // ----------------------------------------------------
                    val currentNumber = if (selectedMethod == "bKash") paymentSettings.bkashNumber else paymentSettings.nagadNumber
                    val brandColor = if (selectedMethod == "bKash") Color(0xFFE2136E) else Color(0xFFF7941D)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF161622))
                            .border(1.dp, brandColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .testTag("payment_instructions_card")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = brandColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$selectedMethod এ Send Money করুন",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF262638))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Send Money Number with Copy Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F0F16))
                                    .border(1.dp, ChaiBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "$selectedMethod Personal Number:",
                                        color = ChaiTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = currentNumber,
                                        color = Color(0xFFFFD54F),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("$selectedMethod Number", currentNumber)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "$currentNumber নম্বরটি কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A38)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("copy_number_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("কপি", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Instructions banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1D1B28))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "📌 উপরের নম্বরে $currentPriceLabel টাকা Send Money করে TrxID এবং যে নম্বর দিয়ে টাকা পাঠিয়েছেন তা নিচে লিখে সাবমিট দিন।",
                                    color = Color(0xFFE2E2F0),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Input: Sender Number
                            OutlinedTextField(
                                value = senderNumber,
                                onValueChange = { senderNumber = it },
                                label = { Text("যে নম্বর দিয়ে টাকা পাঠিয়েছেন (Sender Number)", color = ChaiTextSecondary, fontSize = 11.sp) },
                                placeholder = { Text("01XXXXXXXXX", color = Color.Gray, fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sender_number_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = brandColor,
                                    unfocusedBorderColor = ChaiBorder,
                                    focusedContainerColor = Color(0xFF0F0F16),
                                    unfocusedContainerColor = Color(0xFF0F0F16)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Input: TrxID
                            OutlinedTextField(
                                value = trxId,
                                onValueChange = { trxId = it },
                                label = { Text("Transaction ID (TrxID) / রেফারেন্স", color = ChaiTextSecondary, fontSize = 11.sp) },
                                placeholder = { Text("e.g. BL92A8XYZ", color = Color.Gray, fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trx_id_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = brandColor,
                                    unfocusedBorderColor = ChaiBorder,
                                    focusedContainerColor = Color(0xFF0F0F16),
                                    unfocusedContainerColor = Color(0xFF0F0F16)
                                )
                            )
                        }
                    }
                } else {
                    // ----------------------------------------------------
                    // B. Crypto (Polygon USDT) Payment Page
                    // ----------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF161524))
                            .border(1.dp, Color(0xFF8247E5).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .testTag("crypto_payment_card")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(0xFF8247E5), Color(0xFF26A17B)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("₮", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "USDT (Polygon Network) পেমেন্ট",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Binance • Bybit • Bitget থেকে পাঠান",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF2B2840))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Network and Wallet Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF8247E5).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF8247E5).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Network: ${paymentSettings.cryptoNetwork}",
                                        color = Color(0xFFD1B3FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Amount: $currentPriceLabel",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Crypto Wallet Address Display Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0C0B14))
                                    .border(1.dp, Color(0xFF2F2B48), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Polygon USDT Deposit Address:",
                                        color = Color(0xFF9E9AC0),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("USDT Polygon Address", paymentSettings.cryptoAddress)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Crypto Address কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8247E5)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("copy_crypto_address_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("কপি", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = paymentSettings.cryptoAddress,
                                    color = Color(0xFF76FF03),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Instructions Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1B30))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "📌 নির্দেশিকা (Instructions):",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "১. Binance, Bybit অথবা Bitget থেকে USDT নির্বাচন করুন।\n২. Network হিসেবে Polygon সিলেক্ট করে $currentPriceLabel সেন্ড করুন।\n৩. ডলার সেন্ড করে নিচে TxID লিখুন এবং স্ক্রিনশট সিলেক্ট করে সাবমিট দিন।",
                                        color = Color(0xFFE2E2F0),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Input: Sender Wallet or TxID
                            OutlinedTextField(
                                value = trxId,
                                onValueChange = { trxId = it },
                                label = { Text("TxID (Transaction Hash) অথবা প্রেরক Wallet", color = ChaiTextSecondary, fontSize = 11.sp) },
                                placeholder = { Text("0x... অথবা Transaction Hash", color = Color.Gray, fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("crypto_txid_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF8247E5),
                                    unfocusedBorderColor = ChaiBorder,
                                    focusedContainerColor = Color(0xFF0C0B14),
                                    unfocusedContainerColor = Color(0xFF0C0B14)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Screenshot Picker Button & Thumbnail
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0C0B14))
                                    .border(1.dp, ChaiBorder, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (screenshotUri != null) {
                                        AsyncImage(
                                            model = screenshotUri,
                                            contentDescription = "Screenshot Preview",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("স্ক্রিনশট যুক্ত হয়েছে", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("প্রমাণ সফলভাবে সিলেক্টেড", color = ChaiTextSecondary, fontSize = 10.sp)
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = Color(0xFF8247E5),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("পেমেন্ট স্ক্রিনশট (প্রমাণ)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Binance/Bybit পেমেন্ট প্রুফ ছবি দিন", color = ChaiTextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (screenshotUri != null) Color(0xFF262638) else Color(0xFF8247E5)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("pick_crypto_screenshot_button")
                                ) {
                                    Icon(
                                        imageVector = if (screenshotUri != null) Icons.Default.Check else Icons.Default.Upload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (screenshotUri != null) "Change" else "Attach",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Checklist Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF14141C))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Premium যা যা পাচ্ছেন:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        listOf(
                            "Free messages left: Unlimited (আনলিমিটেড বার্তা)",
                            "5x দ্রুততর ও বুদ্ধিমান AI কথোপকথন",
                            "কোনো Ads নেই & আনলিমিটেড কাস্টম বট তৈরি",
                            "ভিআইপি প্রায়োরিটি মেম্বারশিপ অ্যাক্সেস"
                        ).forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = feature,
                                    color = Color(0xFFD0D0DC),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 4. SUBMIT PAYMENT BUTTON
                // ==========================================
                Button(
                    onClick = {
                        if (!isCrypto) {
                            // bKash / Nagad validation
                            if (senderNumber.isBlank() || trxId.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "দয়া করে আপনার $selectedMethod নম্বর এবং TrxID প্রদান করুন।",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                        } else {
                            // Crypto validation
                            if (trxId.isBlank() && screenshotUri == null) {
                                Toast.makeText(
                                    context,
                                    "দয়া করে আপনার Crypto TxID অথবা স্ক্রিনশট সিলেক্ট করুন।",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                        }

                        val finalPlan = if (isCrypto) {
                            if (isWeekly) "Weekly ($paymentSettings.cryptoWeeklyPrice)" else "Yearly ($paymentSettings.cryptoYearlyPrice)"
                        } else {
                            if (isWeekly) "Weekly (৳${paymentSettings.bdtWeeklyPrice})" else "Yearly (৳${paymentSettings.bdtYearlyPrice})"
                        }

                        val senderRef = if (isCrypto) {
                            if (senderNumber.isNotBlank()) senderNumber.trim() else "Crypto (Polygon USDT)"
                        } else {
                            senderNumber.trim()
                        }

                        val finalTxId = if (trxId.isNotBlank()) trxId.trim() else "CRYPTO-PROOF-${System.currentTimeMillis().toString().takeLast(6)}"
                        val finalAmount = currentPriceLabel
                        val proofNote = if (screenshotUri != null) "Screenshot Attached: ${screenshotUri?.lastPathSegment}" else additionalNote.trim()

                        onBuyPremium(finalPlan, selectedMethod, senderRef, finalTxId, finalAmount, proofNote)

                        Toast.makeText(
                            context,
                            "✅ পেমেন্ট রিকোয়েস্ট সাবমিট হয়েছে!\nঅ্যাডমিন ভেরিফাই করে অ্যাপ্রুভ করলেই আপনার অ্যাকাউন্টে Unlimited মেসেজ সক্রিয় হবে।",
                            Toast.LENGTH_LONG
                        ).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("activate_premium_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCrypto) Color(0xFF8247E5) else ChaiRed
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "পেমেন্ট তথ্য সাবমিট করুন ($currentPriceLabel)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "পেমেন্ট তথ্য সাবমিট করার পর অ্যাডমিন প্যানেলে রিকোয়েস্ট চলে যাবে। অনুমোদন পাওয়ার সাথে সাথে আনলিমিটেড মেসেজ চালু হবে।",
                    color = ChaiTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
