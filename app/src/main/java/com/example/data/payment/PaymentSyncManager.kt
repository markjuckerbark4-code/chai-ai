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

object PaymentSyncManager {
    private const val TAG = "PaymentSyncManager"
    private const val BUCKET_ID = "ChaiAiApp_7v9x2m"
    private const val BASE_KV_URL = "https://kvdb.io/$BUCKET_ID"
    private const val PREFS_NAME = "chai_payment_prefs"
    private const val KEY_LAST_STATUS = "key_last_payment_status"

    private fun cleanEmailKey(email: String): String {
        return email.lowercase().replace("@", "_at_").replace(".", "_dot_").replace("+", "_plus_")
    }

    private fun getIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
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
