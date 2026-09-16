package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ChaiLogo
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun SubscriptionDialog(
    onDismiss: () -> Unit,
    onBuyPremium: (plan: String, paymentMethod: String, trxId: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("Weekly") } // "Weekly" or "Yearly"
    var selectedMethod by remember { mutableStateOf("bKash") } // "bKash" or "Nagad"
    var senderNumber by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }

    val paymentNumber = "01750721835"

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

                Spacer(modifier = Modifier.height(16.dp))

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
                        text = "Premium কিনুন (Chai Ultra)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "আনলিমিটেড মেসেজ ও সুপারফাস্ট AI রেসপন্স",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "যেকোনো প্ল্যান বেছে নিন এবং bKash/Nagad দিয়ে পেমেন্ট করুন",
                    color = ChaiTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Plan Cards: Weekly 230 taka & Yearly 2000 taka
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Weekly Plan (230 taka)
                    val isWeekly = selectedPlan == "Weekly"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isWeekly) Color(0xFF221518) else Color(0xFF1B1B22))
                            .border(
                                width = if (isWeekly) 2.dp else 1.dp,
                                color = if (isWeekly) ChaiRed else ChaiBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedPlan = "Weekly" }
                            .padding(14.dp)
                            .testTag("plan_weekly_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Weekly Plan", color = ChaiTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "230 Taka",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "৭ দিন আনলিমিটেড",
                                color = if (isWeekly) ChaiRed else ChaiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Yearly Plan (2000 taka)
                    val isYearly = selectedPlan == "Yearly"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isYearly) Color(0xFF221518) else Color(0xFF1B1B22))
                            .border(
                                width = if (isYearly) 2.dp else 1.dp,
                                color = if (isYearly) ChaiRed else ChaiBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedPlan = "Yearly" }
                            .testTag("plan_yearly_button")
                    ) {
                        // Badge Save
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 8.dp))
                                .background(Color(0xFFFFB300))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Save 83%",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text("Yearly Plan", color = ChaiTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "2000 Taka",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "৩৬৫ দিন আনলিমিটেড",
                                color = if (isYearly) ChaiRed else ChaiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Payment Instruction Card (bKash / Nagad Send Money)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1B1B22))
                        .border(1.dp, Color(0x33E50914), RoundedCornerShape(16.dp))
                        .padding(16.dp)
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
                                tint = Color(0xFFE50914),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment bKash / Nagad এ Send Money করুন",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Number Box with Copy Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF121217))
                                .border(1.dp, ChaiBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Send Money Number (Personal):",
                                    color = ChaiTextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = paymentNumber,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Payment Number", paymentNumber)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "$paymentNumber কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E38)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_number_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("কপি", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Payment Method Selector: bKash vs Nagad
                        Text(
                            text = "পেমেন্ট মেথড নির্বাচন করুন:",
                            color = ChaiTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // bKash Button
                            val isBkash = selectedMethod == "bKash"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isBkash) Color(0xFFD12053) else Color(0xFF262630))
                                    .clickable { selectedMethod = "bKash" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "bKash বিকাশ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Nagad Button
                            val isNagad = selectedMethod == "Nagad"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isNagad) Color(0xFFF7941D) else Color(0xFF262630))
                                    .clickable { selectedMethod = "Nagad" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nagad নগদ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Input Sender Number
                        OutlinedTextField(
                            value = senderNumber,
                            onValueChange = { senderNumber = it },
                            label = { Text("আপনার $selectedMethod নম্বর", color = ChaiTextSecondary, fontSize = 12.sp) },
                            placeholder = { Text("01XXXXXXXXX", color = ChaiTextSecondary, fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sender_number_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ChaiRed,
                                unfocusedBorderColor = ChaiBorder,
                                focusedContainerColor = Color(0xFF121217),
                                unfocusedContainerColor = Color(0xFF121217)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Input Transaction ID (TrxID)
                        OutlinedTextField(
                            value = trxId,
                            onValueChange = { trxId = it },
                            label = { Text("Transaction ID (TrxID) / রেফারেন্স", color = ChaiTextSecondary, fontSize = 12.sp) },
                            placeholder = { Text("e.g. BL92A8XYZ", color = ChaiTextSecondary, fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trx_id_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ChaiRed,
                                unfocusedBorderColor = ChaiBorder,
                                focusedContainerColor = Color(0xFF121217),
                                unfocusedContainerColor = Color(0xFF121217)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Feature Checklist Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF181820))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Premium সুবিধাসমূহ:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        listOf(
                            "Free messages left: Unlimited (আনলিমিটেড মেসেজ)",
                            "5x দ্রুততর ও বুদ্ধিমান AI কথোপকথন",
                            "কোনো Ads বা লিমিট নেই",
                            "আনলিমিটেড কাস্টম AI বট তৈরি"
                        ).forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feature,
                                    color = Color(0xFFE0E0E0),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Submit / Activate Premium Button
                Button(
                    onClick = {
                        val finalPlan = if (selectedPlan == "Weekly") "Weekly (230 Taka)" else "Yearly (2000 Taka)"
                        onBuyPremium(finalPlan, selectedMethod, trxId)
                        Toast.makeText(
                            context,
                            "🎉 অভিনন্দন! আপনার $finalPlan Premium অ্যাক্টিভ হয়েছে। Free messages: Unlimited!",
                            Toast.LENGTH_LONG
                        ).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("activate_premium_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed)
                ) {
                    Text(
                        text = "পেমেন্ট নিশ্চিত করুন & Premium চালু করুন",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "টাকা সেন্ড করার পর পেমেন্ট নিশ্চিত করুন বাটনে চাপ দিন। সাথে সাথে Unlimited মেসেজ চালু হবে।",
                    color = ChaiTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
