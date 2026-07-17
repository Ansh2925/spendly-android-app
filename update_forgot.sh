cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/auth/ForgotPasswordScreen.kt
package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AuthState
import com.example.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            delay(5000)
            viewModel.resetState()
        }
    }

    Scaffold(containerColor = Color.White) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color(0xFF99F6E4), // Pastel Teal
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.resetPassword(email) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCFBF1)), // Pastel teal
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Send Reset Link", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.8f), color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF14B8A6)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF99F6E4)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back to Login")
                }
            }

            AnimatedVisibility(
                visible = authState is AuthState.Error,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFECDD3)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (authState as? AuthState.Error)?.message ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF881337),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
INNER_EOF
