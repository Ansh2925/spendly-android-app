package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE is_deleted = 0 AND (category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%' OR paymentMode LIKE '%' || :searchQuery || '%') ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedExpenses(limit: Int, offset: Int, searchQuery: String): List<Expense>
    
    @Query("SELECT COUNT(*) FROM expenses WHERE is_deleted = 0 AND (category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%' OR paymentMode LIKE '%' || :searchQuery || '%')")
    suspend fun getExpensesCount(searchQuery: String): Int

    @Query("SELECT * FROM expenses WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND is_deleted = 0 ORDER BY timestamp DESC")
    fun getExpensesForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
    
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
    
    @Query("SELECT * FROM expenses WHERE is_synced = 0")
    suspend fun getUnsyncedExpenses(): List<Expense>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)
}
