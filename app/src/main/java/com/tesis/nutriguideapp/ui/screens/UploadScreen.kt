package com.tesis.nutriguideapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.viewmodel.UploadViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import com.tesis.nutriguideapp.model.AnalysisResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController? = null,
    viewModel: UploadViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val imageUri by viewModel.imageUri
    val analyzing by viewModel.analyzing
    val analysisResponse by viewModel.analysisResponse
    val error by viewModel.error
    
    // 🆕 NUEVOS ESTADOS PARA MANEJO DE ERRORES
    val analysisState by viewModel.analysisState
    val showErrorModal by viewModel.showErrorModal
    
    // Debug: Log cambios de estado
    LaunchedEffect(analysisState, showErrorModal) {
        android.util.Log.d("UploadScreen", "Estado análisis: $analysisState")
        android.util.Log.d("UploadScreen", "Mostrar modal: $showErrorModal")
    }
    
    // Estado para mostrar tips de fotografía
    var showTipsModal by remember { mutableStateOf(false) }
    
    // Launcher para galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setImageUri(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analizar Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // 🆕 BOTÓN DE AYUDA PARA TIPS
                    IconButton(onClick = { showTipsModal = true }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Tips para mejores fotos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Área para la imagen que ocupa todo el contenedor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Seleccionar imagen",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Selecciona una imagen para analizar",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    if (analyzing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = GreenPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón para galería
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Seleccionar de galería",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SELECCIONAR DE GALERÍA",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón para usar cámara
            OutlinedButton(
                onClick = { navController?.navigate("camera") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color(0xFF2196F3))
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Usar cámara",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "TOMAR FOTO",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2196F3)
                )
            }
            
            if (imageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.analyzeImage(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !analyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    )
                ) {
                    if (analyzing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("ANALIZANDO...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Analizar",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ANALIZAR IMAGEN",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        
        // Modal para mostrar resultado del análisis
        if (analysisResponse != null) {
            AlertDialog(
                onDismissRequest = { /* No permitir cerrar tocando fuera */ },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resultado del Análisis",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                        
                        IconButton(
                            onClick = { viewModel.clearAnalysis() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Aptitud del producto
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (analysisResponse?.suitable == true) 
                                    Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (analysisResponse?.suitable == true) 
                                        Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = "Aptitud",
                                    tint = if (analysisResponse?.suitable == true) 
                                        Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (analysisResponse?.suitable == true) 
                                            "✓ Producto APTO" 
                                        else 
                                            "✗ Producto NO APTO",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = if (analysisResponse?.suitable == true) 
                                            "Seguro según tus restricciones" 
                                        else 
                                            "Contiene ingredientes restringidos",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        
                        // Restricciones detectadas si hay
                        val restrictionsNotSuitable = try {
                            analysisResponse?.resultJson?.clasificacion?.filter { 
                                !it.value.apto && !it.value.razon.isNullOrEmpty() 
                            }
                        } catch (e: Exception) {
                            null
                        }
                          if (!restrictionsNotSuitable.isNullOrEmpty()) {
                            Text(
                                text = "⚠️ Restricciones detectadas:",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = Color(0xFFFF9800)
                            )
                            
                            var count = 0
                            for (entry in restrictionsNotSuitable) {
                                if (count >= 3) break
                                val restriction = entry.key
                                val restrictionData = entry.value
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "• ${restriction.uppercase()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFFF9800)
                                        )
                                        // Mostrar la explicación si está disponible
                                        if (!restrictionData.razon.isNullOrBlank()) {
                                            Text(
                                                text = restrictionData.razon,
                                                fontSize = 12.sp,
                                                color = Color(0xFF666666),
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                                count++
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón para volver al inicio
                        Button(
                            onClick = {
                                viewModel.clearAnalysis()
                                navController?.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Inicio",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inicio")
                        }
                        
                        // Botón para analizar otro producto
                        OutlinedButton(
                            onClick = {
                                viewModel.clearAnalysis()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Analizar otro",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analizar Otro")
                        }
                    }
                },
                dismissButton = { }
            )
        }
        
        // 🆕 MODALES DE ERRORES ESPECÍFICOS - INTEGRADOS DIRECTAMENTE
        // Capturar el estado actual para evitar smart cast issues
        val currentAnalysisState = analysisState
        android.util.Log.d("UploadScreen", "Evaluando estado para mostrar modales: $currentAnalysisState, Modal visible: $showErrorModal")
        when (currentAnalysisState) {
            is AnalysisResult.ImageError -> {
                when (currentAnalysisState.errorType) {
                    "invalid_image" -> {
                        SimpleErrorModal(
                            isVisible = showErrorModal,
                            title = "❌ Imagen no válida",
                            message = currentAnalysisState.message,
                            instructions = currentAnalysisState.instructions,
                            primaryButtonText = "📷 Tomar otra foto",
                            onPrimaryClick = { 
                                viewModel.dismissErrorModal()
                                viewModel.clearAnalysisAndRetakePhoto()
                            },
                            secondaryButtonText = "💡 Ver Tips",
                            onSecondaryClick = { 
                                viewModel.dismissErrorModal()
                                showTipsModal = true
                            },
                            onDismiss = { 
                                viewModel.dismissErrorModal()
                                viewModel.clearAnalysisAndRetakePhoto()
                            },
                            primaryColor = Color(0xFFFF5722)
                        )
                    }
                    "poor_quality" -> {
                        SimpleErrorModal(
                            isVisible = showErrorModal,
                            title = "⚠️ Imagen borrosa",
                            message = currentAnalysisState.message,
                            instructions = currentAnalysisState.instructions,
                            primaryButtonText = "📷 Tomar otra foto",
                            onPrimaryClick = { 
                                viewModel.dismissErrorModal()
                                viewModel.clearAnalysisAndRetakePhoto()
                            },
                            secondaryButtonText = "💡 Ver Tips",
                            onSecondaryClick = { 
                                viewModel.dismissErrorModal()
                                showTipsModal = true
                            },
                            onDismiss = { 
                                viewModel.dismissErrorModal()
                                viewModel.clearAnalysisAndRetakePhoto()
                            },
                            primaryColor = Color(0xFFFF9800)
                        )
                    }
                    "no_ingredients" -> {
                        SimpleErrorModal(
                            isVisible = showErrorModal,
                            title = "🔍 Ingredientes no detectados",
                            message = currentAnalysisState.message,
                            instructions = currentAnalysisState.instructions,
                            primaryButtonText = "📷 Tomar otra foto",
                            onPrimaryClick = { 
                                viewModel.dismissErrorModal()
                                viewModel.clearAnalysisAndRetakePhoto()
                            },
                            secondaryButtonText = "💡 Ver Tips",
                            onSecondaryClick = { 
                                viewModel.dismissErrorModal()
                                showTipsModal = true
                            },
                            onDismiss = { 
                                viewModel.dismissErrorModal()
                                viewModel.clearAnalysisAndRetakePhoto()
                            },
                            primaryColor = Color(0xFF9C27B0)
                        )
                    }
                }
            }
            is AnalysisResult.LowConfidenceError -> {
                android.util.Log.d("UploadScreen", "Mostrando modal para LowConfidenceError - isVisible: $showErrorModal")
                SimpleErrorModal(
                    isVisible = showErrorModal,
                    title = "⚠️ Análisis con baja confianza",
                    message = currentAnalysisState.message,
                    instructions = currentAnalysisState.instructions,
                    primaryButtonText = "📷 Tomar otra foto",
                    onPrimaryClick = { 
                        viewModel.dismissErrorModal()
                        viewModel.clearAnalysisAndRetakePhoto()
                    },
                    secondaryButtonText = "💡 Ver Tips",
                    onSecondaryClick = { 
                        viewModel.dismissErrorModal()
                        showTipsModal = true
                    },
                    onDismiss = { 
                        viewModel.dismissErrorModal()
                        viewModel.clearAnalysisAndRetakePhoto()
                    },
                    primaryColor = Color(0xFFFF9800)
                )
            }
            is AnalysisResult.ServerError -> {
                SimpleErrorModal(
                    isVisible = showErrorModal,
                    title = "❌ Error del servidor",
                    message = currentAnalysisState.message,
                    instructions = currentAnalysisState.instructions,
                    primaryButtonText = "🔄 Reintentar",
                    onPrimaryClick = { 
                        viewModel.dismissErrorModal()
                        viewModel.analyzeImage(context)
                    },
                    secondaryButtonText = "Cancelar",
                    onSecondaryClick = { 
                        viewModel.dismissErrorModal()
                    },
                    onDismiss = { 
                        viewModel.dismissErrorModal()
                    },
                    primaryColor = Color(0xFFF44336)
                )
            }
            is AnalysisResult.NetworkError -> {
                SimpleErrorModal(
                    isVisible = showErrorModal,
                    title = "📶 Error de conexión",
                    message = currentAnalysisState.message,
                    instructions = currentAnalysisState.instructions,
                    primaryButtonText = "🔄 Reintentar",
                    onPrimaryClick = { 
                        viewModel.dismissErrorModal()
                        viewModel.analyzeImage(context)
                    },
                    secondaryButtonText = "Cancelar",
                    onSecondaryClick = { 
                        viewModel.dismissErrorModal()
                    },
                    onDismiss = { 
                        viewModel.dismissErrorModal()
                    },
                    primaryColor = Color(0xFF607D8B)
                )
            }
            is AnalysisResult.RateLimitError -> {
                SimpleErrorModal(
                    isVisible = showErrorModal,
                    title = "⏳ Límite de solicitudes",
                    message = currentAnalysisState.message,
                    instructions = currentAnalysisState.instructions,
                    primaryButtonText = "🔄 Reintentar",
                    onPrimaryClick = { 
                        viewModel.dismissErrorModal()
                        viewModel.analyzeImage(context)
                    },
                    secondaryButtonText = "Cancelar",
                    onSecondaryClick = { 
                        viewModel.dismissErrorModal()
                    },
                    onDismiss = { 
                        viewModel.dismissErrorModal()
                    },
                    primaryColor = Color(0xFF2196F3)
                )
            }
            else -> {
                // No mostrar modal para estados Success o Loading
            }
        }
        
        // 🆕 MODAL DE TIPS PARA FOTOGRAFÍA SIMPLE
        if (showTipsModal) {
            SimpleTipsModal(
                onDismiss = { showTipsModal = false }
            )
        }
        
        // Snackbar para errores
        if (error != null) {
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(error ?: "")
            }
        }
    }
}

// 🆕 MODALES SIMPLES INTEGRADOS DIRECTAMENTE
@Composable
fun SimpleErrorModal(
    isVisible: Boolean,
    title: String,
    message: String,
    instructions: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryClick: () -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Mensaje
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Instrucciones
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = instructions,
                            fontSize = 14.sp,
                            color = primaryColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    
                    // Botones
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPrimaryClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text(primaryButtonText, color = Color.White)
                        }
                        
                        OutlinedButton(
                            onClick = onSecondaryClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(secondaryButtonText, color = primaryColor)
                        }
                        
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTipsModal(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Tips para mejores fotos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                // Tips list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TipCard(
                        emoji = "☀️",
                        title = "Buena iluminación",
                        description = "Usa luz natural o asegúrate de que la etiqueta esté bien iluminada",
                        color = Color(0xFFFF5722)
                    )
                    
                    TipCard(
                        emoji = "🎯",
                        title = "Enfoque nítido",
                        description = "Asegúrate de que el texto de la etiqueta se vea claramente",
                        color = Color(0xFF4CAF50)
                    )
                    
                    TipCard(
                        emoji = "📐",
                        title = "Imagen derecha",
                        description = "Mantén la etiqueta derecha y evita ángulos inclinados",
                        color = Color(0xFF2196F3)
                    )
                    
                    TipCard(
                        emoji = "🔍",
                        title = "Distancia correcta",
                        description = "Acércate lo suficiente para que la etiqueta llene la imagen",
                        color = Color(0xFF9C27B0)
                    )
                    
                    TipCard(
                        emoji = "🚫",
                        title = "Evita reflejos",
                        description = "Inclina ligeramente la cámara para evitar reflejos en la etiqueta",
                        color = Color(0xFF607D8B)
                    )
                    
                    TipCard(
                        emoji = "✂️",
                        title = "Solo la etiqueta",
                        description = "Enfócate únicamente en la etiqueta nutricional del producto",
                        color = Color(0xFFF44336)
                    )
                }
                
                // Close button
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("✅ Entendido", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TipCard(
    emoji: String,
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
