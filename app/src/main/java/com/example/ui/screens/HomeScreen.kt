package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Expense
import com.example.ui.components.ExpenseCard
import com.example.ui.theme.*
import com.example.utils.Constants
import com.example.utils.DateUtils
import com.example.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    authViewModel: com.example.viewmodel.AuthViewModel? = null,
    onNavigateToProfile: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    val startOfDay = remember { DateUtils.getStartOfDay() }
    val endOfDay = remember { DateUtils.getEndOfDay() }
    val startOfMonth = remember { DateUtils.getStartOfMonth() }
    
    val todaysExpenses = allExpenses.filter { it.timestamp in startOfDay..endOfDay }
    val monthlyExpenses = allExpenses.filter { it.timestamp in startOfMonth..endOfDay }
    
    val todaysTotal = todaysExpenses.sumOf { it.amount }
    val monthlyTotal = monthlyExpenses.sumOf { it.amount }
    
    val highestCategory = monthlyExpenses.groupBy { it.category }
        .mapValues { it.value.sumOf { exp -> exp.amount } }
        .maxByOrNull { it.value }
        
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()
    var showQuickAdd by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current



    val isImeVisible = WindowInsets.isImeVisible
    var wasImeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.silentSync()
    }



    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            wasImeVisible = true
        } else if (wasImeVisible) {
            wasImeVisible = false
            showQuickAdd = false
            focusManager.clearFocus()
        }
    }

    BackHandler(enabled = showQuickAdd) {
        showQuickAdd = false
        focusManager.clearFocus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .then(if (showQuickAdd) Modifier.blur(8.dp) else Modifier),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HomeHeader(onNavigateToProfile, authViewModel)
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TodaySummaryCard(
                            total = formatter.format(todaysTotal),
                            count = todaysExpenses.size,
                            todaysExpenses = todaysExpenses,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MonthlySummaryCard(
                            total = formatter.format(monthlyTotal),
                            totalNum = monthlyTotal,
                            highestCategoryName = highestCategory?.key ?: "None",
                            highestCategoryAmountNum = highestCategory?.value ?: 0.0,
                            highestCategoryAmount = formatter.format(highestCategory?.value ?: 0.0),
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }

                item {
                    if (!showQuickAdd) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showQuickAdd = true },
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                            border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF7E22CE))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "QUICK ADD EXPENSE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7E22CE),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
                
                if (todaysExpenses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Transactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }
                    
                    items(todaysExpenses.take(5)) { expense ->
                        ExpenseCard(expense = expense)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
                }
            }
            
            // Dim background overlay
            AnimatedVisibility(
                visible = showQuickAdd,
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
                            showQuickAdd = false
                        }
                )
            }
            
            // Quick Add Card attached to keyboard
            AnimatedVisibility(
                visible = showQuickAdd,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .imePadding()
                        .padding(16.dp)
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    QuickAddCard(
                        onAddExpense = { amount, category, paymentMode, description ->
                            viewModel.insert(
                                Expense(
                                    amount = amount,
                                    category = category,
                                    paymentMode = paymentMode,
                                    notes = description,
                                    date = DateUtils.getCurrentDate(),
                                    time = DateUtils.getCurrentTime(),
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Expense Added Successfully")
                            }
                            focusManager.clearFocus()
                            showQuickAdd = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(onNavigateToProfile: () -> Unit, authViewModel: com.example.viewmodel.AuthViewModel? = null) {
    val currentUser by (authViewModel?.currentUser ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    val name = currentUser?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "Guest User"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello $name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Slate400
            )
            Text(
                text = "Today is ${DateUtils.getFormattedDateForHeader()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = Slate900
            )
        }
        
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Indigo100)
                .border(2.dp, Color.White, CircleShape)
                .clickable(onClick = onNavigateToProfile),
            contentAlignment = Alignment.Center

        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color(0xFF7E22CE),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddCard(onAddExpense: (Double, String, String, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Constants.CATEGORIES.first().name) }
    var selectedPaymentMode by remember { mutableStateOf(Constants.PAYMENT_MODES.first()) }
    var isAmountFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
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
                    text = "QUICK ADD EXPENSE",
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
                            onAddExpense(amt, selectedCategory, selectedPaymentMode, description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("Add Expense", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TodaySummaryCard(total: String, count: Int, todaysExpenses: List<Expense>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TODAY'S TOTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = total,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
            
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val topExpenses = todaysExpenses.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.amount } }.toList().sortedByDescending { it.second }.take(3)
                
                topExpenses.forEach { (catName, amount) ->
                    val category = Constants.getCategoryByName(catName)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(16.dp))
                            Text(
                                text = category.name,
                                fontSize = 13.sp,
                                color = Slate900
                            )
                        }
                        Text(
                            text = "₹${amount.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                HorizontalDivider(color = Color(0xFFBFDBFE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$count Transactions",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(total: String, totalNum: Double, highestCategoryName: String, highestCategoryAmountNum: Double, highestCategoryAmount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald50),
        border = BorderStroke(1.dp, Emerald100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "THIS MONTH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald600,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = total,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald900
                )
            }
            
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = highestCategoryName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                        Text(
                            text = highestCategoryAmount,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    }
                    
                    val ratio = if (totalNum > 0) (highestCategoryAmountNum / totalNum).toFloat() else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFA7F3D0).copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(Emerald500)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFA7F3D0).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "AVERAGE/DAY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600,
                            letterSpacing = 0.5.sp
                        )
                        val cal = Calendar.getInstance()
                        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                        val avg = if (dayOfMonth > 0) totalNum / dayOfMonth else totalNum
                        val avgFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                        Text(
                            text = avgFormatter.format(avg),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                    }
                }
            }
        }
    }
}
