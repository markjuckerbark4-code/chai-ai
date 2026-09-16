package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChaiDatabase
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PersonaEntity
import com.example.data.payment.PaymentSyncManager
import com.example.data.repository.ChaiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StreamingReply(
    val botId: String,
    val text: String,
    val isComplete: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChaiDatabase.getDatabase(application)
    private val repository = ChaiRepository(database.chaiDao())

    val allBots: StateFlow<List<BotEntity>> = repository.allBots.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPersonas: StateFlow<List<PersonaEntity>> = repository.allPersonas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Chat
    private val _activeBotId = MutableStateFlow<String?>(null)
    val activeBotId: StateFlow<String?> = _activeBotId.asStateFlow()

    val activeBotMessages: StateFlow<List<MessageEntity>> = _activeBotId.flatMapLatest { botId ->
        if (botId != null) {
            repository.getMessagesForBot(botId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state
    val selectedTab = MutableStateFlow(0) // 0: Home, 1: Search, 2: Create, 3: Chats, 4: Profile
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    val isLoggedIn = MutableStateFlow(false)
    val userAccount = MutableStateFlow(
        UserAccount(
            name = "Mark Juckerbark",
            email = "markjuckerbark4@gmail.com",
            provider = "Google",
            userId = "CHAI-939348",
            memberTier = "Standard Member",
            avatarInitial = "M",
            joinDate = "Sep 2024",
            isPremium = false,
            tier = "none",
            isBanned = false,
            expiryDate = null
        )
    )
    val userName = MutableStateFlow("Mark Juckerbark")
    val freeMessagesLeft = MutableStateFlow("0")
    val piecesCount = MutableStateFlow(200)

    val isSubscriptionSheetVisible = MutableStateFlow(false)
    val isGeneratingReply = MutableStateFlow(false)

    private var approvalJob: Job? = null

    val paymentSettings = MutableStateFlow(com.example.data.payment.PaymentSettings())

    private val _streamingBotReply = MutableStateFlow<StreamingReply?>(null)
    val streamingBotReply: StateFlow<StreamingReply?> = _streamingBotReply.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            // Fetch live payment settings from cloud
            paymentSettings.value = PaymentSyncManager.fetchPaymentSettings(getApplication())
            // Sync default/current user to cloud registry
            val current = userAccount.value
            PaymentSyncManager.syncUserRegistration(
                userId = current.userId,
                name = current.name,
                email = current.email,
                provider = current.provider
            )
            // Immediately start listening for admin changes from cloud
            startApprovalCheckLoop(current.email)
        }
    }

    fun openChat(botId: String) {
        _activeBotId.value = botId
    }

    fun closeChat() {
        _activeBotId.value = null
    }

    fun sendMessage(text: String) {
        val currentBotId = _activeBotId.value ?: return
        if (text.isBlank() || isGeneratingReply.value || _streamingBotReply.value != null) return

        val bot = allBots.value.find { it.id == currentBotId } ?: return

        viewModelScope.launch {
            isGeneratingReply.value = true
            try {
                val fullReply = repository.generateReplyForUser(bot, text.trim())
                isGeneratingReply.value = false

                // Chai style typewriter live streaming ("colo to")
                val totalLength = fullReply.length
                val step = if (totalLength > 120) 3 else 2
                var cur = 0
                while (cur < totalLength) {
                    cur = (cur + step).coerceAtMost(totalLength)
                    _streamingBotReply.value = StreamingReply(
                        botId = bot.id,
                        text = fullReply.substring(0, cur),
                        isComplete = cur >= totalLength
                    )
                    delay(20)
                }
                delay(120)
                repository.saveBotMessage(bot.id, fullReply)
                _streamingBotReply.value = null
            } catch (e: Exception) {
                isGeneratingReply.value = false
                _streamingBotReply.value = null
            }
        }
    }

    fun rerollBotResponse() {
        val currentBotId = _activeBotId.value ?: return
        val bot = allBots.value.find { it.id == currentBotId } ?: return
        if (isGeneratingReply.value || _streamingBotReply.value != null) return

        viewModelScope.launch {
            isGeneratingReply.value = true
            try {
                val fullReply = repository.rerollLastBotMessage(bot)
                isGeneratingReply.value = false

                val totalLength = fullReply.length
                val step = if (totalLength > 120) 3 else 2
                var cur = 0
                while (cur < totalLength) {
                    cur = (cur + step).coerceAtMost(totalLength)
                    _streamingBotReply.value = StreamingReply(
                        botId = bot.id,
                        text = fullReply.substring(0, cur),
                        isComplete = cur >= totalLength
                    )
                    delay(20)
                }
                delay(120)
                repository.saveBotMessage(bot.id, fullReply)
                _streamingBotReply.value = null
            } catch (e: Exception) {
                isGeneratingReply.value = false
                _streamingBotReply.value = null
            }
        }
    }

    fun clearActiveChat() {
        val currentBotId = _activeBotId.value ?: return
        val bot = allBots.value.find { it.id == currentBotId } ?: return
        viewModelScope.launch {
            repository.clearChat(bot.id, bot.firstMessage)
        }
    }

    fun editMessage(id: Long, newText: String) {
        viewModelScope.launch {
            repository.updateMessage(id, newText)
        }
    }

    fun toggleLike(bot: BotEntity) {
        viewModelScope.launch {
            repository.toggleLike(bot)
        }
    }

    fun toggleFollow(creatorName: String, currentFollow: Boolean) {
        viewModelScope.launch {
            repository.toggleFollow(creatorName, currentFollow)
        }
    }

    fun createNewBot(
        name: String,
        creatorName: String,
        tagline: String,
        description: String,
        firstMessage: String,
        personalityPrompt: String,
        category: String,
        avatarName: String = "img_bot_scarlett"
    ) {
        viewModelScope.launch {
            val id = "user_bot_" + System.currentTimeMillis()
            val newBot = BotEntity(
                id = id,
                name = name,
                creatorName = creatorName.ifBlank { userName.value },
                tagline = tagline,
                description = description,
                avatarDrawableName = avatarName,
                firstMessage = firstMessage,
                personalityPrompt = personalityPrompt,
                category = category,
                likeCount = 1,
                chatCount = 1,
                isUserCreated = true
            )
            repository.createBot(newBot)
            // Automatically open chat with the newly created bot
            _activeBotId.value = id
        }
    }

    fun addPersona(name: String, pronouns: String, description: String) {
        viewModelScope.launch {
            repository.addPersona(name, pronouns, description)
        }
    }

    fun selectPersona(personaId: Long) {
        viewModelScope.launch {
            repository.selectPersona(personaId)
        }
    }

    fun deletePersona(personaId: Long) {
        viewModelScope.launch {
            repository.deletePersona(personaId)
        }
    }

    fun updateUserName(name: String) {
        userName.value = name
        userAccount.value = userAccount.value.copy(
            name = name,
            avatarInitial = name.firstOrNull()?.uppercase() ?: "U"
        )
    }

    fun login(name: String, email: String, provider: String) {
        val initial = name.firstOrNull()?.uppercase() ?: "U"
        val uid = "CHAI-" + (100000 + (Math.random() * 900000).toInt())
        userAccount.value = UserAccount(
            name = name,
            email = email,
            provider = provider,
            userId = uid,
            memberTier = "Standard Member",
            avatarInitial = initial,
            joinDate = "Sep 2024",
            isPremium = false,
            tier = "none",
            isBanned = false,
            expiryDate = null
        )
        userName.value = name
        isLoggedIn.value = true
        // Sync user registration to cloud KV database immediately
        viewModelScope.launch {
            PaymentSyncManager.syncUserRegistration(uid, name, email, provider)
        }
        // Directly show Account/Profile tab upon login
        selectedTab.value = 4
        // Automatically show "Premium কিনুন" popup dialog on new login as requested!
        isSubscriptionSheetVisible.value = true
        // Check if user is already approved in cloud or start continuous sync
        startApprovalCheckLoop(email)
    }

    fun logout() {
        approvalJob?.cancel()
        isLoggedIn.value = false
        selectedTab.value = 0
    }

    fun chargePieces(amount: Int) {
        piecesCount.value += amount
    }

    fun refreshPaymentSettings() {
        viewModelScope.launch {
            paymentSettings.value = PaymentSyncManager.fetchPaymentSettings(getApplication())
        }
    }

    fun buyPremium(
        plan: String = "Weekly",
        paymentMethod: String = "bKash",
        senderNumber: String = "",
        trxId: String = "",
        amount: String = "",
        screenshotNote: String = ""
    ) {
        val email = userAccount.value.email
        val name = userAccount.value.name
        val resolvedAmount = if (amount.isNotBlank()) {
            amount
        } else if (paymentMethod.contains("Crypto", ignoreCase = true) || paymentMethod.contains("USDT", ignoreCase = true)) {
            if (plan.contains("Yearly", ignoreCase = true) || plan.contains("18")) "$18" else "$2"
        } else {
            if (plan.contains("2000") || plan.contains("Yearly", ignoreCase = true)) "৳2000" else "৳230"
        }

        // Submit order to cloud so admin panel receives it immediately!
        viewModelScope.launch {
            PaymentSyncManager.submitOrder(
                context = getApplication(),
                userName = name,
                userEmail = email,
                paymentMethod = paymentMethod,
                senderNumber = senderNumber,
                trxId = trxId,
                plan = plan,
                amount = resolvedAmount,
                screenshotNote = screenshotNote
            )
            // Continuously listen for admin approval
            startApprovalCheckLoop(email)
        }

        isSubscriptionSheetVisible.value = false
    }

    fun grantPremium(tierName: String = "Chai Ultra (10 Months)", tierCode: String = "ultra") {
        userAccount.value = userAccount.value.copy(
            memberTier = tierName,
            isPremium = true,
            tier = tierCode
        )
        freeMessagesLeft.value = "Unlimited"
    }

    fun startApprovalCheckLoop(email: String) {
        if (email.isBlank()) return
        approvalJob?.cancel()
        approvalJob = viewModelScope.launch {
            while (true) {
                val cloudStatus = PaymentSyncManager.fetchUserStatus(email)
                if (cloudStatus != null) {
                    val isBanned = cloudStatus.isBanned
                    val tier = cloudStatus.tier.lowercase()
                    val isPrem = cloudStatus.isPremium || tier == "premium" || tier == "ultra"

                    val memberTitle = when (tier) {
                        "ultra" -> "Chai Ultra (10 Months)"
                        "premium" -> "Chai Premium (7 Days)"
                        else -> if (isPrem) "Chai Ultra Premium" else "Standard Member"
                    }

                    userAccount.value = userAccount.value.copy(
                        isBanned = isBanned,
                        tier = tier,
                        isPremium = isPrem,
                        memberTier = memberTitle,
                        expiryDate = cloudStatus.expiryDate
                    )

                    if (isPrem) {
                        freeMessagesLeft.value = "Unlimited"
                    } else {
                        freeMessagesLeft.value = "0"
                    }
                }
                delay(3000)
            }
        }
    }

    fun showSubscriptionSheet(show: Boolean) {
        isSubscriptionSheetVisible.value = show
    }

    fun setTab(index: Int) {
        selectedTab.value = index
    }
}

data class UserAccount(
    val name: String = "Mark Juckerbark",
    val email: String = "markjuckerbark4@gmail.com",
    val provider: String = "Google",
    val userId: String = "CHAI-939348",
    val memberTier: String = "Standard Member",
    val avatarInitial: String = "M",
    val joinDate: String = "Sep 2024",
    val isPremium: Boolean = false,
    val tier: String = "none", // "none", "premium", "ultra"
    val isBanned: Boolean = false,
    val expiryDate: String? = null
)
