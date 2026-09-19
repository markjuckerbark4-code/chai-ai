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
import java.nio.charset.StandardCharsets
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
    val screenshotNote: String = "",
    val status: String = "pending",
    val createdAt: String = ""
)

data class PaymentSettings(
    val bkashNumber: String = "01750721835",
    val nagadNumber: String = "01750721835",
    val cryptoAddress: String = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
    val cryptoNetwork: String = "Polygon (USDT)",
    val cryptoWallets: String = "Binance, Bybit, Bitget",
    val cryptoWeeklyPrice: String = "$2",
    val cryptoYearlyPrice: String = "$18",
    val bdtWeeklyPrice: String = "230",
    val bdtYearlyPrice: String = "2000",
    val notice: String = "নোটিশ: ১ ঘণ্টা বা ২ ঘণ্টা একটানা ব্যবহারের পর ৩০ মিনিট কুল ডাউন থাকবে।"
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
    
    // Dedicated Cloud Storage Endpoints for Chai AI
    private const val API_BASE = "https://api.restful-api.dev/objects"
    private const val USERS_OBJ_ID = "ff808181a09d98f701a0a95c8c431879"
    private const val ORDERS_OBJ_ID = "ff808181a09d98f701a0a95c8cfa187a"
    private const val SETTINGS_OBJ_ID = "ff808181a09d98f701a0a95c8d83187c"

    private const val USERS_URL = "$API_BASE/$USERS_OBJ_ID"
    private const val ORDERS_URL = "$API_BASE/$ORDERS_OBJ_ID"
    private const val SETTINGS_URL = "$API_BASE/$SETTINGS_OBJ_ID"

    private const val PREFS_NAME = "chai_payment_prefs"
    private const val KEY_LAST_STATUS = "key_last_payment_status"

    @Volatile
    private var rateLimitCooldownUntil: Long = 0L

    fun isRateLimited(): Boolean = System.currentTimeMillis() < rateLimitCooldownUntil

    private fun handleRateLimitOrError(code: Int, responseBody: String) {
        if (code == 405 || code == 429 ||
            responseBody.contains("limit", ignoreCase = true) ||
            responseBody.contains("too many", ignoreCase = true)
        ) {
            rateLimitCooldownUntil = System.currentTimeMillis() + (10 * 60 * 1000L) // 10 minutes cooldown
            Log.w(TAG, "Cloud API limit reached (code=$code). Cloud sync paused for 10 minutes; app running in local/cached mode.")
        }
    }

    fun hasPendingOrder(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_STATUS, "") == "pending"
    }

    fun cleanEmailKey(email: String): String {
        return email.lowercase().replace("@", "_at_").replace(".", "_dot_").replace("+", "_plus_")
    }

    private fun getIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /**
     * Registers and syncs user to the Cloud database
     * so that the Admin Panel immediately displays them in the Users list with their User ID.
     */
    suspend fun syncUserRegistration(
        userId: String,
        name: String,
        email: String,
        provider: String
    ) = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext
        try {
            val response = httpGet(USERS_URL)
            val usersArray = if (response.isNotBlank()) {
                val root = JSONObject(response)
                val dataObj = root.optJSONObject("data") ?: root
                dataObj.optJSONArray("users") ?: JSONArray()
            } else {
                JSONArray()
            }

            var found = false
            for (i in 0 until usersArray.length()) {
                val u = usersArray.optJSONObject(i) ?: continue
                val existingEmail = u.optString("email", "")
                val existingUid = u.optString("user_id", "")
                if (existingEmail.equals(email, ignoreCase = true) ||
                    (userId.isNotBlank() && existingUid.equals(userId, ignoreCase = true))
                ) {
                    u.put("name", name)
                    u.put("provider", provider)
                    if (userId.isNotBlank()) u.put("user_id", userId)
                    u.put("last_active", getIsoDate())
                    found = true
                    break
                }
            }

            if (!found) {
                val finalUid = if (userId.isNotBlank()) userId else "CHAI-${(100000..999999).random()}"
                val newUser = JSONObject().apply {
                    put("user_id", finalUid)
                    put("name", name)
                    put("email", email)
                    put("provider", provider)
                    put("tier", "none")
                    put("is_premium", false)
                    put("is_banned", false)
                    put("expiry_date", JSONObject.NULL)
                    put("created_at", getIsoDate())
                    put("last_active", getIsoDate())
                }
                usersArray.put(newUser)
            }

            val payload = JSONObject().apply {
                put("name", "chai_ai_users")
                put("data", JSONObject().apply {
                    put("users", usersArray)
                })
            }
            val ok = httpPut(USERS_URL, payload.toString())
            Log.d(TAG, "User registration cloud sync: email=$email, success=$ok")
        } catch (e: Exception) {
            Log.w(TAG, "User registration cloud sync warning: ${e.message}")
        }
    }

    /**
     * Checks if this user's account has been banned, approved, or assigned a tier (none/premium/ultra).
     */
    suspend fun fetchUserStatus(
        userEmail: String,
        context: Context? = null
    ): CloudUserStatus? = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext null
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        try {
            val response = httpGet(USERS_URL)
            if (response.isNotBlank()) {
                val root = JSONObject(response)
                val dataObj = root.optJSONObject("data") ?: root
                val usersArray = dataObj.optJSONArray("users") ?: JSONArray()

                for (i in 0 until usersArray.length()) {
                    val u = usersArray.optJSONObject(i) ?: continue
                    val email = u.optString("email", "")
                    if (email.equals(userEmail, ignoreCase = true)) {
                        val userId = u.optString("user_id", "")
                        val name = u.optString("name", "")
                        val isBanned = u.optBoolean("is_banned", false)
                        var tier = u.optString("tier", "none").lowercase()
                        var isPremium = u.optBoolean("is_premium", false) || tier == "premium" || tier == "ultra"
                        val expiryDate = if (u.has("expiry_date") && !u.isNull("expiry_date")) {
                            u.optString("expiry_date")
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

                        // Cache in SharedPreferences
                        prefs?.edit()
                            ?.putString("user_tier", tier)
                            ?.putBoolean("is_premium", isPremium)
                            ?.putBoolean("is_banned", isBanned)
                            ?.putString("expiry_date", expiryDate)
                            ?.apply()

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
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchUserStatus warning: ${e.message}")
        }

        // Fallback to local cached status if remote is unavailable or rate-limited
        if (prefs != null) {
            val isPrem = prefs.getBoolean("is_premium", false)
            val lastStatus = prefs.getString(KEY_LAST_STATUS, "")
            val isBanned = prefs.getBoolean("is_banned", false)
            val expiry = prefs.getString("expiry_date", null)
            val tier = prefs.getString("user_tier", if (isPrem || lastStatus == "approved") "ultra" else "none") ?: "none"
            if (isPrem || lastStatus == "approved" || isBanned) {
                return@withContext CloudUserStatus(
                    userId = prefs.getString("last_order_id", "") ?: "",
                    name = "",
                    email = userEmail,
                    tier = tier,
                    isPremium = isPrem || lastStatus == "approved",
                    isBanned = isBanned,
                    expiryDate = expiry
                )
            }
        }

        null
    }

    /**
     * Submits a new payment order from the Android app to the shared cloud store
     * so that the Admin Panel receives it immediately.
     */
    suspend fun submitOrder(
        context: Context,
        userName: String,
        userEmail: String,
        paymentMethod: String,
        senderNumber: String,
        trxId: String,
        plan: String,
        amount: String,
        screenshotNote: String = ""
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
                screenshotNote = screenshotNote,
                status = "pending",
                createdAt = getIsoDate()
            )

            // 1. Fetch current orders list
            val response = httpGet(ORDERS_URL)
            val existingOrdersArray = if (response.isNotBlank()) {
                try {
                    val root = JSONObject(response)
                    val dataObj = root.optJSONObject("data") ?: root
                    dataObj.optJSONArray("orders") ?: JSONArray()
                } catch (e: Exception) {
                    JSONArray()
                }
            } else {
                JSONArray()
            }

            // Prepend new order
            val orderJson = JSONObject().apply {
                put("order_id", newOrder.orderId)
                put("user_name", newOrder.userName)
                put("user_email", newOrder.userEmail)
                put("payment_method", newOrder.paymentMethod)
                put("sender_number", newOrder.senderNumber)
                put("trx_id", newOrder.trxId)
                put("plan", newOrder.plan)
                put("amount", newOrder.amount)
                put("screenshot_note", newOrder.screenshotNote)
                put("status", newOrder.status)
                put("created_at", newOrder.createdAt)
            }

            val updatedOrdersArray = JSONArray()
            updatedOrdersArray.put(orderJson)

            for (i in 0 until existingOrdersArray.length()) {
                val item = existingOrdersArray.optJSONObject(i) ?: continue
                val existingTrx = item.optString("trx_id")
                val existingId = item.optString("order_id")
                if (existingTrx.equals(newOrder.trxId, ignoreCase = true) || existingId == newOrder.orderId) {
                    continue
                }
                updatedOrdersArray.put(item)
            }

            val payload = JSONObject().apply {
                put("name", "chai_ai_orders")
                put("data", JSONObject().apply {
                    put("orders", updatedOrdersArray)
                })
            }
            val success = httpPut(ORDERS_URL, payload.toString())
            if (success) {
                Log.d(TAG, "Order submitted to cloud successfully: $orderId, orders count: ${updatedOrdersArray.length()}")
            } else {
                Log.w(TAG, "Failed to upload order to cloud ORDERS_URL, will attach to user profile: $orderId")
            }

            // 2. CRITICAL SYNC: Also attach the pending payment order directly to user in USERS_URL
            try {
                val usersResponse = httpGet(USERS_URL)
                val usersArray = if (usersResponse.isNotBlank()) {
                    val root = JSONObject(usersResponse)
                    val dataObj = root.optJSONObject("data") ?: root
                    dataObj.optJSONArray("users") ?: JSONArray()
                } else {
                    JSONArray()
                }

                var matched = false
                for (i in 0 until usersArray.length()) {
                    val u = usersArray.optJSONObject(i) ?: continue
                    if (u.optString("email").equals(userEmail, ignoreCase = true)) {
                        u.put("pending_order", orderJson)
                        u.put("last_trx_id", newOrder.trxId)
                        u.put("last_active", getIsoDate())
                        usersArray.put(i, u)
                        matched = true
                        break
                    }
                }

                if (!matched && userEmail.isNotBlank()) {
                    val newUserObj = JSONObject().apply {
                        put("user_id", "CHAI-" + (100000 + (Math.random() * 900000).toInt()))
                        put("name", userName.ifBlank { "User" })
                        put("email", userEmail)
                        put("provider", "Google")
                        put("tier", "none")
                        put("is_premium", false)
                        put("is_banned", false)
                        put("pending_order", orderJson)
                        put("last_trx_id", newOrder.trxId)
                        put("created_at", getIsoDate())
                        put("last_active", getIsoDate())
                    }
                    usersArray.put(0, newUserObj)
                }

                val usersPayload = JSONObject().apply {
                    put("name", "chai_ai_users")
                    put("data", JSONObject().apply {
                        put("users", usersArray)
                    })
                }
                val userOk = httpPut(USERS_URL, usersPayload.toString())
                Log.d(TAG, "Attached pending order to user profile in USERS_URL: success=$userOk")
            } catch (e: Exception) {
                Log.w(TAG, "User profile order attachment skipped: ${e.message}")
            }

            // Save locally in SharedPreferences immediately
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_STATUS, "pending")
                .putString("last_order_id", orderId)
                .putString("last_trx_id", newOrder.trxId)
                .putString("last_plan", newOrder.plan)
                .putString("last_amount", newOrder.amount)
                .apply()

            Result.success(newOrder)
        } catch (e: Exception) {
            Log.w(TAG, "Order submission remote sync skipped: ${e.message}")
            Result.success(
                PaymentOrder(
                    orderId = "ord-" + System.currentTimeMillis().toString().takeLast(6),
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
            )
        }
    }

    /**
     * Checks if this user's payment has been approved by the Admin.
     */
    suspend fun checkUserApprovalStatus(
        context: Context,
        userEmail: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext false

        try {
            val status = fetchUserStatus(userEmail, context)
            if (status != null && status.isPremium) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LAST_STATUS, "approved")
                    .putBoolean("is_premium", true)
                    .apply()
                return@withContext true
            }
        } catch (e: Exception) {
            Log.w(TAG, "checkUserApprovalStatus notice: ${e.message}")
        }
        false
    }

    suspend fun fetchPaymentSettings(context: Context? = null): PaymentSettings = withContext(Dispatchers.IO) {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val response = httpGet(SETTINGS_URL)
            if (response.isNotBlank()) {
                val root = JSONObject(response)
                val dataObj = root.optJSONObject("data") ?: root
                val settings = PaymentSettings(
                    bkashNumber = dataObj.optString("bkash_number", prefs?.getString("bkash_num", "01750721835") ?: "01750721835"),
                    nagadNumber = dataObj.optString("nagad_number", prefs?.getString("nagad_num", "01750721835") ?: "01750721835"),
                    cryptoAddress = dataObj.optString("crypto_address", prefs?.getString("crypto_addr", "0x742d35Cc6634C0532925a3b844Bc454e4438f44e") ?: "0x742d35Cc6634C0532925a3b844Bc454e4438f44e"),
                    cryptoNetwork = dataObj.optString("crypto_network", "Polygon (USDT)"),
                    cryptoWallets = dataObj.optString("crypto_wallets", "Binance, Bybit, Bitget"),
                    cryptoWeeklyPrice = dataObj.optString("crypto_weekly_price", "$2"),
                    cryptoYearlyPrice = dataObj.optString("crypto_yearly_price", "$18"),
                    bdtWeeklyPrice = dataObj.optString("bdt_weekly_price", "230"),
                    bdtYearlyPrice = dataObj.optString("bdt_yearly_price", "2000"),
                    notice = dataObj.optString("notice", "নোটিশ: ১ ঘণ্টা বা ২ ঘণ্টা একটানা ব্যবহারের পর ৩০ মিনিট কুল ডাউন থাকবে।")
                )
                // Save cache
                prefs?.edit()
                    ?.putString("bkash_num", settings.bkashNumber)
                    ?.putString("nagad_num", settings.nagadNumber)
                    ?.putString("crypto_addr", settings.cryptoAddress)
                    ?.putString("crypto_weekly", settings.cryptoWeeklyPrice)
                    ?.putString("crypto_yearly", settings.cryptoYearlyPrice)
                    ?.putString("bdt_weekly", settings.bdtWeeklyPrice)
                    ?.putString("bdt_yearly", settings.bdtYearlyPrice)
                    ?.putString("server_notice", settings.notice)
                    ?.apply()
                return@withContext settings
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notice: could not fetch remote payment settings (${e.message}), using default settings.")
        }

        PaymentSettings(
            bkashNumber = prefs?.getString("bkash_num", "01750721835") ?: "01750721835",
            nagadNumber = prefs?.getString("nagad_num", "01750721835") ?: "01750721835",
            cryptoAddress = prefs?.getString("crypto_addr", "0x742d35Cc6634C0532925a3b844Bc454e4438f44e") ?: "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
            cryptoNetwork = "Polygon (USDT)",
            cryptoWallets = "Binance, Bybit, Bitget",
            cryptoWeeklyPrice = prefs?.getString("crypto_weekly", "$2") ?: "$2",
            cryptoYearlyPrice = prefs?.getString("crypto_yearly", "$18") ?: "$18",
            bdtWeeklyPrice = prefs?.getString("bdt_weekly", "230") ?: "230",
            bdtYearlyPrice = prefs?.getString("bdt_yearly", "2000") ?: "2000",
            notice = prefs?.getString("server_notice", "নোটিশ: ১ ঘণ্টা বা ২ ঘণ্টা একটানা ব্যবহারের পর ৩০ মিনিট কুল ডাউন থাকবে।") ?: "নোটিশ: ১ ঘণ্টা বা ২ ঘণ্টা একটানা ব্যবহারের পর ৩০ মিনিট কুল ডাউন থাকবে।"
        )
    }

    suspend fun fetchServerNotice(): String? = withContext(Dispatchers.IO) {
        try {
            val response = httpGet(SETTINGS_URL)
            if (response.isNotBlank()) {
                val root = JSONObject(response)
                val dataObj = root.optJSONObject("data") ?: root
                return@withContext if (dataObj.has("notice")) dataObj.optString("notice") else null
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchServerNotice notice: ${e.message}")
        }
        null
    }

    private fun httpGet(urlString: String): String {
        if (isRateLimited()) {
            Log.d(TAG, "Skipping httpGet to $urlString (rate limit cooldown active)")
            return ""
        }
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ChaiAi-Android/1.0")
            }
            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                conn.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                    reader.readText()
                }
            } else {
                val err = conn.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
                handleRateLimitOrError(responseCode, err)
                Log.w(TAG, "httpGet response code=$responseCode for $urlString")
                ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "httpGet network error: $urlString, ${e.message}")
            ""
        } finally {
            conn?.disconnect()
        }
    }

    private fun httpPut(urlString: String, payload: String): Boolean {
        if (isRateLimited()) {
            Log.d(TAG, "Skipping httpPut to $urlString (rate limit cooldown active)")
            return false
        }
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ChaiAi-Android/1.0")
            }
            val bytes = payload.toByteArray(StandardCharsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)
            conn.outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }
            val code = conn.responseCode
            if (code in 200..299) {
                true
            } else {
                val err = conn.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
                handleRateLimitOrError(code, err)
                Log.w(TAG, "httpPut response code=$code for $urlString: $err")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "httpPut network exception: $urlString, ${e.message}")
            false
        } finally {
            conn?.disconnect()
        }
    }
}
