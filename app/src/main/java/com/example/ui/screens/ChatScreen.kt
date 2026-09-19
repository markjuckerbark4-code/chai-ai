package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PersonaEntity
import com.example.ui.StreamingReply
import com.example.ui.components.CharacterImage
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun ChatScreen(
    bot: BotEntity,
    messages: List<MessageEntity>,
    activePersona: PersonaEntity?,
    isGenerating: Boolean,
    streamingReply: StreamingReply? = null,
    isPremium: Boolean = false,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onReroll: () -> Unit,
    onClearChat: () -> Unit,
    onOpenUpgrade: () -> Unit = {},
    onEditMessage: (Long, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    // Dialog state for editing a bot message
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editedText by remember { mutableStateOf("") }

    // Auto-scroll when new messages arrive or during streaming
    LaunchedEffect(messages.size, isGenerating, streamingReply?.text?.length) {
        val totalCount = messages.size + (if (streamingReply != null || isGenerating) 2 else 1)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount)
        }
    }

    val suggestions = remember(bot) {
        when {
            bot.name.contains("CEO", ignoreCase = true) -> listOf(
                "Hi",
                "What's up",
                "I miss you...",
                "Are you still in Tokyo?"
            )
            bot.name.contains("Scarlett", ignoreCase = true) -> listOf(
                "Hi",
                "What's up",
                "*Smiles* Tell me about your art",
                "Care to get another drink?"
            )
            bot.name.contains("Yukina", ignoreCase = true) -> listOf(
                "Hi",
                "Play that new melody for me.",
                "*Sits down beside you* What are you thinking about?",
                "Do you ever get lonely staying up so late?"
            )
            bot.name.contains("Kai", ignoreCase = true) -> listOf(
                "Hi",
                "What's up",
                "I've got your back. What's the plan?",
                "*Ducks low* Who sent the drones?"
            )
            else -> listOf(
                "Hi",
                "What's up",
                "*Looks at you* Tell me more.",
                "What should we do next?"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBlack)
            .imePadding()
            .testTag("chat_screen")
    ) {
        // --- Chai Official Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14141A))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button (<)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF20202A))
                    .clickable { onBack() }
                    .testTag("chat_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Title: CHAI with "plus v" pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CHAI",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Plus badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF22222D))
                        .border(1.dp, Color(0xFF343444), RoundedCornerShape(12.dp))
                        .clickable { onOpenUpgrade() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "plus",
                            color = Color(0xFFECECF2),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Plus dropdown",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Hamburger menu button (=)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF20202A))
                    .clickable { showMenu = true }
                    .testTag("chat_options_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(ChaiCardBackground)
                ) {
                    DropdownMenuItem(
                        text = { Text("Restart Conversation", color = ChaiRed) },
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = ChaiRed)
                        },
                        onClick = {
                            onClearChat()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Get Chai Ultra", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ChaiRed)
                        },
                        onClick = {
                            showMenu = false
                            onOpenUpgrade()
                        }
                    )
                }
            }
        }

        // Cooldown & Server Policy Notice Banner (User Request: "1 ghonta ba 2 ghonta calanor por 30 minit cool down")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A130D))
                .border(0.5.dp, Color(0xFF4E342E))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "নোটিশ: ১-২ ঘণ্টা একটানা চ্যাটের পর ৩০ মিনিট কুল ডাউন (Cool Down) থাকবে।",
                color = Color(0xFFFFE0B2),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        // Active Persona notice if set
        if (activePersona != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF13131A))
                    .padding(horizontal = 16.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Roleplaying as ${activePersona.displayName}",
                    color = Color(0xFF888898),
                    fontSize = 11.sp
                )
            }
        }

        // --- Chat Messages List ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Pill: bot avatar + name (scenario)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1F1F28))
                            .border(1.dp, Color(0xFF2E2E3C), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                        ) {
                            CharacterImage(
                                drawableName = bot.avatarDrawableName,
                                characterName = bot.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${bot.name} ${bot.tagline}",
                            color = Color(0xFFD6D6E0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            // Message list items
            items(messages, key = { it.id }) { msg ->
                val isBot = msg.sender == "bot"
                val isLatestBot = isBot && msg.id == messages.lastOrNull { it.sender == "bot" }?.id

                ChaiMessageRow(
                    message = msg,
                    bot = bot,
                    isLatestBot = isLatestBot && streamingReply == null && !isGenerating,
                    onReroll = onReroll,
                    onEdit = {
                        editingMessage = msg
                        editedText = msg.text
                    },
                    onVoicePlay = {
                        Toast.makeText(context, "Playing audio narration...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Actively streaming live bot reply ("colo to" live typewriter)
            if (streamingReply != null && streamingReply.botId == bot.id) {
                item {
                    StreamingBotBubble(
                        bot = bot,
                        currentText = streamingReply.text
                    )
                }
            } else if (isGenerating) {
                // Pulse dots indicator during initial generation before streaming starts
                item {
                    TypingIndicator(bot = bot)
                }
            }
        }

        // Suggestions Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    onClick = {
                        if (isPremium) {
                            onSendMessage(suggestion)
                        } else {
                            onOpenUpgrade()
                        }
                    },
                    label = {
                        Text(
                            text = suggestion,
                            color = ChaiTextPrimary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = ChaiCardBackground
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = ChaiBorder
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // --- Bottom Input Area: Locked for Free Users, Active for Premium Users ---
        if (!isPremium) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF14141E))
                    .border(1.dp, Color(0x44E50914), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable { onOpenUpgrade() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("chat_locked_premium_bar"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B161B))
                            .border(1.5.dp, Color(0xFFE50914), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFE50914),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "মেসেজিং লক করা (Premium Only)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বটের সাথে চ্যাট করতে প্রিমিয়াম আনলক করুন",
                            color = ChaiTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenUpgrade,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChaiRed),
                    modifier = Modifier.testTag("chat_unlock_premium_button")
                ) {
                    Text("Unlock", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF13131A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar circle on the left
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262632))
                        .border(1.dp, Color(0xFF383848), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile",
                        tint = Color(0xFFC0C0D0),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Pill text field "Type a message"
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Type a message", color = Color(0xFF7A7A8A), fontSize = 14.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1C1C24),
                        unfocusedContainerColor = Color(0xFF1C1C24),
                        focusedBorderColor = Color(0xFF3A3A4C),
                        unfocusedBorderColor = Color(0xFF282834),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = ChaiRed
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Action button on right: refresh when empty, send when text typed
                if (inputText.isBlank()) {
                    // Circular refresh/reroll button (as seen in screenshot)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF252532))
                            .clickable { onReroll() }
                            .testTag("chat_reroll_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reroll",
                            tint = Color(0xFFD4D4E0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Send button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ChaiRed)
                            .clickable {
                                if (!isGenerating && streamingReply == null) {
                                    onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            }
                            .testTag("chat_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog for editing bot message
    if (editingMessage != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            containerColor = ChaiCardBackground,
            title = { Text("Edit Message", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141419),
                        unfocusedContainerColor = Color(0xFF141419),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChaiRed
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val msg = editingMessage
                        if (msg != null && editedText.isNotBlank()) {
                            onEditMessage(msg.id, editedText)
                        }
                        editingMessage = null
                    }
                ) {
                    Text("Save", color = ChaiRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Cancel", color = ChaiTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ChaiMessageRow(
    message: MessageEntity,
    bot: BotEntity,
    isLatestBot: Boolean,
    onReroll: () -> Unit,
    onEdit: () -> Unit,
    onVoicePlay: () -> Unit
) {
    val isBot = message.sender == "bot"
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        if (isBot) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            ) {
                CharacterImage(
                    drawableName = bot.avatarDrawableName,
                    characterName = bot.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            horizontalAlignment = if (isBot) Alignment.Start else Alignment.End,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (isBot) {
                // Bot Text: Clean roleplay text without heavy bubble boundary
                Text(
                    text = formatRoleplayText(message.text),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )

                // Icons under bot message
                if (!isLatestBot) {
                    // Small photo and video icons on right (as in Chai UI)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { Toast.makeText(context, "Opening photo memory...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photo",
                                tint = Color(0xFF6E6E7E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { Toast.makeText(context, "Video memory available with Chai Ultra", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video",
                                tint = Color(0xFF6E6E7E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // --- Chai Full Action Bar under latest bot message ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        // 5-button action bar: Edit, Wand, Reroll, Image, Video
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChaiActionButton(
                                icon = Icons.Default.Edit,
                                description = "Edit",
                                onClick = onEdit
                            )
                            ChaiActionButton(
                                icon = Icons.Default.AutoAwesome,
                                description = "Wand",
                                onClick = {
                                    Toast.makeText(context, "Enhancing response with Chai AI...", Toast.LENGTH_SHORT).show()
                                    onReroll()
                                }
                            )
                            ChaiActionButton(
                                icon = Icons.Default.Refresh,
                                description = "Reroll",
                                onClick = onReroll
                            )
                            ChaiActionButton(
                                icon = Icons.Default.Image,
                                description = "Gallery",
                                onClick = {
                                    Toast.makeText(context, "Character photo generation", Toast.LENGTH_SHORT).show()
                                }
                            )
                            ChaiActionButton(
                                icon = Icons.Default.Videocam,
                                description = "Video",
                                onClick = {
                                    Toast.makeText(context, "Video memory feature", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Audio speaker circular button aligned to right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22222E))
                                    .border(1.dp, Color(0xFF323242), CircleShape)
                                    .clickable { onVoicePlay() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Voice narration",
                                    tint = Color(0xFFB4B4C8),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // User Message: rounded dark gray bubble on right
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 4.dp
                            )
                        )
                        .background(Color(0xFF33333E))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChaiActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E28))
            .border(1.dp, Color(0xFF2E2E3E), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color(0xFFC4C4D4),
            modifier = Modifier.size(17.dp)
        )
    }
}

/**
 * Live typewriter streaming bot bubble ("colo to")
 */
@Composable
fun StreamingBotBubble(
    bot: BotEntity,
    currentText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
        ) {
            CharacterImage(
                drawableName = bot.avatarDrawableName,
                characterName = bot.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f, fill = false)) {
            // Live typing text with a flashing subtle red cursor
            val formatted = formatRoleplayText(currentText)
            Text(
                text = buildAnnotatedString {
                    append(formatted)
                    withStyle(SpanStyle(color = ChaiRed, fontWeight = FontWeight.Bold)) {
                        append(" ▍")
                    }
                },
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
fun TypingIndicator(bot: BotEntity) {
    val transition = rememberInfiniteTransition(label = "dots")
    val dot1Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
        ) {
            CharacterImage(
                drawableName = bot.avatarDrawableName,
                characterName = bot.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E26))
                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0EC).copy(alpha = dot1Alpha))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0EC).copy(alpha = dot2Alpha))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0EC).copy(alpha = dot3Alpha))
                )
            }
        }
    }
}

fun formatRoleplayText(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("*")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                // Inside asterisks: action/expression (italic and softer grey/purple)
                withStyle(
                    style = SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFC0C0D0)
                    )
                ) {
                    append("*${parts[i]}*")
                }
            } else {
                // Spoken dialogue or normal text (clean bright white)
                withStyle(
                    style = SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    append(parts[i])
                }
            }
        }
    }
}
