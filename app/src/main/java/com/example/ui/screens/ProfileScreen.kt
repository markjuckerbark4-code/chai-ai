package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.data.model.PersonaEntity
import com.example.ui.UserAccount
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiPurple
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun ProfileScreen(
    userAccount: UserAccount,
    freeMessages: String,
    piecesCount: Int,
    personas: List<PersonaEntity>,
    onUpdateName: (String) -> Unit,
    onOpenUpgrade: () -> Unit,
    onChargePieces: (Int) -> Unit,
    onAddPersona: (String, String, String) -> Unit,
    onSelectPersona: (Long) -> Unit,
    onDeletePersona: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(userAccount.name) }
    var showPersonasDialog by remember { mutableStateOf(false) }
    var showChargeDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = "PROFILE",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Large Avatar with user initial and camera badge
        Box(
            modifier = Modifier
                .size(125.dp)
                .padding(bottom = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .clip(CircleShape)
                    .background(ChaiPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userAccount.avatarInitial.ifBlank { userAccount.name.take(1).uppercase() }.ifBlank { "M" },
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Camera badge on bottom right
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C34))
                    .border(2.dp, ChaiBlack, CircleShape)
                    .clickable {
                        Toast.makeText(context, "Change avatar clicked", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==============================================================
        // MAIN ACCOUNT OPTIONS LIST (Matching the Chai screenshot layout)
        // ==============================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF15151C))
                .border(1.dp, Color(0xFF262632), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Name Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editingName = userAccount.name
                        showEditNameDialog = true
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Name",
                        color = ChaiTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = userAccount.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        editingName = userAccount.name
                        showEditNameDialog = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF232330))

            // 2. Personas Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPersonasDialog = true }
                    .padding(vertical = 14.dp)
                    .testTag("personas_menu_item"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Style,
                    contentDescription = "Personas",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Personas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Open Personas",
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(14.dp)
                )
            }

            HorizontalDivider(color = Color(0xFF232330))

            // 3. Free messages left Row (0 for free users, Unlimited for approved/premium users)
            val isUserPremium = userAccount.isPremium || freeMessages.equals("Unlimited", ignoreCase = true)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("free_messages_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = "Messages",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free messages left",
                        color = ChaiTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isUserPremium) "Unlimited" else "0",
                        color = if (isUserPremium) Color(0xFFFFD54F) else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onOpenUpgrade,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUserPremium) Color(0xFFFFB300) else ChaiRed
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("upgrade_button")
                ) {
                    Text(
                        text = if (isUserPremium) "Ultra" else "Upgrade",
                        color = if (isUserPremium) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Restore Purchases Button
            Button(
                onClick = {
                    if (userAccount.isPremium) {
                        Toast.makeText(context, "✅ Premium সক্রিয় আছে: ${userAccount.memberTier}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "ক্লাউড থেকে সাবস্ক্রিপশন চেক করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222D)),
                border = BorderStroke(1.dp, Color(0xFF333342)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("restore_purchases_button")
            ) {
                Text(
                    text = "Restore Purchases",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF232330))

            // 5. My Pieces Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(ChaiRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Token,
                        contentDescription = "Pieces",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Pieces",
                        color = ChaiTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = piecesCount.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { showChargeDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("charge_pieces_button")
                ) {
                    Text(
                        text = "Charge",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF232330))

            // 6. Contact Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:hello@chai-research.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Chai AI Query")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Chai Email", "hello@chai-research.com"))
                            Toast.makeText(context, "ইমেইল কপি হয়েছে: hello@chai-research.com", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Contact",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contact",
                        color = ChaiTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "hello@chai-research.com",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Got a question or complaint? Send us an email...",
                        color = ChaiTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Details & ID Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF15151C))
                .border(1.dp, Color(0xFF262632), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("account_details_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (userAccount.isPremium) Color(0xFFFFB300) else Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACCOUNT IDENTIFIER",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Active Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (userAccount.isPremium) Color(0x33FFB300) else Color(0x224CAF50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (userAccount.isPremium) Color(0xFFFFB300) else Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (userAccount.isPremium) "Ultra VIP" else "Active",
                            color = if (userAccount.isPremium) Color(0xFFFFD54F) else Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF232330))
                Spacer(modifier = Modifier.height(10.dp))

                // User ID Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Chai User ID", userAccount.userId))
                            Toast.makeText(context, "User ID কপি হয়েছে: ${userAccount.userId}", Toast.LENGTH_SHORT).show()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("User ID", color = ChaiTextSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userAccount.userId,
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy User ID",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Email Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Email", color = ChaiTextSecondary, fontSize = 12.sp)
                    Text(userAccount.email, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Member Tier Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Membership", color = ChaiTextSecondary, fontSize = 12.sp)
                    Text(
                        text = if (userAccount.isPremium) "Chai Ultra (Unlimited)" else "Standard Free (0 Messages)",
                        color = if (userAccount.isPremium) Color(0xFFFFD54F) else Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Server Cooldown Notice Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1D1612))
                .border(1.dp, Color(0xFF533829), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("cooldown_notice_card")
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF382318)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Server Notice",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "সার্ভার নোটিশ ও কুল ডাউন",
                        color = Color(0xFFFFB74D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "১ ঘণ্টা বা ২ ঘণ্টা একটানা ব্যবহারের পর ৩০ মিনিট কুল ডাউন (Cool Down) থাকবে। এটি এআই সার্ভার রিলোড, মেমরি রিফ্রেশ ও স্মুথ চ্যাটিংয়ের জন্য প্রযোজ্য।",
                        color = Color(0xFFFFE0B2),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Social Media Logos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF15151C))
                .border(1.dp, Color(0xFF262632), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("social_media_section"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FOLLOW US & CONNECT",
                color = ChaiTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Facebook
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Chai AI Facebook কমিউনিটি খুলছে...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1877F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Facebook", color = Color(0xFFB0C4DE), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Instagram
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Chai AI Instagram পেজ খুলছে...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Instagram",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Instagram", color = Color(0xFFFFB2B2), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // TikTok
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Chai AI TikTok চ্যানেল খুলছে...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF010101))
                            .border(1.dp, Color(0xFF00F2FE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "TikTok",
                            tint = Color(0xFF00F2FE),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("TikTok", color = Color(0xFF80DEEA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Out / Switch Account Button
        Button(
            onClick = { showLogoutConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("logout_account_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2A1B1E),
                contentColor = Color(0xFFFF5252)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FF5252))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFFF5252)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out / Switch Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF5252)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        var newNameInput by remember { mutableStateOf(userAccount.name) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = ChaiCardBackground,
            title = { Text("Edit Display Name", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChaiRed,
                        unfocusedBorderColor = ChaiBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNameInput.isNotBlank()) {
                            onUpdateName(newNameInput.trim())
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = ChaiTextSecondary)
                }
            }
        )
    }

    // Log Out Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = Color(0xFF1F1F26),
            title = {
                Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of ${userAccount.email}?\nYou can log back in at any time.",
                    color = ChaiTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = ChaiTextSecondary)
                }
            }
        )
    }

    // Personas Manager Dialog
    if (showPersonasDialog) {
        var showCreatePersona by remember { mutableStateOf(false) }
        var pName by remember { mutableStateOf("") }
        var pPronouns by remember { mutableStateOf("He/Him") }
        var pDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPersonasDialog = false },
            containerColor = ChaiCardBackground,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Personas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    IconButton(onClick = { showCreatePersona = !showCreatePersona }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Persona", tint = ChaiRed)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Bots will address you and tailor their roleplay based on your active persona.",
                        color = ChaiTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (showCreatePersona) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF262632), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("New Persona", color = ChaiRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = pName,
                                onValueChange = { pName = it },
                                placeholder = { Text("Persona Name (e.g. Alex)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = pPronouns,
                                onValueChange = { pPronouns = it },
                                placeholder = { Text("Pronouns (e.g. He/Him, She/Her)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = pDesc,
                                onValueChange = { pDesc = it },
                                placeholder = { Text("Backstory & Personality") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (pName.isNotBlank()) {
                                        onAddPersona(pName, pPronouns, pDesc)
                                        pName = ""
                                        pDesc = ""
                                        showCreatePersona = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ChaiRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Persona")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    personas.forEach { persona ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (persona.isSelected) Color(0xFF33202E) else Color(0xFF1E1E26))
                                .border(
                                    1.dp,
                                    if (persona.isSelected) ChaiRed else ChaiBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectPersona(persona.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(persona.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${persona.pronouns})", color = ChaiTextSecondary, fontSize = 12.sp)
                                }
                                if (persona.description.isNotBlank()) {
                                    Text(persona.description, color = ChaiTextSecondary, fontSize = 12.sp, maxLines = 2)
                                }
                            }
                            if (persona.isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = ChaiRed)
                            } else {
                                IconButton(onClick = { onDeletePersona(persona.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ChaiTextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPersonasDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // Charge Pieces Dialog
    if (showChargeDialog) {
        AlertDialog(
            onDismissRequest = { showChargeDialog = false },
            containerColor = ChaiCardBackground,
            title = { Text("Recharge Pieces", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pieces let you unlock exclusive character features, voice, and memories.", color = ChaiTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    listOf(
                        100 to "BDT 99.00",
                        500 to "BDT 399.00",
                        1200 to "BDT 799.00 (Best Value)"
                    ).forEach { (amount, price) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF262632))
                                .clickable {
                                    onChargePieces(amount)
                                    Toast.makeText(context, "Added $amount pieces!", Toast.LENGTH_SHORT).show()
                                    showChargeDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("+$amount Pieces", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(price, color = ChaiRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChargeDialog = false }) {
                    Text("Close", color = ChaiTextSecondary)
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = ChaiCardBackground,
            title = { Text("Edit Display Name", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    singleLine = true,
                    label = { Text("Your Name", color = ChaiTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChaiRed,
                        unfocusedBorderColor = ChaiBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingName.isNotBlank()) {
                            onUpdateName(editingName.trim())
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = ChaiTextSecondary)
                }
            }
        )
    }
}
