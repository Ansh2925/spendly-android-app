import re

with open('app/src/main/java/com/example/data/repository/ExpenseRepository.kt', 'r') as f:
    content = f.read()

new_methods = """
    suspend fun getPagedExpenses(limit: Int, offset: Int, searchQuery: String): List<Expense> {
        return expenseDao.getPagedExpenses(limit, offset, searchQuery)
    }
    
    suspend fun getExpensesCount(searchQuery: String): Int {
        return expenseDao.getExpensesCount(searchQuery)
    }
"""

content = content.replace("suspend fun insert(expense: Expense) {", new_methods + "\n    suspend fun insert(expense: Expense) {")

with open('app/src/main/java/com/example/data/repository/ExpenseRepository.kt', 'w') as f:
    f.write(content)
