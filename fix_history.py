import re

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

# Add pagination logic and Edit
# We will use LazyColumn's itemsIndexed to detect when we reach the end and call viewModel.loadNextPage()
# We will add an EditExpenseCard

new_imports = """
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.utils.Constants
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
"""

# add imports
content = content.replace("import com.example.viewmodel.ExpenseViewModel", "import com.example.viewmodel.ExpenseViewModel\n" + new_imports)

# Replace variables
old_vars = """    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<com.example.data.entity.Expense?>(null) }"""

new_vars = """    val pagedExpenses by viewModel.pagedExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<com.example.data.entity.Expense?>(null) }
    
    var expenseToEdit by remember { mutableStateOf<com.example.data.entity.Expense?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
"""

content = content.replace(old_vars, new_vars)

# Replace Box content
content = content.replace("if (filteredExpenses.isEmpty()) {", "if (pagedExpenses.isEmpty()) {")
content = content.replace("items(filteredExpenses, key = { it.id }) { expense ->", """itemsIndexed(pagedExpenses, key = { _, it -> it.id }) { index, expense ->
                        if (index >= pagedExpenses.size - 1 && !viewModel.isLastPage && !viewModel.isLoading.value) {
                            LaunchedEffect(index) {
                                viewModel.loadNextPage()
                            }
                        }""")
                        
content = content.replace("ExpenseCard(expense = expense)", """ExpenseCard(expense = expense, modifier = Modifier.clickable { expenseToEdit = expense })""")

edit_card_code = """
        if (showDeleteConfirm && expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Expense") },
                text = { Text("Are you sure you want to delete this expense?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(expenseToDelete!!)
                        showDeleteConfirm = false
                        expenseToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        AnimatedVisibility(
            visible = expenseToEdit != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                        expenseToEdit = null
                    }
            )
        }

        AnimatedVisibility(
            visible = expenseToEdit != null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (expenseToEdit != null) {
                Box(
                    modifier = Modifier
                        .imePadding()
                        .padding(16.dp)
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    EditExpenseCard(
                        expense = expenseToEdit!!,
                        onUpdateExpense = { amount, category, paymentMode, description ->
                            viewModel.update(
                                expenseToEdit!!.copy(
                                    amount = amount,
                                    category = category,
                                    paymentMode = paymentMode,
                                    notes = description,
                                    updated_at = System.currentTimeMillis()
                                )
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Expense Updated Successfully")
                            }
                            focusManager.clearFocus()
                            expenseToEdit = null
                        }
                    )
                }
            }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
"""

content = content.replace("""if (showDeleteConfirm && expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Expense") },
                text = { Text("Are you sure you want to delete this expense?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(expenseToDelete!!)
                        showDeleteConfirm = false
                        expenseToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }""", edit_card_code)
        
content = content.replace("} { paddingValues ->", """} { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {""")
content = content.replace("    }\n}", "    }\n}\n}")

edit_component = """

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseCard(expense: com.example.data.entity.Expense, onUpdateExpense: (Double, String, String, String) -> Unit) {
    var amount by remember { mutableStateOf(expense.amount.toString().replace(".0", "")) }
    var description by remember { mutableStateOf(expense.notes) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var selectedPaymentMode by remember { mutableStateOf(expense.paymentMode) }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { /* Consume clicks to prevent dismiss */ },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
        border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EDIT EXPENSE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7E22CE),
                    letterSpacing = 1.sp
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\\\d*\\\\.?\\\\d*$"))) amount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Slate900
                        ),
                        placeholder = { 
                            Text("0", 
                                fontSize = 32.sp, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Slate300,
                                modifier = Modifier.fillMaxWidth()
                            ) 
                        },
                        prefix = { 
                            Text("₹", 
                                fontSize = 32.sp, 
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                        ),
                        singleLine = true
                    )
                    Divider(modifier = Modifier.fillMaxWidth(0.5f), color = Color(0xFFD8B4FE), thickness = 2.dp)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("What was this for?", color = Slate400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE9D5FF),
                        focusedBorderColor = Color(0xFFC084FC),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    ),
                    singleLine = true
                )

                Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate600, modifier = Modifier.padding(start = 4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Constants.CATEGORIES) { category ->
                        val isSelected = selectedCategory == category.name
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) Color(0xFF9333EA) else Color.White,
                            border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE9D5FF)) else null,
                            modifier = Modifier.clickable { selectedCategory = category.name }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category.icon)
                                Text(
                                    text = category.name,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Slate600
                                )
                            }
                        }
                    }
                }

                Text("Payment Mode", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate600, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Constants.PAYMENT_MODES) { mode ->
                        val isSelected = selectedPaymentMode == mode
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) Color(0xFF9333EA) else Color.White,
                            border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE9D5FF)) else null,
                            modifier = Modifier.clickable { selectedPaymentMode = mode }
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) Color.White else Slate600
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val amountVal = amount.toDoubleOrNull()
                        if (amountVal != null && amountVal > 0 && description.isNotBlank()) {
                            onUpdateExpense(amountVal, selectedCategory, selectedPaymentMode, description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                    enabled = amount.toDoubleOrNull()?.let { it > 0 } == true && description.isNotBlank()
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
"""

content = content + edit_component

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
    f.write(content)
