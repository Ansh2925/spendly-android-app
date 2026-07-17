cat << 'INNER_EOF' > app/src/main/java/com/example/viewmodel/ExpenseViewModel.kt
package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Expense
import com.example.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        searchQuery
    ) { expenses, query ->
        if (query.isBlank()) {
            expenses
        } else {
            expenses.filter {
                it.category.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.paymentMode.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    fun update(expense: Expense) = viewModelScope.launch {
        repository.update(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
    
    fun clearAllData() = viewModelScope.launch {
        repository.clearData()
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }
    
    fun forceSync() {
        viewModelScope.launch {
            try {
                repository.forceSync()
                _syncMessage.value = "Sync successful!"
            } catch (e: Exception) {
                _syncMessage.value = "Sync failed: 'expenses' table missing or network error."
            }
        }
    }
}
INNER_EOF
