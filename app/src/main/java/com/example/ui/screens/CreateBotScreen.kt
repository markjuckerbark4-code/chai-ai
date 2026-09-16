package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.CharacterImage
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun CreateBotScreen(
    currentUserName: String,
    onCreateBot: (
        name: String,
        creatorName: String,
        tagline: String,
        description: String,
        firstMessage: String,
        personalityPrompt: String,
        category: String,
        avatarName: String
    ) -> Unit
) {
    val context = LocalContext.current

    // Steps: 1 = Identity, 2 = Prompt & Memory, 3 = Intro Details & Publish
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1: Identity fields
    var name by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    val avatarPresets = listOf(
        "img_bot_scarlett" to "Scarlett (Anime)",
        "img_bot_yukina" to "Yukina (Indie)",
        "img_bot_kai" to "Kai (Sci-Fi)"
    )
    var selectedAvatar by remember { mutableStateOf(avatarPresets[0].first) }
    var customPhotoUploaded by remember { mutableStateOf(false) }

    // Step 2: Dialogue & Memory fields
    var firstMessage by remember { mutableStateOf("") }
    var memoryPrompt by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Romance") }
    val categories = listOf("Romance", "Anime", "Fantasy", "Sci-Fi", "Mystery", "Comedy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBlack)
            .testTag("create_bot_screen")
    ) {
        // Top Header with Step indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("create_bot_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentStep) {
                            1 -> "Identity"
                            2 -> "First Message & Memory"
                            else -> "Intro Details"
                        },
                        color = ChaiTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step $currentStep of 3",
                        color = ChaiTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Step Progress Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i <= currentStep) ChaiRed else Color(0xFF2C2C34))
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))

        // Screen Body with animated transitions between steps
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "step_transition",
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                // ==================== STEP 1: IDENTITY ====================
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "Character Visual & Identity",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upload a picture or select an avatar for your new bot.",
                            color = ChaiTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                        )

                        // Upload Picture preview & button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ChaiCardBackground)
                                .border(1.dp, ChaiBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, ChaiRed, CircleShape)
                                ) {
                                    CharacterImage(
                                        drawableName = selectedAvatar,
                                        characterName = name.ifBlank { "Bot" },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // Upload Pic Button
                                    Button(
                                        onClick = {
                                            customPhotoUploaded = true
                                            Toast.makeText(context, "Picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C38)),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.testTag("upload_pic_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (customPhotoUploaded) "Photo Selected ✓" else "Upload Pic",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Avatar Presets Row
                        Text("Or choose preset visual:", color = ChaiTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            avatarPresets.forEach { (drawable, label) ->
                                val isSelected = selectedAvatar == drawable
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF2C1920) else ChaiCardBackground)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) ChaiRed else ChaiBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedAvatar = drawable }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                        ) {
                                            CharacterImage(
                                                drawableName = drawable,
                                                characterName = label,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label.substringBefore(" "),
                                            color = if (isSelected) Color.White else ChaiTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Name field ("tar por name")
                        Text("Name *", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. Kavita, Scarlett, Ethan", color = ChaiTextSecondary, fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bot_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ChaiCardBackground,
                                unfocusedContainerColor = ChaiCardBackground,
                                focusedBorderColor = ChaiRed,
                                unfocusedBorderColor = ChaiBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tagline / Scenario hook
                        Text("Tagline / Role", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = tagline,
                            onValueChange = { tagline = it },
                            placeholder = { Text("e.g. (Childhood friend), (Cold Mafia Boss)", color = ChaiTextSecondary, fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bot_tagline_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ChaiCardBackground,
                                unfocusedContainerColor = ChaiCardBackground,
                                focusedBorderColor = ChaiRed,
                                unfocusedBorderColor = ChaiBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Visibility field ("tar por visiblity")
                        Text("Visibility", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Public Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isPublic) Color(0xFF2C1920) else ChaiCardBackground)
                                    .border(
                                        width = if (isPublic) 2.dp else 1.dp,
                                        color = if (isPublic) ChaiRed else ChaiBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { isPublic = true }
                                    .padding(12.dp)
                                    .testTag("visibility_public_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (isPublic) ChaiRed else ChaiTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Public",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Anyone can chat",
                                            color = ChaiTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // Private Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isPublic) Color(0xFF2C1920) else ChaiCardBackground)
                                    .border(
                                        width = if (!isPublic) 2.dp else 1.dp,
                                        color = if (!isPublic) ChaiRed else ChaiBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { isPublic = false }
                                    .padding(12.dp)
                                    .testTag("visibility_private_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (!isPublic) ChaiRed else ChaiTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Private",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Only you can chat",
                                            color = ChaiTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Continue Button ("er por nicea continue")
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    currentStep = 2
                                } else {
                                    Toast.makeText(context, "Please enter character name", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("continue_step1_button"),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChaiRed,
                                disabledContainerColor = Color(0xFF382228)
                            )
                        ) {
                            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                // ==================== STEP 2: PROMPT & MEMORY ====================
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "Dialogue & Memory",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Set up how $name will greet you and their core personality memory.",
                            color = ChaiTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                        )

                        // Category
                        Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                val selected = cat == selectedCategory
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, color = if (selected) Color.White else ChaiTextSecondary, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ChaiRed,
                                        containerColor = ChaiCardBackground
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = if (selected) ChaiRed else ChaiBorder
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // First message ("er por fast massage")
                        Text("First Message (Greeting) *", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("The very first message $name sends when you open chat:", color = ChaiTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = firstMessage,
                            onValueChange = { firstMessage = it },
                            placeholder = {
                                Text(
                                    text = "*smiles softly as you walk in* Hey! You're finally here. I've been waiting for you all morning...",
                                    color = ChaiTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bot_greeting_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ChaiCardBackground,
                                unfocusedContainerColor = ChaiCardBackground,
                                focusedBorderColor = ChaiRed,
                                unfocusedBorderColor = ChaiBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Memory ("tar por memory")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = ChaiRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Memory & Persona *", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Facts, personality rules, and lore that $name always remembers:",
                            color = ChaiTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = memoryPrompt,
                            onValueChange = { memoryPrompt = it },
                            placeholder = {
                                Text(
                                    text = "You are $name. You are caring, teasing, and playful. You love coffee and hate rain. Always use asterisks *like this* for physical actions and emotions.",
                                    color = ChaiTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bot_memory_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ChaiCardBackground,
                                unfocusedContainerColor = ChaiCardBackground,
                                focusedBorderColor = ChaiRed,
                                unfocusedBorderColor = ChaiBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Navigation Buttons: Back & Continue
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ChaiBorder)
                            ) {
                                Text("Back", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (firstMessage.isNotBlank()) {
                                        currentStep = 3
                                    } else {
                                        Toast.makeText(context, "Please enter the first message", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = firstMessage.isNotBlank(),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(52.dp)
                                    .testTag("continue_step2_button"),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChaiRed,
                                    disabledContainerColor = Color(0xFF382228)
                                )
                            ) {
                                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                // ==================== STEP 3: INTRO DETAILS & PUBLISH ====================
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "Intro Details",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Review your character card details before publishing to messages.",
                            color = ChaiTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                        )

                        // Intro Details Card (Full Preview)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(ChaiCardBackground)
                                .border(1.dp, ChaiBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                                .testTag("intro_details_preview_card")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Header: Avatar, Name, Visibility, Category
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, ChaiRed, CircleShape)
                                    ) {
                                        CharacterImage(
                                            drawableName = selectedAvatar,
                                            characterName = name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = tagline.ifBlank { "(Custom Companion)" },
                                            color = ChaiRed,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Created by @$currentUserName",
                                            color = ChaiTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Badges: Visibility + Category
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF262632))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isPublic) "Public" else "Private",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ChaiRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = selectedCategory,
                                            color = ChaiRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = ChaiBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // First Message Preview Box
                                Text(
                                    text = "FAST MESSAGE (FIRST GREETING)",
                                    color = ChaiTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1B1B22))
                                        .border(1.dp, ChaiBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "\"$firstMessage\"",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Memory Box
                                Text(
                                    text = "MEMORY & PERSONA RULES",
                                    color = ChaiTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1B1B22))
                                        .border(1.dp, ChaiBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = memoryPrompt.ifBlank { "Stay in character as $name. Use asterisks for actions." },
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Publish Now button ("nice publish now dile massage option e bot asbi")
                        Button(
                            onClick = {
                                onCreateBot(
                                    name,
                                    currentUserName,
                                    tagline.ifBlank { "(Custom Character)" },
                                    "A character created by $currentUserName on Chai AI.",
                                    firstMessage,
                                    memoryPrompt.ifBlank { "Stay in character as $name. Use asterisks for actions." },
                                    selectedCategory,
                                    selectedAvatar
                                )
                                Toast.makeText(context, "🎉 $name Published! Chat opened.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("publish_now_button"),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChaiRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Publish Now",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Edit Details Button
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChaiTextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChaiBorder)
                        ) {
                            Text("Edit Details", fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
