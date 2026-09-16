package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChaiLogo
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun WelcomeScreen(
    onSignIn: (name: String, email: String, provider: String) -> Unit
) {
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showCustomAccountDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("welcome_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Chai Header
            ChaiLogo(
                iconWidth = 42.dp,
                iconHeight = 22.dp,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tagline: CHAT + AI
            Text(
                text = "CHAT + AI",
                color = ChaiTextPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Abstract 3D shape / Card banner: "EXPLORE AND CREATE"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFE6E6EB),
                                Color(0xFFC7C7D0),
                                Color(0xFF9898A6)
                            )
                        )
                    )
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(32.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Stylized curve inside card
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(topStart = 80.dp, bottomEnd = 80.dp))
                        .background(Color(0x22000000))
                )

                Text(
                    text = "EXPLORE AND\nCREATE",
                    color = Color(0xFF6E6E7A),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    lineHeight = 30.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons: Sign in with Google
            Button(
                onClick = { showGoogleAccountPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signin_google_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google icon G circle representation
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Buttons: Sign in with Facebook
            Button(
                onClick = {
                    onSignIn("Mark Juckerbark", "mark.juckerbark@facebook.com", "Facebook")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("signin_facebook_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1877F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "f",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Facebook",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Continue / Enter as Guest
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onSignIn("Guest User", "guest@chai.ai", "Guest")
                    }
                    .padding(vertical = 12.dp)
                    .testTag("continue_guest_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue as Guest",
                    color = ChaiTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = ChaiTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Google Account Chooser Dialog
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            modifier = Modifier.testTag("google_account_picker_dialog"),
            containerColor = Color(0xFF1F1F26),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Choose a Google Account",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "to continue to Chai AI",
                        color = ChaiTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Account 1: Mark Juckerbark
                    AccountSelectItem(
                        name = "Mark Juckerbark",
                        email = "markjuckerbark4@gmail.com",
                        avatarColor = Color(0xFFE91E63),
                        initial = "M",
                        onClick = {
                            showGoogleAccountPicker = false
                            onSignIn("Mark Juckerbark", "markjuckerbark4@gmail.com", "Google")
                        }
                    )

                    HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    // Account 2: Arafat Md
                    AccountSelectItem(
                        name = "Arafat Md",
                        email = "arafat.chai@gmail.com",
                        avatarColor = Color(0xFF673AB7),
                        initial = "A",
                        onClick = {
                            showGoogleAccountPicker = false
                            onSignIn("Arafat Md", "arafat.chai@gmail.com", "Google")
                        }
                    )

                    HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    // Use another account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showGoogleAccountPicker = false
                                showCustomAccountDialog = true
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2C34)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Use another account",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Cancel", color = ChaiTextSecondary)
                }
            }
        )
    }

    // Custom Account input dialog
    if (showCustomAccountDialog) {
        var customName by remember { mutableStateOf("") }
        var customEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCustomAccountDialog = false },
            containerColor = Color(0xFF1F1F26),
            title = {
                Text("Sign in with Google", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Your Name", color = ChaiTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ChaiRed,
                            unfocusedBorderColor = ChaiBorder
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Google Email", color = ChaiTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ChaiRed,
                            unfocusedBorderColor = ChaiBorder
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = customName.trim().ifBlank { "User" }
                        val finalEmail = customEmail.trim().ifBlank { "user@gmail.com" }
                        showCustomAccountDialog = false
                        onSignIn(finalName, finalEmail, "Google")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed)
                ) {
                    Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAccountDialog = false }) {
                    Text("Cancel", color = ChaiTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AccountSelectItem(
    name: String,
    email: String,
    avatarColor: Color,
    initial: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = email,
                color = ChaiTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
