package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.UserAccountEntity
import com.example.ui.theme.LocalNovaTokens

@Composable
fun AuthDialog(
    currentUser: UserAccountEntity?,
    onRegister: (String, String, String, (Boolean, String) -> Unit) -> Unit,
    onLogin: (String, String, (Boolean, String) -> Unit) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalNovaTokens.current
    var isSignUpMode by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(tokens.surface)
                .border(1.dp, tokens.border, RoundedCornerShape(24.dp)),
            color = tokens.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentUser != null) "User Account" else if (isSignUpMode) "Create Account" else "Welcome Back",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "Local Mode • On-Device Room Database",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = tokens.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = tokens.textSecondary)
                    }
                }

                if (currentUser != null) {
                    // Logged In Profile View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(tokens.surfaceElevated)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(tokens.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = currentUser.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary
                        )

                        Text(
                            text = currentUser.email,
                            fontSize = 13.sp,
                            color = tokens.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                onLogout()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Out")
                        }
                    }
                } else {
                    // Auth Input Form
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = tokens.primary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.primary,
                                unfocusedBorderColor = tokens.border
                            )
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = tokens.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.border
                        )
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.primary) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = tokens.textSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.border
                        )
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.primary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.primary,
                                unfocusedBorderColor = tokens.border
                            )
                        )
                    }

                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = if (isError) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }

                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                if (nameInput.isBlank() || emailInput.isBlank() || passwordInput.isBlank()) {
                                    statusMessage = "Please complete all fields"
                                    isError = true
                                    return@Button
                                }
                                if (passwordInput != confirmPasswordInput) {
                                    statusMessage = "Passwords do not match"
                                    isError = true
                                    return@Button
                                }
                                onRegister(nameInput, emailInput, passwordInput) { success, msg ->
                                    statusMessage = msg
                                    isError = !success
                                    if (success) onDismiss()
                                }
                            } else {
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    statusMessage = "Please enter email and password"
                                    isError = true
                                    return@Button
                                }
                                onLogin(emailInput, passwordInput) { success, msg ->
                                    statusMessage = msg
                                    isError = !success
                                    if (success) onDismiss()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                    ) {
                        Text(
                            text = if (isSignUpMode) "Create Account" else "Log In",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isSignUpMode = !isSignUpMode
                                statusMessage = null
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have an account? Log In" else "Don't have an account? Sign Up",
                            fontSize = 13.sp,
                            color = tokens.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
