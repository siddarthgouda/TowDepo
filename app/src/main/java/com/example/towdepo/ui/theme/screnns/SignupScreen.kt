package com.example.towdepo.ui.theme.screnns

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.towdepo.viewmodels.AuthState
import com.example.towdepo.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Custom Name Text Field
        TextFieldHint(
            value = name,
            onValueChange = { name = it },
            hint = "Full Name",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Email Text Field
        TextFieldHint(
            value = email,
            onValueChange = { email = it },
            hint = "Email",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Password Text Field
        TextFieldHint(
            value = password,
            onValueChange = { password = it },
            hint = "Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Confirm Password Text Field
        TextFieldHint(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            hint = "Confirm Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            Text(
                text = "Passwords don't match",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthState.Error -> {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                RegisterButton(name, email, password, confirmPassword, viewModel)
            }
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    onRegisterSuccess()
                }
            }
            else -> {
                RegisterButton(name, email, password, confirmPassword, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account?")
        }
    }
}

@Composable
fun RegisterButton(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    viewModel: AuthViewModel
) {
    val isFormValid = name.isNotEmpty() &&
            email.isNotEmpty() &&
            password.length >= 6 &&
            password == confirmPassword

    Button(
        onClick = { viewModel.register(name, email, password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isFormValid
    ) {
        Text("Create Account")
    }
}