cat << 'INNER_EOF' > app/src/main/java/com/example/data/repository/ExpenseRepository.kt
package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val syncRepository: SyncRepository
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesForDateRange(start: Long, end: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesForDateRange(start, end)
    }

    suspend fun insert(expense: Expense) {
        val newExpense = expense.copy(is_synced = false, updated_at = System.currentTimeMillis())
        expenseDao.insertExpense(newExpense)
        triggerSync()
    }

    suspend fun update(expense: Expense) {
        val updatedExpense = expense.copy(is_synced = false, updated_at = System.currentTimeMillis())
        expenseDao.updateExpense(updatedExpense)
        triggerSync()
    }

    suspend fun delete(expense: Expense) {
        val deletedExpense = expense.copy(is_deleted = true, is_synced = false, updated_at = System.currentTimeMillis())
        expenseDao.updateExpense(deletedExpense) // Soft delete
        triggerSync()
    }
    
    suspend fun clearData() {
        expenseDao.deleteAllExpenses()
    }
    
    suspend fun forceSync() {
        syncRepository.syncExpenses()
    }
    
    private fun triggerSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncRepository.silentSync()
        }
    }
}
INNER_EOF
