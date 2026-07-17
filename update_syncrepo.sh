cat << 'INNER_EOF' > app/src/main/java/com/example/data/repository/SyncRepository.kt
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

@Serializable
data class ExpenseDto(
    val id: String,
    val user_id: String,
    val amount: Double,
    val category: String,
    val payment_mode: String,
    val notes: String,
    val expense_date: String,
    val created_at: Long
)

fun Expense.toDto(userId: String) = ExpenseDto(
    id = id,
    user_id = userId,
    amount = amount,
    category = category,
    payment_mode = paymentMode,
    notes = notes,
    expense_date = date,
    created_at = timestamp
)

fun ExpenseDto.toEntity() = Expense(
    id = id,
    user_id = user_id,
    amount = amount,
    category = category,
    paymentMode = payment_mode,
    date = expense_date,
    time = com.example.utils.DateUtils.formatTime(created_at),
    timestamp = created_at,
    notes = notes,
    is_synced = true,
    is_deleted = false,
    updated_at = System.currentTimeMillis()
)

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
INNER_EOF
