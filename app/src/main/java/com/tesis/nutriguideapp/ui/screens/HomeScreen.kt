package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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

    LaunchedEffect(key1 = Unit) {
        try {
            if (token == null) {
                try {
                    android.util.Log.d("HomeScreen", "Token nulo, navegando a login")
                    delay(100) // Pequeña pausa antes de navegar
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                } catch (e: Exception) {
                    android.util.Log.e(
                        "HomeScreen",
                        "Error al navegar a login por token nulo: ${e.message}",
                        e
                    )
                }
            } else {
                try {
                    android.util.Log.d("HomeScreen", "Token válido, obteniendo perfil de usuario")
                    viewModel.getUserProfile(context)
                } catch (e: Exception) {
                    android.util.Log.e(
                        "HomeScreen",
                        "Error al obtener perfil de usuario: ${e.message}",
                        e
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Error general en LaunchedEffect: ${e.message}", e)
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
            // Título
            Text(
                text = "NUTRIGUIDE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Bienvenida personalizada
            Text(
                text = if (username.isNotEmpty()) "¡Hola, $username!" else "¡Bienvenido!",
                fontSize = 20.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Tarjeta Analizar Producto
            HomeCard(
                title = "Analizar Producto",
                description = "Sube una foto de la etiqueta de un producto para analizarlo",
                icon = Icons.Default.Camera,
                backgroundColor = GreenPrimary,
                onClick = {
                    try {
                        android.util.Log.d("HomeScreen", "Navegando a pantalla de upload")
                        navController.navigate("upload")
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error al navegar a upload: ${e.message}", e)
                    }
                }
            )

            // Captura directa con cámara
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        try {
                            android.util.Log.d("HomeScreen", "Navegando a pantalla de cámara")
                            navController.navigate("camera")
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error al navegar a cámara: ${e.message}", e)
                        }
                    },
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Capturar con cámara"
                    )
                }
                Text(
                    text = "Capturar directamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                )
            }

            // Tarjeta Historial
            HomeCard(
                title = "Ver Historial",
                description = "Revisa los productos que has analizado anteriormente",
                icon = Icons.Default.History,
                backgroundColor = YellowSecondary,
                onClick = {
                    try {
                        android.util.Log.d("HomeScreen", "Navegando a pantalla de historial")
                        val restrictionsList: List<String> = userRestrictions ?: emptyList()
                        val restrictionsParam = restrictionsList.joinToString(",")
                        navController.navigate("history/$restrictionsParam")
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "HomeScreen",
                            "Error al navegar a historial: ${e.message}",
                            e
                        )
                    }
                }            )

            // Tarjeta Mi Perfil
            HomeCard(
                title = "Mi Perfil",
                description = "Ver y editar información personal",
                icon = Icons.Default.Person,
                backgroundColor = androidx.compose.ui.graphics.Color(0xFF9C27B0), // Purple
                onClick = {
                    try {
                        android.util.Log.d("HomeScreen", "Navegando a pantalla de perfil")
                        navController.navigate("profile")
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "HomeScreen",
                            "Error al navegar a perfil: ${e.message}",
                            e
                        )
                    }
                }            )

            // Espacio para empujar el botón de logout al fondo
            Spacer(modifier = Modifier.weight(1f))

            // Botón de cerrar sesión
            Button(
                onClick = {
                    try {
                        android.util.Log.d("HomeScreen", "Iniciando logout")
                        viewModel.logout(context) {
                            try {
                                android.util.Log.d(
                                    "HomeScreen",
                                    "Logout exitoso, navegando a login"
                                )
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "HomeScreen",
                                    "Error al navegar después del logout: ${e.message}",
                                    e
                                )
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "HomeScreen",
                            "Error al realizar logout: ${e.message}",
                            e
                        )
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
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = GreenPrimary
            )
        }
    }
}

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
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
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
