import re

with open('app/src/main/java/com/example/viewmodel/ExpenseViewModel.kt', 'r') as f:
    content = f.read()

# Remove filteredExpenses
content = re.sub(r'    val filteredExpenses: StateFlow<List<Expense>> = combine\(.*?\)\.stateIn\(viewModelScope, SharingStarted\.Lazily, emptyList\(\)\)', '', content, flags=re.DOTALL)

pagination_logic = """
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
"""

content = content.replace("    private val _syncMessage = MutableStateFlow<String?>(null)", pagination_logic + "\n    private val _syncMessage = MutableStateFlow<String?>(null)")

with open('app/src/main/java/com/example/viewmodel/ExpenseViewModel.kt', 'w') as f:
    f.write(content)
