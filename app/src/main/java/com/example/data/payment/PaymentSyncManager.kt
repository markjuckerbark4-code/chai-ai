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
            Log.d(TAG, "fetchUserStatus error: ${e.message}")
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
                val root = JSONObject(response)
                val dataObj = root.optJSONObject("data") ?: root
                dataObj.optJSONArray("orders") ?: JSONArray()
            } else {
                JSONArray()
            }

            // Prepend new order
            val updatedOrdersArray = JSONArray()
            updatedOrdersArray.put(JSONObject().apply {
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
            })

            for (i in 0 until existingOrdersArray.length()) {
                val item = existingOrdersArray.optJSONObject(i) ?: continue
                updatedOrdersArray.put(item)
            }

            val payload = JSONObject().apply {
                put("name", "chai_ai_orders")
                put("data", JSONObject().apply {
                    put("orders", updatedOrdersArray)
                })
            }
            httpPut(ORDERS_URL, payload.toString())

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
     * Checks if this user's payment has been approved by the Admin.
     */
    suspend fun checkUserApprovalStatus(
        context: Context,
        userEmail: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext false

        try {
            val status = fetchUserStatus(userEmail)
            if (status != null && status.isPremium) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LAST_STATUS, "approved")
                    .putBoolean("is_premium", true)
                    .apply()
                return@withContext true
            }
        } catch (e: Exception) {
            Log.d(TAG, "checkUserApprovalStatus error: ${e.message}")
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
            Log.e(TAG, "Error fetching payment settings: ${e.message}")
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
                return@withContext dataObj.optString("notice", null)
            }
        } catch (e: Exception) {
            Log.d(TAG, "fetchServerNotice error: ${e.message}")
        }
        null
    }

    private fun httpGet(urlString: String): String {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ChaiAi-Android")
            }
            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                    reader.readText()
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "httpGet error: $urlString, ${e.message}")
            ""
        } finally {
            conn?.disconnect()
        }
    }

    private fun httpPut(urlString: String, payload: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ChaiAi-Android")
            }
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(payload)
                writer.flush()
            }
            val code = conn.responseCode
            code in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "httpPut error: $urlString, ${e.message}")
            false
        } finally {
            conn?.disconnect()
        }
    }
}
