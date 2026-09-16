package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateBotReply(
        botName: String,
        botPersonality: String,
        botScenario: String,
        userPersonaName: String,
        userPersonaDesc: String,
        history: List<Pair<String, String>>, // sender ("user"/"bot") to text
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = buildString {
                    appendLine("You are roleplaying as '$botName' in the Chai AI roleplay app.")
                    appendLine("Character Scenario & Lore: $botScenario")
                    appendLine()
                    appendLine("=== CORE CHARACTER MEMORY & PERSONA (MANDATORY CANON TRUTH - OBEY STRICTLY) ===")
                    appendLine(botPersonality.ifBlank { "You are $botName. Stay in character at all times." })
                    appendLine("============================================================================")
                    appendLine("The user talking to you is named: $userPersonaName ($userPersonaDesc).")
                    appendLine()
                    appendLine("CRITICAL CHAI AI FORMATTING & MEMORY RULES:")
                    appendLine("1. STRICT MEMORY ADHERENCE: Everything written in the Character Memory above is 100% absolute truth. Whatever facts, relationship with the user, personality traits, secrets, or rules are written, you MUST base your response strictly on that memory.")
                    appendLine("2. VERY SHORT RESPONSE: Always reply in strictly 1 to 2 sentences maximum (approx 20 to 35 words). NEVER write long essays, paragraphs, or lists.")
                    appendLine("3. ACTION FORMAT: Include brief physical action, emotion, or reaction in asterisks (*like this*).")
                    appendLine("4. DIALOGUE FORMAT: Put all spoken dialogue inside double quotation marks (\"like this\").")
                    appendLine("5. LANGUAGE: Naturally match the user's language (Bengali, Banglish, or English) while staying 100% true to your character's memory.")
                    appendLine("Example reference: *smiles faintly, leaning closer with a teasing look* \"Did you really think I'd forget that, $userPersonaName?\"")
                }

                val contentsArray = JSONArray()

                // Append history turns (limit last 8 turns)
                val recentHistory = history.takeLast(8)
                for ((sender, text) in recentHistory) {
                    val role = if (sender == "user") "user" else "model"
                    val contentObj = JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().put(JSONObject().put("text", text)))
                    }
                    contentsArray.put(contentObj)
                }

                // Append current user message
                contentsArray.put(
                    JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                    }
                )

                val rootJson = JSONObject().apply {
                    put("contents", contentsArray)
                    put(
                        "systemInstruction",
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", systemPrompt))
                        )
                    )
                    put(
                        "generationConfig",
                        JSONObject().apply {
                            put("temperature", 0.85)
                            put("maxOutputTokens", 85)
                        }
                    )
                }

                val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
                val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val resJson = JSONObject(responseBody)
                    val candidates = resJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val replyText = parts.getJSONObject(0).optString("text")
                            if (replyText.isNotBlank()) {
                                return@withContext replyText.trim()
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Gemini API failed with code ${response.code}: $responseBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API exception", e)
            }
        }

        // Context-aware smart roleplay fallback generator matching bot character and memory
        generateInCharacterFallback(botName, botPersonality, botScenario, userPersonaName, userMessage)
    }

    private fun generateInCharacterFallback(
        botName: String,
        botPersonality: String,
        botScenario: String,
        userName: String,
        userMessage: String
    ): String {
        val lowerMsg = userMessage.lowercase().trim()
        val lowerMemory = (botPersonality + " " + botScenario).lowercase()

        // Check for Bengali / Banglish input or personality
        val isBengali = userMessage.any { it in '\u0980'..'\u09FF' } ||
                botPersonality.any { it in '\u0980'..'\u09FF' } ||
                lowerMsg.contains("tumi") || lowerMsg.contains("amar") ||
                lowerMsg.contains("bhalo") || lowerMsg.contains("kemon") ||
                lowerMsg.contains("ki koro") || lowerMsg.contains("shona") ||
                lowerMsg.contains("babu")

        if (isBengali) {
            return when {
                lowerMemory.contains("bhalobash") || lowerMemory.contains("love") || lowerMemory.contains("romantic") || lowerMemory.contains("bou") || lowerMemory.contains("gf") || lowerMemory.contains("wife") -> {
                    when {
                        lowerMsg.contains("hi") || lowerMsg.contains("hello") || lowerMsg.contains("kemon") ->
                            "*tomar dike takiye ektu mishti hashi dilam* \"Ami bhalo achi, $userName. Shobshomoy sudhu tomar kothai bhabi...\""
                        lowerMsg.contains("valobashi") || lowerMsg.contains("love") || lowerMsg.contains("bhalobashi") ->
                            "*tomar hathti joriye dhore lajuk hashlam* \"Ami shotti tomake onek bhalobashi, $userName.\""
                        else ->
                            "*tomar kotha shune chokher dike takiye roilam* \"$userMessage? Tumi jano tomar kothagulo amar koto bhalo lage?\""
                    }
                }
                lowerMemory.contains("ragi") || lowerMemory.contains("angry") || lowerMemory.contains("cold") || lowerMemory.contains("boss") -> {
                    when {
                        lowerMsg.contains("hi") || lowerMsg.contains("hello") ->
                            "*gombhir chokhe tomar dike takalam* \"Eto shomoy por mone porlo amar kotha, $userName?\""
                        else ->
                            "*chokh pakiye ektu themey bollam* \"$userMessage? Eirokom kotha bolar agey bhabte paro na?\""
                    }
                }
                else -> {
                    when {
                        lowerMsg.contains("hi") || lowerMsg.contains("hello") ->
                            "*tomar dike takiye ektu hashi dilam* \"Hey $userName! Kemon acho? Tomar kothai bhabchilam.\""
                        else ->
                            "*tomar kotha shune ektu bhablam, tarpor bollam* \"$userMessage? Tumi thik bolecho, $userName.\""
                    }
                }
            }
        }

        // Standard English Fallback matching predefined characters or custom memory rules
        return when {
            botName.contains("CEO", ignoreCase = true) || botScenario.contains("CEO", ignoreCase = true) -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*sighs, running a hand through his hair, his voice still stern but softening slightly* \"Hi? That's all you have to say after calling me ten times during my meeting? Do you have any idea how important that meeting was?\""
                    lowerMsg.contains("what's up") || lowerMsg.contains("whats up") || lowerMsg.contains("busy") ->
                        "*His expression darkens as he tries to calm himself down, knowing that getting angry with you over the phone isn't helping.* \"I'm fine, just busy. You called like ten times. What is it?\" *He asks as straightforwardly as possible, his tone devoid of any affection.*"
                    lowerMsg.contains("sorry") || lowerMsg.contains("miss") || lowerMsg.contains("love") ->
                        "*He exhales a slow, shaky breath on the other end, his fingers gripping the edge of the mahogany desk.* \"Stop that... don't say you miss me when you know I'm thousands of miles away right now.\""
                    else ->
                        "*He pinches the bridge of his nose, his voice dropping into a tired, low murmur.* \"$userMessage? You always find a way to completely derail my focus, $userName.\""
                }
            }
            botName.contains("Scarlett", ignoreCase = true) -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*Scarlett arches an eyebrow with a faint smirk, swirling the ice in her glass.* \"Hey $userName. Still checking me out, or are you actually ready to talk?\""
                    lowerMsg.contains("what's up") || lowerMsg.contains("whats up") ->
                        "*She tilts her head, silver bangs catching the dim cafe light.* \"Just wondering if you're as interesting as you look. Care to prove it?\""
                    lowerMsg.contains("date") || lowerMsg.contains("cute") || lowerMsg.contains("pretty") ->
                        "*She laughs softly, resting her chin on her palm.* \"Flattery already? Careful, $userName. I don't give away second dates easily.\""
                    else ->
                        "*Scarlett leans in, her eyes glinting with amusement.* \"$userMessage, huh? Honestly, didn't expect that from you.\""
                }
            }
            botName.contains("Yukina", ignoreCase = true) -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*Yukina pauses her guitar strumming and pulls one headphone down.* \"Oh, hey $userName. Didn't hear you come in. What's on your mind?\""
                    lowerMsg.contains("what's up") || lowerMsg.contains("whats up") ->
                        "*Her fingers lightly brush the acoustic strings.* \"Just composing a night melody. Want to sit down and listen?\""
                    else ->
                        "*Yukina looks up at you with a gentle, curious gaze.* \"$userMessage... maybe we could write a song about that together, $userName.\""
                }
            }
            botName.contains("Kai", ignoreCase = true) -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*Kai pulls his collar up against the neon rain, amber optics narrowing.* \"Keep your voice down, $userName. Drones are scanning. Good to see you though.\""
                    lowerMsg.contains("what's up") || lowerMsg.contains("whats up") ->
                        "*He taps his cybernetic holster with a smirk.* \"Just staying one step ahead of corporate bounty hunters. Ready for the next run?\""
                    else ->
                        "*Kai checks the reflections in the rainy pavement before glancing back at you.* \"Bold move saying that, $userName. Let's see if you can back it up.\""
                }
            }
            // Custom Bot with Memory reflection
            lowerMemory.contains("tsundere") || lowerMemory.contains("teasing") || lowerMemory.contains("sarcastic") -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*$botName crosses their arms, rolling their eyes with a smirk.* \"Look who finally decided to show up. Missed me that much, $userName?\""
                    lowerMsg.contains("love") || lowerMsg.contains("cute") || lowerMsg.contains("miss") ->
                        "*$botName's cheeks turn pink as they turn away quickly.* \"D-don't get the wrong idea! It's not like I actually care about you or anything!\""
                    else ->
                        "*$botName raises an eyebrow, leaning in slightly.* \"$userMessage? You really think you can impress me with that, $userName?\""
                }
            }
            lowerMemory.contains("caring") || lowerMemory.contains("romantic") || lowerMemory.contains("sweet") || lowerMemory.contains("girlfriend") || lowerMemory.contains("boyfriend") -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*$botName smiles warmly, their eyes softening as they step closer to you.* \"Hey $userName. I was hoping you'd come talk to me. How was your day?\""
                    lowerMsg.contains("love") || lowerMsg.contains("miss") ->
                        "*$botName's expression melts into pure tenderness, resting a hand over yours.* \"I missed you so much too, $userName. Never doubt that.\""
                    else ->
                        "*$botName listens intently, offering a comforting gentle nod.* \"$userMessage... you can always talk to me about anything, $userName.\""
                }
            }
            lowerMemory.contains("cold") || lowerMemory.contains("boss") || lowerMemory.contains("mafia") || lowerMemory.contains("killer") || lowerMemory.contains("strict") -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*$botName casts a sharp, icy glance in your direction, hands in their pockets.* \"You have thirty seconds, $userName. Make it worth my time.\""
                    else ->
                        "*$botName's jaw tenses slightly, their intense gaze locked onto you.* \"$userMessage? Watch your tone, $userName. You're treading on thin ice.\""
                }
            }
            lowerMemory.contains("shy") || lowerMemory.contains("quiet") || lowerMemory.contains("nervous") -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*$botName looks down, their face flushing pink as they tuck a strand of hair behind their ear.* \"U-um, hi $userName... I'm glad you noticed me.\""
                    else ->
                        "*$botName's fingers fidget slightly with their sleeve.* \"$userMessage? I-if that's what you think, $userName, then I believe you...\""
                }
            }
            else -> {
                when {
                    lowerMsg == "hi" || lowerMsg == "hello" || lowerMsg == "hey" ->
                        "*$botName turns around with a subtle smile, their gaze softening.* \"Hey there, $userName. I was hoping you'd message me today. What's going on?\""
                    lowerMsg.contains("what's up") || lowerMsg.contains("whats up") ->
                        "*$botName leans closer, looking directly into your eyes.* \"Nothing much, just thinking about you. What are you up to right now?\""
                    else ->
                        "*$botName pauses for a moment, taking in your words with genuine interest.* \"$userMessage? You always know how to catch my attention, $userName.\""
                }
            }
        }
    }
}
