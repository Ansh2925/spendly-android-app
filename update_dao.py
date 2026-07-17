import re

with open('app/src/main/java/com/example/data/dao/ExpenseDao.kt', 'r') as f:
    content = f.read()

new_queries = """
    @Query("SELECT * FROM expenses WHERE is_deleted = 0 AND (category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%' OR paymentMode LIKE '%' || :searchQuery || '%') ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedExpenses(limit: Int, offset: Int, searchQuery: String): List<Expense>
    
    @Query("SELECT COUNT(*) FROM expenses WHERE is_deleted = 0 AND (category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%' OR paymentMode LIKE '%' || :searchQuery || '%')")
    suspend fun getExpensesCount(searchQuery: String): Int
"""

content = content.replace("interface ExpenseDao {", "interface ExpenseDao {" + new_queries)

with open('app/src/main/java/com/example/data/dao/ExpenseDao.kt', 'w') as f:
    f.write(content)
