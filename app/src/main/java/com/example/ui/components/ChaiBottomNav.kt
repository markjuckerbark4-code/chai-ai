package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.ChaiBorder
import com.example.ui.theme.ChaiRed
import com.example.ui.theme.ChaiTextSecondary

@Composable
fun ChaiBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChaiBlack)
            .border(
                width = 1.dp,
                color = ChaiBorder.copy(alpha = 0.5f)
            )
            .navigationBarsPadding()
            .height(58.dp)
            .testTag("chai_bottom_nav")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Home
            BottomNavItem(
                isSelected = selectedTab == 0,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                contentDescription = "Home",
                tag = "tab_home",
                onClick = { onTabSelected(0) }
            )

            // Tab 1: Search
            BottomNavItem(
                isSelected = selectedTab == 1,
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Filled.Search,
                contentDescription = "Search",
                tag = "tab_search",
                onClick = { onTabSelected(1) }
            )

            // Tab 2: Create (+)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (selectedTab == 2) ChaiRed else Color(0xFF24242E))
                    .clickable { onTabSelected(2) }
                    .testTag("tab_create"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Bot",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 3: Chats
            BottomNavItem(
                isSelected = selectedTab == 3,
                selectedIcon = Icons.Filled.ChatBubble,
                unselectedIcon = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Chats",
                tag = "tab_chats",
                onClick = { onTabSelected(3) }
            )

            // Tab 4: Profile
            BottomNavItem(
                isSelected = selectedTab == 4,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.PersonOutline,
                contentDescription = "Profile",
                tag = "tab_profile",
                onClick = { onTabSelected(4) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    isSelected: Boolean,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color.White else ChaiTextSecondary,
            modifier = Modifier.size(26.dp)
        )
    }
}
