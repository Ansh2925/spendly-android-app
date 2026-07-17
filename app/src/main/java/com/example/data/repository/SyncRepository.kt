package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.entity.Expense
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant

@Serializable
data class ExpenseDto(
    val id: String,
    val user_id: String,
    val amount: Double,
    val category: String,
    val payment_mode: String,
    val notes: String,
    val expense_date: String,
    val created_at: JsonElement? = null
)

fun parseTimestamp(element: JsonElement?): Long {
    if (element == null) return System.currentTimeMillis()
    val primitive = element.jsonPrimitive
    if (primitive.isString) {
        return try {
            Instant.parse(primitive.content).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    return primitive.longOrNull ?: System.currentTimeMillis()
}

fun Expense.toDto(userId: String) = ExpenseDto(
    id = id,
    user_id = userId,
    amount = amount,
    category = category,
    payment_mode = paymentMode,
    notes = notes,
    expense_date = date,
    created_at = JsonPrimitive(timestamp)
)

fun ExpenseDto.toEntity(): Expense {
    val ts = parseTimestamp(created_at)
    return Expense(
        id = id,
        user_id = user_id,
        amount = amount,
        category = category,
        paymentMode = payment_mode,
        date = expense_date,
        time = com.example.utils.DateUtils.formatTime(ts),
        timestamp = ts,
        notes = notes,
        is_synced = true,
        is_deleted = false,
        updated_at = System.currentTimeMillis()
    )
}

@Singleton
class SyncRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val expenseDao: ExpenseDao
) {
    suspend fun syncExpenses() {
        val user = supabaseClient.auth.currentUserOrNull() ?: return
        val userId = user.id
        
        val unsynced = expenseDao.getUnsyncedExpenses()
        val toPush = unsynced.map { it.toDto(userId) }
        
        if (toPush.isNotEmpty()) {
            supabaseClient.postgrest["expenses"].upsert(toPush)
            val synced = unsynced.map { it.copy(is_synced = true, user_id = userId) }
            expenseDao.insertExpenses(synced)
        }
        
        val remoteExpenses = supabaseClient.postgrest["expenses"]
            .select()
            .decodeList<ExpenseDto>()
            
        val remoteSynced = remoteExpenses.map { it.toEntity() }
        expenseDao.insertExpenses(remoteSynced)
    }
    
    suspend fun silentSync() {
        try {
            syncExpenses()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
