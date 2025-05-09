package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tesis.nutriguideapp.api.AuthService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.AuthRequest
import com.tesis.nutriguideapp.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(context: Context, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }  // 🔁 Antes era mail
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val service = RetrofitInstance.retrofit.create(AuthService::class.java)
                        val response = service.login(AuthRequest(email, password)) // 🔁
                        TokenManager(context).saveToken(response.accessToken)
                        onLoginSuccess()
                    } catch (e: Exception) {
                        errorMessage = "Error al iniciar sesión"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}