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


    val pagedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    var currentPage = 0
    val pageSize = 10
    var isLastPage = false
    val isLoading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            searchQuery.collect {
                resetAndLoad()
            }
        }
        viewModelScope.launch {
            allExpenses.collect {
                // If the underlying data changes (e.g. edit/delete), just reload current page logic
                resetAndLoad()
            }
        }
    }

    private fun resetAndLoad() {
        currentPage = 0
        isLastPage = false
        pagedExpenses.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading.value || isLastPage) return
        isLoading.value = true
        viewModelScope.launch {
            try {
                val newItems = repository.getPagedExpenses(pageSize, currentPage * pageSize, searchQuery.value)
                if (newItems.size < pageSize) {
                    isLastPage = true
                }
                val currentList = pagedExpenses.value.toMutableList()
                currentList.addAll(newItems)
                pagedExpenses.value = currentList
                currentPage++
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading.value = false
            }
        }
    }

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
                _syncMessage.value = "Sync failed: Network error or setup incomplete."
            }
        }
    }

    fun silentSync() {
        viewModelScope.launch {
            try {
                repository.forceSync()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
