import re

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

# Find where the @OptIn(ExperimentalMaterial3Api::class) function EditExpenseCard starts
# Replace it entirely

new_card = """
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
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\\\d*\\\\.?\\\\d*$"))) amount = it },
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
                            .androidx.compose.ui.focus.focusRequester(focusRequester)
                            .androidx.compose.ui.focus.onFocusChanged { isAmountFocused = it.isFocused },
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
"""

content = re.sub(r'@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun EditExpenseCard\(.*?\}', new_card, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
    f.write(content)
