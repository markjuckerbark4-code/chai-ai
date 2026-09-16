package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.ChaiBottomNav
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ChatsListScreen
import com.example.ui.screens.CreateBotScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SubscriptionDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.ChaiBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChaiApp()
            }
        }
    }
}

@Composable
fun ChaiApp(viewModel: MainViewModel = viewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val allBots by viewModel.allBots.collectAsState()
    val allPersonas by viewModel.allPersonas.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    val activeBotId by viewModel.activeBotId.collectAsState()
    val activeBotMessages by viewModel.activeBotMessages.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val freeMessages by viewModel.freeMessagesLeft.collectAsState()
    val piecesCount by viewModel.piecesCount.collectAsState()
    val isSubscriptionSheetVisible by viewModel.isSubscriptionSheetVisible.collectAsState()
    val isGeneratingReply by viewModel.isGeneratingReply.collectAsState()
    val streamingReply by viewModel.streamingBotReply.collectAsState()

    val activePersona = allPersonas.find { it.isSelected } ?: allPersonas.firstOrNull()
    val activeBot = allBots.find { it.id == activeBotId }

    if (!isLoggedIn) {
        WelcomeScreen(
            onSignIn = { name, email, provider ->
                viewModel.login(name, email, provider)
            }
        )
    } else {
        if (activeBot != null) {
            BackHandler {
                viewModel.closeChat()
            }

            ChatScreen(
                bot = activeBot,
                messages = activeBotMessages,
                activePersona = activePersona,
                isGenerating = isGeneratingReply,
                streamingReply = streamingReply,
                onBack = { viewModel.closeChat() },
                onSendMessage = { text -> viewModel.sendMessage(text) },
                onReroll = { viewModel.rerollBotResponse() },
                onClearChat = { viewModel.clearActiveChat() },
                onOpenUpgrade = { viewModel.showSubscriptionSheet(true) },
                onEditMessage = { id, text -> viewModel.editMessage(id, text) }
            )
        } else {
            BackHandler(enabled = selectedTab != 0) {
                viewModel.setTab(0)
            }

            Scaffold(
                containerColor = ChaiBlack,
                bottomBar = {
                    ChaiBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            0 -> FeedScreen(
                                bots = allBots,
                                onBotClick = { botId -> viewModel.openChat(botId) },
                                onToggleLike = { bot -> viewModel.toggleLike(bot) },
                                onToggleFollow = { creator, curFollow -> viewModel.toggleFollow(creator, curFollow) }
                            )
                            1 -> SearchScreen(
                                bots = allBots,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.searchQuery.value = it },
                                selectedCategory = selectedCategory,
                                onCategorySelect = { viewModel.selectedCategory.value = it },
                                onBotClick = { botId -> viewModel.openChat(botId) }
                            )
                            2 -> CreateBotScreen(
                                currentUserName = userAccount.name,
                                isPremium = userAccount.isPremium,
                                onOpenUpgrade = { viewModel.showSubscriptionSheet(true) },
                                onCreateBot = { name, creator, tagline, desc, firstMsg, prompt, cat, avatar ->
                                    viewModel.createNewBot(name, creator, tagline, desc, firstMsg, prompt, cat, avatar)
                                }
                            )
                            3 -> ChatsListScreen(
                                bots = allBots,
                                allMessages = allMessages,
                                onOpenChat = { botId -> viewModel.openChat(botId) }
                            )
                            4 -> ProfileScreen(
                                userAccount = userAccount,
                                freeMessages = freeMessages,
                                piecesCount = piecesCount,
                                personas = allPersonas,
                                onUpdateName = { viewModel.updateUserName(it) },
                                onOpenUpgrade = { viewModel.showSubscriptionSheet(true) },
                                onChargePieces = { viewModel.chargePieces(it) },
                                onAddPersona = { name, pronouns, desc -> viewModel.addPersona(name, pronouns, desc) },
                                onSelectPersona = { viewModel.selectPersona(it) },
                                onDeletePersona = { viewModel.deletePersona(it) },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }
                }
            }
        }

        // Subscription Sheet Dialog
        if (isSubscriptionSheetVisible) {
            SubscriptionDialog(
                onDismiss = { viewModel.showSubscriptionSheet(false) },
                onBuyPremium = { plan, method, senderNumber, trxId ->
                    viewModel.buyPremium(plan, method, senderNumber, trxId)
                }
            )
        }
    }
}
