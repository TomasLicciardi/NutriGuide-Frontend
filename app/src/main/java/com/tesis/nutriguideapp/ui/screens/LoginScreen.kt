package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.ui.theme.WhiteBackground
import com.tesis.nutriguideapp.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    context: Context,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val email by viewModel.email
    val password by viewModel.password
    val loading by viewModel.loading
    val passwordVisible by viewModel.passwordVisible

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "NUTRIGUIDE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Card para el formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )

                    // Botón login
                    Button(
                        onClick = {
                            viewModel.login(
                                context = context,
                                onSuccess = {
                                    coroutineScope.launch {
                                        try {
                                            android.util.Log.d("LoginScreen", "Login exitoso - navegando inmediatamente")
                                            
                                            // Navegar inmediatamente sin delay
                                            onLoginSuccess()
                                            android.util.Log.d("LoginScreen", "Navegación completada")
                                        } catch (e: Exception) {
                                            android.util.Log.e("LoginScreen", "Error al navegar: ${e.message}", e)
                                            snackbarHostState.showSnackbar("Error al navegar: ${e.message}")
                                        }
                                    }
                                },
                                onError = { error ->
                                    coroutineScope.launch {
                                        android.util.Log.e("LoginScreen", "Error en login: $error")
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)                    ) {
                        if (loading) {
                            // Usar un enfoque alternativo sin CircularProgressIndicator
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "...",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text("INICIAR SESIÓN")
                        }
                    }                    // Recuperar contraseña
                    TextButton(
                        onClick = {
                            // TODO: Navegar a recuperar contraseña
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("¿Olvidaste tu contraseña?")
                    }

                    // Registro
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("¿No tienes cuenta?")
                        TextButton(onClick = { onNavigateToRegister() }) {
                            Text("Regístrate")
                        }                    }
                }
            }
            
            // Botón oculto para prueba de errores (siempre visible para desarrollo)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val errorTypes = listOf("401", "404", "422", "timeout", "connection", "json", "http")
                            val errorType = errorTypes.random()
                            viewModel.testErrorHandling(errorType) { errorMessage ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("TEST ERROR ($errorType): $errorMessage")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(20.dp)
                        .align(Alignment.End)
                ) {
                    Text("T", fontSize = 10.sp)
                }
            }
        }
        
        // Snackbar para errores y confirmaciones
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
