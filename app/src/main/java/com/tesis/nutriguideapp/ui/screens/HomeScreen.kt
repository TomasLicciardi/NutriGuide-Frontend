package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tesis.nutriguideapp.ui.theme.BlueAccent
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.ui.theme.WhiteBackground
import com.tesis.nutriguideapp.ui.theme.YellowSecondary
import com.tesis.nutriguideapp.utils.TokenManager
import com.tesis.nutriguideapp.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = backgroundColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    context: Context,
    viewModel: HomeViewModel = viewModel()
) {
    val tokenManager = TokenManager(context)
    val token = tokenManager.getToken()
    val username by viewModel.username
    val loading by viewModel.loading
    val userRestrictions by viewModel.userRestrictions

    LaunchedEffect(Unit) {
        if (token == null) {
            delay(100)
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            viewModel.getUserProfile(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NUTRIGUIDE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = if (username.isNotEmpty()) "¡Hola, $username!" else "¡Bienvenido!",
                fontSize = 20.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            var showAnalyzisOptions by remember { mutableStateOf(false) }

            HomeCard(
                title = "Analizar Producto",
                description = "Escanea etiquetas de productos para verificar compatibilidad",
                icon = Icons.Default.Camera,
                backgroundColor = GreenPrimary,
                onClick = { showAnalyzisOptions = true }
            )

            if (showAnalyzisOptions) {
                AlertDialog(
                    onDismissRequest = { showAnalyzisOptions = false },
                    title = { Text("Analizar Producto", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Cómo quieres subir la imagen del producto?") },
                    confirmButton = {
                        Column {
                            Button(
                                onClick = {
                                    showAnalyzisOptions = false
                                    navController.navigate("upload")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Galería",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Seleccionar de Galería")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    showAnalyzisOptions = false
                                    navController.navigate("camera")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Cámara",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Tomar Foto")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { showAnalyzisOptions = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancelar")
                            }
                        }
                    },
                    dismissButton = { }
                )
            }

            HomeCard(
                title = "Ver Historial",
                description = "Revisa los productos que has analizado anteriormente",
                icon = Icons.Default.History,
                backgroundColor = YellowSecondary,
                onClick = {
                    try {
                        val restrictionsList = userRestrictions ?: emptyList()
                        val restrictionsParam = if (restrictionsList.isEmpty()) {
                            "none"
                        } else {
                            restrictionsList.joinToString(",")
                        }
                        android.util.Log.d("HomeScreen", "Navegando a history con parámetro: $restrictionsParam")
                        navController.navigate("history/$restrictionsParam")
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error al navegar al historial: ${e.message}", e)
                        try {
                            navController.navigate("history/none")
                        } catch (fallbackError: Exception) {
                            android.util.Log.e("HomeScreen", "Error en navegación de fallback: ${fallbackError.message}", fallbackError)
                        }
                    }
                }
            )

            HomeCard(
                title = "Mi Perfil",
                description = "Ver y editar información personal",
                icon = Icons.Default.Person,
                backgroundColor = Color(0xFF9C27B0),
                onClick = {
                    navController.navigate("profile")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout(context) {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Cerrar Sesión",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("CERRAR SESIÓN")
            }
        }

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenPrimary
                )
            }
        }
    }
}
