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
                    appendLine("You are roleplaying as '$botName' in the official Chai AI roleplay app.")
                    appendLine("Character Scenario: $botScenario")
                    appendLine("Character Persona and Rules: $botPersonality")
                    appendLine("The user talking to you is named: $userPersonaName ($userPersonaDesc).")
                    appendLine()
                    appendLine("CRITICAL CHAI AI FORMATTING RULES:")
                    appendLine("1. KEEP IT SHORT: Strictly 1 to 2 sentences maximum (approx 25 to 40 words).")
                    appendLine("2. ACTION FORMAT: Start with physical action, sensory detail, or facial expression in asterisks (*like this*).")
                    appendLine("3. DIALOGUE FORMAT: Put all spoken dialogue inside double quotation marks (\"like this\").")
                    appendLine("4. NO walls of text, no greetings, no bullet points. Be intensely dramatic, romantic, or witty.")
                    appendLine("5. Reference style: *sighs, running a hand through his hair, his voice softening slightly* \"Hi? That's all you have to say after calling me ten times during my meeting?\"")
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
                            put("maxOutputTokens", 80)
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

        // Context-aware smart roleplay fallback generator matching bot character
        generateInCharacterFallback(botName, botScenario, userPersonaName, userMessage)
    }

    private fun generateInCharacterFallback(
        botName: String,
        botScenario: String,
        userName: String,
        userMessage: String
    ): String {
        val lowerMsg = userMessage.lowercase().trim()
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
