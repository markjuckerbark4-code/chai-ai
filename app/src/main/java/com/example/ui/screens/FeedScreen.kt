package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.ChaiLogo
import com.example.ui.components.CharacterImage
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiCardBackground
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextPrimary
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun FeedScreen(
    bots: List<BotEntity>,
    onBotClick: (String) -> Unit,
    onToggleLike: (BotEntity) -> Unit,
    onToggleFollow: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChaiBlack)
            .testTag("feed_screen")
    ) {
        // Top Bar: Chai Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            ChaiLogo(
                iconWidth = 36.dp,
                iconHeight = 18.dp,
                fontSize = 22.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(bots, key = { it.id }) { bot ->
                BotFeedCard(
                    bot = bot,
                    onCardClick = { onBotClick(bot.id) },
                    onLikeClick = { onToggleLike(bot) },
                    onFollowClick = { onToggleFollow(bot.creatorName, bot.isFollowing) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun BotFeedCard(
    bot: BotEntity,
    onCardClick: () -> Unit,
    onLikeClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bot_card_${bot.id}")
    ) {
        // Creator Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Creator avatar circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, ChaiBorder, CircleShape)
            ) {
                CharacterImage(
                    drawableName = bot.avatarDrawableName,
                    characterName = bot.creatorName,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = bot.creatorName,
                color = ChaiTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            // Follow button
            if (bot.isFollowing) {
                Button(
                    onClick = onFollowClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChaiRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("following_button_${bot.id}")
                ) {
                    Text("Following", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                OutlinedButton(
                    onClick = onFollowClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("follow_button_${bot.id}")
                ) {
                    Text("Follow", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Chat bubble icon
            IconButton(
                onClick = onCardClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Chat with bot",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Character Art Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onCardClick() }
        ) {
            CharacterImage(
                drawableName = bot.avatarDrawableName,
                characterName = bot.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        // Social Action Row: Like, Chat, Share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLikeClick() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("like_button_${bot.id}")
            ) {
                Icon(
                    imageVector = if (bot.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (bot.isLiked) ChaiRed else Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatStatCount(bot.likeCount),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Chat Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCardClick() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Chats",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatStatCount(bot.chatCount),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Share
            IconButton(
                onClick = {},
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = ChaiTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Description text: Creator name + Bot name + Tagline + Description
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { expanded = !expanded }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = bot.creatorName,
                    color = ChaiTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${bot.name} ${bot.tagline}",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bot.description,
                color = ChaiTextSecondary,
                fontSize = 13.sp,
                maxLines = if (expanded) 10 else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!expanded && bot.description.length > 60) {
                Text(
                    text = "... more",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun formatStatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000f)
        count >= 1000 -> String.format("%.1fK", count / 1000f)
        else -> count.toString()
    }
}
