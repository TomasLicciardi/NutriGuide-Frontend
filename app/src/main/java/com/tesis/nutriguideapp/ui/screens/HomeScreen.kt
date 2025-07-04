package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tesis.nutriguideapp.R
import com.tesis.nutriguideapp.ui.theme.BlueAccent
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.ui.theme.WhiteBackground
import com.tesis.nutriguideapp.ui.theme.YellowSecondary
import com.tesis.nutriguideapp.utils.TokenManager
import com.tesis.nutriguideapp.viewmodel.HomeViewModel

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
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(
            width = 1.5.dp,
            color = backgroundColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                backgroundColor,
                                backgroundColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(2.dp), // Pequeño padding interno para el efecto
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    backgroundColor.copy(alpha = 0.9f),
                                    backgroundColor
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = backgroundColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 15.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Indicador visual de que es clickeable
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = backgroundColor.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
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

    // Estado para el menú desplegable
    var showProfileMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (token == null) {
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
    ) {
        // Fondo con patrón decorativo
        Image(
            painter = painterResource(id = R.drawable.home_background_pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con menú de perfil
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menú desplegable para perfil y configuraciones
                Box {
                    IconButton(
                        onClick = { showProfileMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menú",
                            tint = GreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Mi Perfil",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mi Perfil")
                                }
                            },
                            onClick = {
                                showProfileMenu = false
                                navController.navigate("profile")
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Cerrar Sesión",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Red
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cerrar Sesión", color = Color.Red)
                                }
                            },
                            onClick = {
                                showProfileMenu = false
                                viewModel.logout(context) {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logo de NutriGuide
            Image(
                painter = painterResource(id = R.drawable.nutriguide_logo),
                contentDescription = "NutriGuide Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Título principal
            Text(
                text = "NUTRIGUIDE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Saludo personalizado
            Text(
                text = if (username.isNotEmpty()) "¡Hola, $username!" else "¡Bienvenido!",
                fontSize = 18.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frase motivacional
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GreenPrimary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "¿Listo para escanear alimentos y descubrir si son perfectos para ti?",
                    fontSize = 16.sp,
                    color = GreenPrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(20.dp),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            var showAnalyzisOptions by remember { mutableStateOf(false) }

            // Cards principales con mejor espaciado
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
                    title = { 
                        Text(
                            "Analizar Producto", 
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        ) 
                    },
                    text = { 
                        Text(
                            "¿Cómo quieres subir la imagen del producto?",
                            color = Color.DarkGray
                        ) 
                    },
                    confirmButton = {
                        Column {
                            Button(
                                onClick = {
                                    showAnalyzisOptions = false
                                    navController.navigate("upload")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp)
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
                                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                shape = RoundedCornerShape(12.dp)
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
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar")
                            }
                        }
                    },
                    dismissButton = { },
                    shape = RoundedCornerShape(16.dp)
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

            Spacer(modifier = Modifier.height(40.dp))
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
