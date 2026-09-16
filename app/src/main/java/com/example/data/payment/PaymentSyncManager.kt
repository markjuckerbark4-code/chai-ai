package com.example.data.payment

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class PaymentOrder(
    val orderId: String,
    val userName: String,
    val userEmail: String,
    val paymentMethod: String,
    val senderNumber: String,
    val trxId: String,
    val plan: String,
    val amount: String,
    val status: String = "pending",
    val createdAt: String = ""
)

data class CloudUserStatus(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val tier: String = "none", // "none", "premium", "ultra"
    val isPremium: Boolean = false,
    val isBanned: Boolean = false,
    val expiryDate: String? = null,
    val notice: String? = null
)

object PaymentSyncManager {
    private const val TAG = "PaymentSyncManager"
    private const val BUCKET_ID = "ChaiAiApp_7v9x2m"
    private const val BASE_KV_URL = "https://kvdb.io/$BUCKET_ID"
    private const val PREFS_NAME = "chai_payment_prefs"
    private const val KEY_LAST_STATUS = "key_last_payment_status"

    fun cleanEmailKey(email: String): String {
        return email.lowercase().replace("@", "_at_").replace(".", "_dot_").replace("+", "_plus_")
    }

    private fun getIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /**
     * Registers and syncs user to the Cloud KV database (/users and /user_{email})
     * so that the Admin Panel immediately displays them in the Users list.
     */
    suspend fun syncUserRegistration(
        userId: String,
        name: String,
        email: String,
        provider: String
    ) = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext
        try {
            val userKey = "user_" + cleanEmailKey(email)
            // Check existing user data if any
            val existingData = getKvData("$BASE_KV_URL/$userKey")
            var currentTier = "none"
            var isBanned = false
            var isPremium = false
            var expiryDate: String? = null

            if (existingData.isNotBlank()) {
                val json = JSONObject(existingData)
                currentTier = json.optString("tier", "none")
                isBanned = json.optBoolean("is_banned", false)
                isPremium = json.optBoolean("is_premium", false)
                if (json.has("expiry_date") && !json.isNull("expiry_date")) {
                    expiryDate = json.optString("expiry_date")
                }
            }

            // 1. Post to user-specific key
            val userObj = JSONObject().apply {
                put("user_id", userId)
                put("name", name)
                put("email", email)
                put("provider", provider)
                put("tier", currentTier)
                put("is_premium", isPremium)
                put("is_banned", isBanned)
                if (expiryDate != null) put("expiry_date", expiryDate)
                put("last_active", getIsoDate())
            }
            postKvData("$BASE_KV_URL/$userKey", userObj.toString())

            // 2. Add or update in /users array
            val usersJson = getKvData("$BASE_KV_URL/users")
            val usersArray = if (usersJson.isNotBlank() && usersJson.trim().startsWith("[")) {
                JSONArray(usersJson)
            } else {
                JSONArray()
            }

            var found = false
            for (i in 0 until usersArray.length()) {
                val item = usersArray.optJSONObject(i) ?: continue
                if (item.optString("email").equals(email, ignoreCase = true) ||
                    item.optString("user_id").equals(userId, ignoreCase = true)) {
                    item.put("name", name)
                    item.put("provider", provider)
                    item.put("user_id", userId)
                    item.put("last_active", getIsoDate())
                    found = true
                    break
                }
            }

            if (!found) {
                val newUserJson = JSONObject().apply {
                    put("user_id", userId)
                    put("name", name)
                    put("email", email)
                    put("provider", provider)
                    put("tier", currentTier)
                    put("is_banned", isBanned)
                    put("is_premium", isPremium)
                    if (expiryDate != null) put("expiry_date", expiryDate)
                    put("created_at", getIsoDate())
                    put("last_active", getIsoDate())
                }
                usersArray.put(newUserJson)
            }

            postKvData("$BASE_KV_URL/users", usersArray.toString())
            Log.d(TAG, "User registered in cloud: $userId - $email")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user to cloud", e)
        }
    }

    /**
     * Checks if this user's account has been banned, approved, or assigned a tier (none/premium/ultra).
     */
    suspend fun fetchUserStatus(
        userEmail: String
    ): CloudUserStatus? = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext null

        try {
            val userKey = "user_" + cleanEmailKey(userEmail)
            val response = getKvData("$BASE_KV_URL/$userKey")
            if (response.isNotBlank()) {
                val json = JSONObject(response)
                val status = json.optString("status", "")
                var isPremium = json.optBoolean("is_premium", false) || status.equals("approved", ignoreCase = true)
                var tier = json.optString("tier", if (isPremium) "premium" else "none")
                val isBanned = json.optBoolean("is_banned", false)
                val userId = json.optString("user_id", "")
                val name = json.optString("name", "")
                val expiryDate = if (json.has("expiry_date") && !json.isNull("expiry_date")) {
                    json.optString("expiry_date")
                } else null

                // Check if expiry date has passed
                if (expiryDate != null && isPremium) {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        val expiryTime = sdf.parse(expiryDate)?.time ?: Long.MAX_VALUE
                        if (System.currentTimeMillis() > expiryTime) {
                            // Expired!
                            isPremium = false
                            tier = "none"
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Expiry date parse error: $e")
                    }
                }

                return@withContext CloudUserStatus(
                    userId = userId,
                    name = name,
                    email = userEmail,
                    tier = tier,
                    isPremium = isPremium,
                    isBanned = isBanned,
                    expiryDate = expiryDate
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Status check notice: ${e.message}")
        }
        null
    }

    /**
     * Submits a new payment order from the Android app to the shared cloud store
     * so that the Vercel Admin Panel receives it immediately.
     */
    suspend fun submitOrder(
        context: Context,
        userName: String,
        userEmail: String,
        paymentMethod: String,
        senderNumber: String,
        trxId: String,
        plan: String,
        amount: String
    ): Result<PaymentOrder> = withContext(Dispatchers.IO) {
        try {
            val orderId = "ord-" + System.currentTimeMillis().toString().takeLast(6)
            val newOrder = PaymentOrder(
                orderId = orderId,
                userName = userName,
                userEmail = userEmail,
                paymentMethod = paymentMethod,
                senderNumber = senderNumber,
                trxId = trxId.trim().uppercase(),
                plan = plan,
                amount = amount,
                status = "pending",
                createdAt = getIsoDate()
            )

            // 1. Fetch current orders list
            val existingOrders = fetchAllOrdersInternal().toMutableList()
            // Add new order at the top
            existingOrders.add(0, newOrder)

            // Save updated orders to KV store
            val ordersArray = JSONArray()
            existingOrders.forEach { order ->
                ordersArray.put(JSONObject().apply {
                    put("order_id", order.orderId)
                    put("user_name", order.userName)
                    put("user_email", order.userEmail)
                    put("payment_method", order.paymentMethod)
                    put("sender_number", order.senderNumber)
                    put("trx_id", order.trxId)
                    put("plan", order.plan)
                    put("amount", order.amount)
                    put("status", order.status)
                    put("created_at", order.createdAt)
                })
            }

            postKvData("$BASE_KV_URL/orders", ordersArray.toString())

            // 2. Set user specific status
            val userKey = "user_" + cleanEmailKey(userEmail)
            val userStatusJson = JSONObject().apply {
                put("status", "pending")
                put("order_id", orderId)
                put("user_email", userEmail)
                put("trx_id", trxId)
                put("plan", plan)
                put("amount", amount)
                put("is_premium", false)
            }
            postKvData("$BASE_KV_URL/$userKey", userStatusJson.toString())

            // Save locally in SharedPreferences
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_STATUS, "pending")
                .putString("last_order_id", orderId)
                .apply()

            Log.d(TAG, "Order submitted successfully: $orderId")
            Result.success(newOrder)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting order to cloud", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if this user's payment has been approved by the Admin in the Vercel panel.
     */
    suspend fun checkUserApprovalStatus(
        context: Context,
        userEmail: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext false

        try {
            val userKey = "user_" + cleanEmailKey(userEmail)
            val response = getKvData("$BASE_KV_URL/$userKey")
            if (response.isNotBlank()) {
                val json = JSONObject(response)
                val status = json.optString("status", "")
                val isPremium = json.optBoolean("is_premium", false) || status.equals("approved", ignoreCase = true)
                if (isPremium) {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString(KEY_LAST_STATUS, "approved")
                        .putBoolean("is_premium", true)
                        .apply()
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Status check notice: ${e.message}")
        }
        false
    }

    private fun fetchAllOrdersInternal(): List<PaymentOrder> {
        val list = mutableListOf<PaymentOrder>()
        try {
            val response = getKvData("$BASE_KV_URL/orders")
            if (response.isNotBlank()) {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        PaymentOrder(
                            orderId = obj.optString("order_id"),
                            userName = obj.optString("user_name"),
                            userEmail = obj.optString("user_email"),
                            paymentMethod = obj.optString("payment_method"),
                            senderNumber = obj.optString("sender_number"),
                            trxId = obj.optString("trx_id"),
                            plan = obj.optString("plan"),
                            amount = obj.optString("amount"),
                            status = obj.optString("status", "pending"),
                            createdAt = obj.optString("created_at")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all orders", e)
        }
        return list
    }

    private fun getKvData(urlString: String): String {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Accept", "application/json")
            }
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                    reader.readText()
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        } finally {
            conn?.disconnect()
        }
    }

    private fun postKvData(urlString: String, payload: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(payload)
                writer.flush()
            }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }
}
