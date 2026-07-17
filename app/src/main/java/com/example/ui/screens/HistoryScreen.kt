
package com.example.ui.screens
import androidx.compose.foundation.border
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ExpenseCard
import com.example.ui.theme.*
import com.example.viewmodel.ExpenseViewModel

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: ExpenseViewModel) {
    val pagedExpenses by viewModel.pagedExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<com.example.data.entity.Expense?>(null) }
    
    var expenseToEdit by remember { mutableStateOf<com.example.data.entity.Expense?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense History", fontWeight = FontWeight.Bold, color = Slate900) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundGray
                )
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by category, mode...", color = Slate400) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Slate200,
                    focusedBorderColor = Emerald500,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedTextColor = Slate900,
                    unfocusedTextColor = Slate900
                )
            )
            
            if (pagedExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No expenses found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate400
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(pagedExpenses, key = { _, it -> it.id }) { index, expense ->
                        if (index >= pagedExpenses.size - 1 && !viewModel.isLastPage && !viewModel.isLoading.value) {
                            LaunchedEffect(index) {
                                viewModel.loadNextPage()
                            }
                        }
                        SwipeToDismissBox(
                            expense = expense,
                            onDelete = {
                                expenseToDelete = expense
                                showDeleteConfirm = true
                            }
                        ) {
                            ExpenseCard(expense = expense, modifier = Modifier.clickable { expenseToEdit = expense })
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        
        
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

    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissBox(
    expense: com.example.data.entity.Expense,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't dismiss immediately, wait for confirm
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        content()
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseCard(expense: com.example.data.entity.Expense, onUpdateExpense: (Double, String, String, String) -> Unit) {
    var amount by remember { mutableStateOf(if (expense.amount > 0) expense.amount.toString().replace(".0", "") else "") }
    var description by remember { mutableStateOf(expense.notes) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var selectedPaymentMode by remember { mutableStateOf(expense.paymentMode) }
    var isAmountFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        // focusRequester.requestFocus()
    }
    
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                        placeholder = { 
                            if (!isAmountFocused && amount.isEmpty()) {
                                Text("₹0", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Slate400, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) 
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 36.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Emerald600,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isAmountFocused = it.isFocused },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Emerald600,
                            unfocusedTextColor = Emerald600,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Emerald600,
                            focusedContainerColor = Color.White.copy(alpha = 0.5f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Description (Optional)", color = Slate400, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedBorderColor = Color(0xFF9333EA).copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(Constants.CATEGORIES) { category ->
                        val isSelected = selectedCategory == category.name
                        Column(
                            modifier = Modifier
                                .clickable { selectedCategory = category.name }
                                .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(16.dp))
                                .border(if (isSelected) 2.dp else 0.dp, if (isSelected) Color(0xFF9333EA) else Color.Transparent, RoundedCornerShape(16.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (isSelected) Color(0xFFF3E8FF) else category.color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = category.icon, contentDescription = category.name, tint = if (isSelected) Color(0xFF7E22CE) else category.color, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = category.name,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF7E22CE) else Slate900
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Constants.PAYMENT_MODES) { mode ->
                            val isSelected = selectedPaymentMode == mode
                            Box(
                                modifier = Modifier
                                    .clickable { selectedPaymentMode = mode }
                                    .background(if (isSelected) Color(0xFF9333EA) else Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                                    .border(1.dp, if (isSelected) Color(0xFF9333EA) else Color.White, RoundedCornerShape(50))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate600
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            onUpdateExpense(amt, selectedCategory, selectedPaymentMode, description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
