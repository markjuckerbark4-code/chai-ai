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
        // Title
        Text(
            text = "ACCOUNT & PROFILE",
            color = ChaiTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Large Avatar with user initial and camera badge
        Box(
            modifier = Modifier
                .size(130.dp)
                .padding(bottom = 8.dp),
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
                    text = userAccount.avatarInitial.ifBlank { "M" },
                    color = Color.White,
                    fontSize = 54.sp,
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

        Spacer(modifier = Modifier.height(10.dp))

        // Account Name (editable)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = userAccount.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
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
                    tint = ChaiTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userAccount.email,
            color = ChaiTextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Free messages left Card (User Request: "tar nice free massage left eta zero thakbi jodi kew taka deai ar ami admin panal e aprove di taile ekhane unlimited show korbi")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (userAccount.isPremium) {
                        Brush.horizontalGradient(listOf(Color(0xFF281E0C), Color(0xFF1B1408)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF201318), Color(0xFF14141E)))
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (userAccount.isPremium) Color(0xFFFFB300) else Color(0x55E50914),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .testTag("free_messages_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (userAccount.isPremium) Color(0xFFFFB300) else Color(0xFF33161C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (userAccount.isPremium) Icons.Default.WorkspacePremium else Icons.Outlined.Chat,
                        contentDescription = null,
                        tint = if (userAccount.isPremium) Color.Black else Color(0xFFFF4D4D),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free messages left",
                        color = ChaiTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (userAccount.isPremium) "Unlimited 👑" else "0",
                        color = if (userAccount.isPremium) Color(0xFFFFD54F) else Color(0xFFFF5252),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (userAccount.isPremium) {
                            "Chai Ultra Premium Active • অনুমোদিত"
                        } else {
                            "সীমা শেষ • আনলিমিটেড করতে রিচার্জ করুন"
                        },
                        color = if (userAccount.isPremium) Color(0xFF81C784) else Color(0xFFB0B0C0),
                        fontSize = 11.sp,
                        fontWeight = if (userAccount.isPremium) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                if (!userAccount.isPremium) {
                    Button(
                        onClick = onOpenUpgrade,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChaiRed),
                        modifier = Modifier.testTag("recharge_premium_button")
                    ) {
                        Text(
                            text = "Premium কিনুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Contact Email Card (User Request: "tar por nice contact email chai@recharge.com")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14141E))
                .border(1.dp, Color(0xFF262638), RoundedCornerShape(16.dp))
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Chai Support Email", "chai@recharge.com"))
                    Toast.makeText(context, "ইমেইল কপি করা হয়েছে: chai@recharge.com", Toast.LENGTH_SHORT).show()
                }
                .padding(16.dp)
                .testTag("contact_email_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F2233)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Support",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contact & Recharge Support",
                        color = ChaiTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "chai@recharge.com",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A28)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Email",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Media Logos (User Request: "ar nice facbook instagram tiktok er logo")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14141E))
                .border(1.dp, Color(0xFF262638), RoundedCornerShape(16.dp))
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
                // Facebook Logo & Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Chai AI Facebook কমিউনিটি খুলছে...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                        .testTag("facebook_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1877F2))
                            .border(1.5.dp, Color(0xFF4285F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "f",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Facebook",
                        color = Color(0xFFB0C4DE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Instagram Logo & Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Chai AI Instagram পেজ খুলছে...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                        .testTag("instagram_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF833AB4),
                                        Color(0xFFFD1D1D),
                                        Color(0xFFF77737)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFFF8A80), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Instagram",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instagram",
                        color = Color(0xFFFFB2B2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // TikTok Logo & Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Chai AI TikTok চ্যানেল খুলছে...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                        .testTag("tiktok_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF010101))
                            .border(1.5.dp, Color(0xFF00F2FE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "TikTok",
                                tint = Color(0xFF00F2FE),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TikTok",
                        color = Color(0xFF80DEEA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Account Details Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ChaiCardBackground)
                .border(1.dp, ChaiBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
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
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACCOUNT DETAILS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Active Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x224CAF50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Active",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Account ID Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("User ID", color = ChaiTextSecondary, fontSize = 13.sp)
                    Text(userAccount.userId, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Email Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Email", color = ChaiTextSecondary, fontSize = 13.sp)
                    Text(userAccount.email, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Membership Tier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Member Tier", color = ChaiTextSecondary, fontSize = 13.sp)
                    Text(
                        text = if (userAccount.isPremium) "Chai Ultra Premium" else userAccount.memberTier,
                        color = if (userAccount.isPremium) Color(0xFFFFD54F) else Color(0xFFFFB74D),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Member Since
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Member Since", color = ChaiTextSecondary, fontSize = 13.sp)
                    Text(userAccount.joinDate, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Upgrade / Premium Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (userAccount.isPremium) {
                        Brush.horizontalGradient(listOf(Color(0xFF2E2412), Color(0xFF1E180E)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF3B1218), Color(0xFF1E1418)))
                    }
                )
                .border(
                    1.dp,
                    if (userAccount.isPremium) Color(0xFFFFB300) else ChaiRed.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                )
                .clickable { onOpenUpgrade() }
                .padding(16.dp)
                .testTag("upgrade_banner_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (userAccount.isPremium) Color(0xFFFFB300) else ChaiRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (userAccount.isPremium) "Chai Ultra Premium 👑" else "Upgrade to Premium ✨",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (userAccount.isPremium) "Unlimited messages & Fast memory enabled" else "Weekly ৳230 | Yearly ৳2000 (bKash/Nagad)",
                        color = if (userAccount.isPremium) Color(0xFFFFD54F) else ChaiTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = onOpenUpgrade,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userAccount.isPremium) Color(0xFFFFB300) else ChaiRed
                    )
                ) {
                    Text(
                        text = if (userAccount.isPremium) "Ultra" else "Upgrade",
                        color = if (userAccount.isPremium) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sign Out / Switch Account Button
        Button(
            onClick = { showLogoutConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
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

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(8.dp))

        // Name Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Display Name", color = ChaiTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(userAccount.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = { showEditNameDialog = true },
                modifier = Modifier.testTag("edit_name_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.6f))

        // Personas Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPersonasDialog = true }
                .padding(vertical = 16.dp)
                .testTag("personas_menu_item"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Style,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = "Personas",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.6f))

        // Free messages left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Chat,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Free messages left", color = ChaiTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (userAccount.isPremium) "Unlimited" else freeMessages,
                    color = if (userAccount.isPremium) Color(0xFFFFD54F) else Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onOpenUpgrade,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userAccount.isPremium) Color(0xFF2C2210) else ChaiRed
                ),
                border = if (userAccount.isPremium) BorderStroke(1.dp, Color(0xFFFFB300)) else null,
                modifier = Modifier
                    .height(38.dp)
                    .testTag("upgrade_button")
            ) {
                Text(
                    text = if (userAccount.isPremium) "Unlimited" else "Upgrade",
                    color = if (userAccount.isPremium) Color(0xFFFFB300) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Restore Purchases button
        Button(
            onClick = {
                Toast.makeText(context, "Purchases restored successfully", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222228)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("restore_purchases_button")
        ) {
            Text(
                text = "Restore Purchases",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.6f))

        // My Pieces
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ChaiRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Token,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("My Pieces", color = ChaiTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(piecesCount.toString(), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showChargeDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChaiRed),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("charge_pieces_button")
            ) {
                Text("Charge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.6f))

        // Contact
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Contact", color = ChaiTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("hello@chai-research.com", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Got a question or complaint? Send us an email...",
                    color = ChaiTextSecondary,
                    fontSize = 12.sp
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
