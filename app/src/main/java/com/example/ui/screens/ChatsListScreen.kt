package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.ui.components.CharacterImage
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun ChatsListScreen(
    bots: List<BotEntity>,
    allMessages: List<MessageEntity>,
    onOpenChat: (String) -> Unit
) {
    // Group messages by botId and get the latest message for each
    val recentChats = remember(bots, allMessages) {
        val messagesByBot = allMessages.groupBy { it.botId }
        bots.mapNotNull { bot ->
            val botMessages = messagesByBot[bot.id]
            if (!botMessages.isNullOrEmpty()) {
                val latest = botMessages.maxByOrNull { it.timestamp }
                bot to latest
            } else {
                null
            }
        }.sortedByDescending { it.second?.timestamp ?: 0L }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBlack)
            .padding(top = 16.dp)
            .testTag("chats_list_screen")
    ) {
        Text(
            text = "Chats",
            color = ChaiTextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        if (recentChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = ChaiTextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No active conversations yet",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore bots in the feed and start your first conversation!",
                        color = ChaiTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentChats, key = { it.first.id }) { (bot, latestMessage) ->
                    ChatItemRow(
                        bot = bot,
                        lastMessage = latestMessage?.text ?: bot.firstMessage,
                        sender = latestMessage?.sender ?: "bot",
                        onClick = { onOpenChat(bot.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(
    bot: BotEntity,
    lastMessage: String,
    sender: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ChaiCardBackground)
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("chat_item_${bot.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Character avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        ) {
            CharacterImage(
                drawableName = bot.avatarDrawableName,
                characterName = bot.name,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bot.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = bot.category,
                    color = ChaiRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val prefix = if (sender == "user") "You: " else ""
            Text(
                text = "$prefix$lastMessage",
                color = ChaiTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
