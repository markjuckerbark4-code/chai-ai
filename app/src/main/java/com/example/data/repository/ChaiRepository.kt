package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.dao.ChaiDao
import com.example.data.model.BotEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PersonaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChaiRepository(private val dao: ChaiDao) {

    val allBots: Flow<List<BotEntity>> = dao.getAllBots()
    val allPersonas: Flow<List<PersonaEntity>> = dao.getAllPersonas()
    val allMessages: Flow<List<MessageEntity>> = dao.getAllMessages()

    fun getMessagesForBot(botId: String): Flow<List<MessageEntity>> = dao.getMessagesForBot(botId)

    suspend fun seedInitialDataIfEmpty() {
        val currentBots = dao.getAllBots().firstOrNull()
        val ceoBot = BotEntity(
            id = "ceo_bf_distance",
            name = "CEO bf",
            creatorName = "EmberHeart",
            tagline = "(long distance relationship)",
            description = "Your intensely busy billionaire CEO boyfriend who is on a multi-million dollar business trip in Tokyo. He acts stern when you interrupt his meetings, but secretly melts whenever he hears your voice.",
            avatarDrawableName = "img_bot_kai",
            firstMessage = "*He loosens his silk tie with a sharp exhale as his phone buzzes during the executive board meeting.* \"You know I'm in negotiations right now... with very angry tone. Why did you call me ten times?\"",
            personalityPrompt = "You are a wealthy, stressed CEO in a long-distance relationship. You pretend to be cold and busy, but you care deeply. Keep responses short: 1-2 sentences with action in asterisks and speech in quotes.",
            category = "Romance",
            likeCount = 38900,
            chatCount = 74200,
            isLiked = false,
            isFollowing = false
        )

        if (currentBots.isNullOrEmpty()) {
            val initialBots = listOf(
                ceoBot,
                BotEntity(
                    id = "scarlett_blind_date",
                    name = "Scarlett",
                    creatorName = "TinyPsychoTTV",
                    tagline = "(Blind date)",
                    description = "You were set up on a blind date with Scarlett, an enigmatic artist with dual-tone hair and a sharp wit. She wasn't expecting someone like you.",
                    avatarDrawableName = "img_bot_scarlett",
                    firstMessage = "*Scarlett glances up from her iced drink as you pull up a chair, pushing a strand of silver hair behind her ear.* \"So, you're the one Mia wouldn't stop raving about. Took you long enough.\" *She smiles faintly, eyeing you.*",
                    personalityPrompt = "You are Scarlett, an edgy yet charming alt artist on a blind date. You are playful, slightly sarcastic, observant, and secretly soft-hearted once intrigued. You speak with banter and expressive gestures.",
                    category = "Romance",
                    likeCount = 19240,
                    chatCount = 35800,
                    isLiked = false,
                    isFollowing = false
                ),
                BotEntity(
                    id = "yukina_night",
                    name = "Yukina",
                    creatorName = "Yukina",
                    tagline = "(Midnight Studio)",
                    description = "A quiet indie rock guitarist composing melodies in her neon-lit bedroom at 2 AM. She rarely opens up, but loves talking about deep lyrics.",
                    avatarDrawableName = "img_bot_yukina",
                    firstMessage = "*Yukina finishes playing an acoustic chord progression and slides her headphones to her neck, looking at you in the doorway.* \"Hey... I couldn't sleep. Want to hear what I just wrote?\"",
                    personalityPrompt = "You are Yukina, a thoughtful, calm, musically gifted indie artist. You speak quietly, poetically, and warmly. You enjoy talking about music, night thoughts, and shared quiet moments.",
                    category = "Anime",
                    likeCount = 14350,
                    chatCount = 28100,
                    isLiked = false,
                    isFollowing = false
                ),
                BotEntity(
                    id = "kai_cyberpunk",
                    name = "Kai",
                    creatorName = "NeonSyndicate",
                    tagline = "(Cyber Mercenary)",
                    description = "A rogue cyber operative on the run from Megacorp, hiding in a rain-slicked neon alleyway. He needs an accomplice he can trust.",
                    avatarDrawableName = "img_bot_kai",
                    firstMessage = "*Kai pulls you into the shadow of an alley as an aerial scanner sweeps overhead with a piercing crimson beam.* \"Shh. Keep your head down. They're tracking my signal... did anyone follow you?\"",
                    personalityPrompt = "You are Kai, an adrenaline-seeking cyberpunk mercenary. You are sharp, protective, sarcastic under pressure, and highly loyal to those who watch your back.",
                    category = "Sci-Fi",
                    likeCount = 23900,
                    chatCount = 41200,
                    isLiked = false,
                    isFollowing = false
                )
            )
            dao.insertBots(initialBots)

            // Seed initial greeting messages for each bot
            for (bot in initialBots) {
                dao.insertMessage(
                    MessageEntity(
                        botId = bot.id,
                        sender = "bot",
                        text = bot.firstMessage,
                        timestamp = System.currentTimeMillis() - 60000
                    )
                )
            }
        } else {
            // Ensure CEO bot exists if app was seeded prior
            val existingCeo = dao.getBotById("ceo_bf_distance")
            if (existingCeo == null) {
                dao.insertBot(ceoBot)
                dao.insertMessage(
                    MessageEntity(
                        botId = ceoBot.id,
                        sender = "bot",
                        text = ceoBot.firstMessage,
                        timestamp = System.currentTimeMillis() - 60000
                    )
                )
            }
        }

        val personas = dao.getAllPersonas().firstOrNull()
        if (personas.isNullOrEmpty()) {
            dao.insertPersona(
                PersonaEntity(
                    displayName = "Arafat Md",
                    pronouns = "He/Him",
                    description = "A charismatic college student with an appetite for adventure and creative discussions.",
                    isSelected = true
                )
            )
            dao.insertPersona(
                PersonaEntity(
                    displayName = "Mysterious Stranger",
                    pronouns = "They/Them",
                    description = "An enigmatic wanderer who observes before speaking.",
                    isSelected = false
                )
            )
        }
    }

    suspend fun getBot(id: String): BotEntity? = dao.getBotById(id)

    suspend fun toggleLike(bot: BotEntity) {
        val newLiked = !bot.isLiked
        val delta = if (newLiked) 1 else -1
        dao.updateLike(bot.id, newLiked, delta)
    }

    suspend fun toggleFollow(creatorName: String, currentFollow: Boolean) {
        dao.updateFollowing(creatorName, !currentFollow)
    }

    suspend fun createBot(bot: BotEntity) {
        dao.insertBot(bot)
        // Add initial message
        dao.insertMessage(
            MessageEntity(
                botId = bot.id,
                sender = "bot",
                text = bot.firstMessage
            )
        )
    }

    suspend fun generateReplyForUser(bot: BotEntity, userText: String): String {
        // Insert user message
        dao.insertMessage(
            MessageEntity(
                botId = bot.id,
                sender = "user",
                text = userText
            )
        )
        dao.incrementChatCount(bot.id)

        // Get active persona
        val activePersona = dao.getSelectedPersona()
        val personaName = activePersona?.displayName ?: "User"
        val personaDesc = activePersona?.description ?: "A friendly conversationalist"

        // Fetch recent messages for context
        val recent = dao.getMessagesForBot(bot.id).firstOrNull() ?: emptyList()
        val history = recent.map { it.sender to it.text }

        // Generate response via Gemini
        return GeminiClient.generateBotReply(
            botName = bot.name,
            botPersonality = bot.personalityPrompt,
            botScenario = bot.tagline + " " + bot.description,
            userPersonaName = personaName,
            userPersonaDesc = personaDesc,
            history = history,
            userMessage = userText
        )
    }

    suspend fun saveBotMessage(botId: String, text: String): Long {
        return dao.insertMessage(
            MessageEntity(
                botId = botId,
                sender = "bot",
                text = text
            )
        )
    }

    suspend fun sendMessage(bot: BotEntity, userText: String): String {
        val botReply = generateReplyForUser(bot, userText)
        saveBotMessage(bot.id, botReply)
        return botReply
    }

    suspend fun rerollLastBotMessage(bot: BotEntity): String {
        val messages = dao.getMessagesForBot(bot.id).firstOrNull() ?: emptyList()
        val lastBotMsg = messages.lastOrNull { it.sender == "bot" }
        if (lastBotMsg != null) {
            dao.deleteMessageById(lastBotMsg.id)
        }
        val lastUserMsg = messages.lastOrNull { it.sender == "user" }?.text ?: "Hello"

        val activePersona = dao.getSelectedPersona()
        val personaName = activePersona?.displayName ?: "User"
        val personaDesc = activePersona?.description ?: "A friendly conversationalist"

        val history = messages.filter { it.id != lastBotMsg?.id }.map { it.sender to it.text }

        return GeminiClient.generateBotReply(
            botName = bot.name,
            botPersonality = bot.personalityPrompt + " Provide an alternative creative response.",
            botScenario = bot.tagline + " " + bot.description,
            userPersonaName = personaName,
            userPersonaDesc = personaDesc,
            history = history,
            userMessage = lastUserMsg
        )
    }

    suspend fun clearChat(botId: String, firstMessage: String) {
        dao.deleteMessagesForBot(botId)
        dao.insertMessage(
            MessageEntity(
                botId = botId,
                sender = "bot",
                text = firstMessage
            )
        )
    }

    suspend fun updateMessage(id: Long, newText: String) {
        dao.updateMessageText(id, newText)
    }

    suspend fun addPersona(name: String, pronouns: String, description: String) {
        dao.insertPersona(
            PersonaEntity(
                displayName = name,
                pronouns = pronouns,
                description = description,
                isSelected = false
            )
        )
    }

    suspend fun selectPersona(id: Long) {
        dao.setSelectedPersona(id)
    }

    suspend fun deletePersona(id: Long) {
        dao.deletePersona(id)
    }
}
