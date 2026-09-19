package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChaiDatabase
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PersonaEntity
import com.example.data.payment.CloudUserStatus
import com.example.data.payment.PaymentSyncManager
import com.example.data.repository.ChaiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
            name = "Chai User",
            email = "",
            provider = "Google",
            userId = "CHAI-000000",
            memberTier = "Standard Member",
            avatarInitial = "U",
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

    companion object {
        private const val AUTH_PREFS = "chai_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_PROVIDER = "key_user_provider"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_MEMBER_TIER = "key_member_tier"
        private const val KEY_AVATAR_INITIAL = "key_avatar_initial"
        private const val KEY_JOIN_DATE = "key_join_date"
        private const val KEY_IS_PREMIUM = "key_is_premium"
        private const val KEY_TIER = "key_tier"
        private const val KEY_IS_BANNED = "key_is_banned"
        private const val KEY_EXPIRY_DATE = "key_expiry_date"
    }

    init {
        restoreUserSession()
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            // Fetch live payment settings from cloud
            paymentSettings.value = PaymentSyncManager.fetchPaymentSettings(getApplication())
            // If logged in, sync current user to cloud registry and check status
            val current = userAccount.value
            if (isLoggedIn.value && current.email.isNotBlank()) {
                PaymentSyncManager.syncUserRegistration(
                    userId = current.userId,
                    name = current.name,
                    email = current.email,
                    provider = current.provider
                )
                checkUserStatusOnce(current.email)
            }
        }
    }

    private fun restoreUserSession() {
        try {
            val prefs = getApplication<Application>().getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            val savedLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            val savedEmail = prefs.getString(KEY_USER_EMAIL, "") ?: ""
            if (savedLoggedIn && savedEmail.isNotBlank()) {
                val savedName = prefs.getString(KEY_USER_NAME, "Chai User") ?: "Chai User"
                val savedProvider = prefs.getString(KEY_USER_PROVIDER, "Google") ?: "Google"
                val savedUserId = prefs.getString(KEY_USER_ID, "CHAI-000000") ?: "CHAI-000000"
                val savedMemberTier = prefs.getString(KEY_MEMBER_TIER, "Standard Member") ?: "Standard Member"
                val savedAvatarInitial = prefs.getString(KEY_AVATAR_INITIAL, "U") ?: "U"
                val savedJoinDate = prefs.getString(KEY_JOIN_DATE, "Sep 2024") ?: "Sep 2024"
                val savedIsPremium = prefs.getBoolean(KEY_IS_PREMIUM, false)
                val savedTier = prefs.getString(KEY_TIER, "none") ?: "none"
                val savedIsBanned = prefs.getBoolean(KEY_IS_BANNED, false)
                val savedExpiryDate = prefs.getString(KEY_EXPIRY_DATE, null)

                val restoredAccount = UserAccount(
                    name = savedName,
                    email = savedEmail,
                    provider = savedProvider,
                    userId = savedUserId,
                    memberTier = savedMemberTier,
                    avatarInitial = savedAvatarInitial,
                    joinDate = savedJoinDate,
                    isPremium = savedIsPremium,
                    tier = savedTier,
                    isBanned = savedIsBanned,
                    expiryDate = savedExpiryDate
                )
                userAccount.value = restoredAccount
                userName.value = savedName
                isLoggedIn.value = true
                freeMessagesLeft.value = if (savedIsPremium) "Unlimited" else "0"
            }
        } catch (e: Exception) {
            android.util.Log.w("MainViewModel", "Error restoring user session: ${e.message}")
        }
    }

    private fun saveAccountToPrefs(account: UserAccount, loggedIn: Boolean) {
        try {
            val prefs = getApplication<Application>().getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                .putString(KEY_USER_NAME, account.name)
                .putString(KEY_USER_EMAIL, account.email)
                .putString(KEY_USER_PROVIDER, account.provider)
                .putString(KEY_USER_ID, account.userId)
                .putString(KEY_MEMBER_TIER, account.memberTier)
                .putString(KEY_AVATAR_INITIAL, account.avatarInitial)
                .putString(KEY_JOIN_DATE, account.joinDate)
                .putBoolean(KEY_IS_PREMIUM, account.isPremium)
                .putString(KEY_TIER, account.tier)
                .putBoolean(KEY_IS_BANNED, account.isBanned)
                .putString(KEY_EXPIRY_DATE, account.expiryDate)
                .apply()
        } catch (e: Exception) {
            android.util.Log.w("MainViewModel", "Error saving user session: ${e.message}")
        }
    }

    fun openChat(botId: String) {
        _activeBotId.value = botId
    }

    fun closeChat() {
        _activeBotId.value = null
    }

    fun sendMessage(text: String) {
        if (!userAccount.value.isPremium) {
            isSubscriptionSheetVisible.value = true
            return
        }
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
        if (!userAccount.value.isPremium) {
            isSubscriptionSheetVisible.value = true
            return
        }
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
        val newAccount = UserAccount(
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
        userAccount.value = newAccount
        userName.value = name
        isLoggedIn.value = true

        // Persist session locally immediately so user stays logged in across app restarts
        saveAccountToPrefs(newAccount, loggedIn = true)

        // Sync user registration to cloud KV database immediately
        viewModelScope.launch {
            PaymentSyncManager.syncUserRegistration(uid, name, email, provider)
            // Immediately check status in case user already had active subscription
            val status = PaymentSyncManager.fetchUserStatus(email, getApplication())
            if (status != null) {
                applyUserStatus(status)
            }
        }
        // Directly show Account/Profile tab upon login
        selectedTab.value = 4
        // Automatically show "Premium কিনুন" popup dialog on new login as requested!
        isSubscriptionSheetVisible.value = true
    }

    fun logout() {
        approvalJob?.cancel()
        isLoggedIn.value = false
        selectedTab.value = 0
        try {
            val prefs = getApplication<Application>().getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
        } catch (e: Exception) {
            android.util.Log.w("MainViewModel", "Logout error: ${e.message}")
        }
    }

    fun chargePieces(amount: Int) {
        piecesCount.value += amount
    }

    fun refreshPaymentSettings() {
        viewModelScope.launch {
            paymentSettings.value = PaymentSyncManager.fetchPaymentSettings(getApplication())
        }
    }

    fun checkUserStatusOnce(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            val cloudStatus = PaymentSyncManager.fetchUserStatus(email, getApplication())
            if (cloudStatus != null) {
                applyUserStatus(cloudStatus)
            }
        }
    }

    private fun applyUserStatus(cloudStatus: CloudUserStatus) {
        val isBanned = cloudStatus.isBanned
        val tier = cloudStatus.tier.lowercase()
        val isPrem = cloudStatus.isPremium || tier == "premium" || tier == "ultra"

        val memberTitle = when (tier) {
            "ultra" -> "Chai Ultra (10 Months)"
            "premium" -> "Chai Premium (7 Days)"
            else -> if (isPrem) "Chai Ultra Premium" else "Standard Member"
        }

        val updatedAccount = userAccount.value.copy(
            isBanned = isBanned,
            tier = tier,
            isPremium = isPrem,
            memberTier = memberTitle,
            expiryDate = cloudStatus.expiryDate
        )
        userAccount.value = updatedAccount

        if (isPrem) {
            freeMessagesLeft.value = "Unlimited"
        } else {
            freeMessagesLeft.value = "0"
        }

        // Persist updated status locally
        saveAccountToPrefs(updatedAccount, loggedIn = isLoggedIn.value)
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
            var attempts = 0
            val maxAttempts = 15 // Check for up to 5 minutes
            while (isActive && attempts < maxAttempts) {
                attempts++
                delay(20000L) // check every 20 seconds
                val cloudStatus = PaymentSyncManager.fetchUserStatus(email, getApplication())
                if (cloudStatus != null) {
                    applyUserStatus(cloudStatus)
                    if (cloudStatus.isPremium) {
                        // User is now premium! Stop polling loop.
                        break
                    }
                }
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
    val name: String = "Chai User",
    val email: String = "",
    val provider: String = "Google",
    val userId: String = "CHAI-000000",
    val memberTier: String = "Standard Member",
    val avatarInitial: String = "U",
    val joinDate: String = "Sep 2024",
    val isPremium: Boolean = false,
    val tier: String = "none", // "none", "premium", "ultra"
    val isBanned: Boolean = false,
    val expiryDate: String? = null
)
