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
    
    // Launcher para selector de archivos
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setImageUri(uri)
    }
    
    // Launcher alternativo para más opciones
    val intentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            viewModel.setImageUri(uri)
        }
    }
    
    // Función para abrir selector con más opciones
    fun openFileSelector() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        val chooser = Intent.createChooser(intent, "Seleccionar imagen desde...")
        intentLauncher.launch(chooser)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analizar Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            
            // Botones de acción
            Button(
                onClick = { openFileSelector() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Seleccionar imagen",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SELECCIONAR DESDE ARCHIVOS",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón alternativo para galería tradicional
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Seleccionar de galería",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SELECCIONAR DE GALERÍA",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
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
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    )
                                ) {
                                    Text(
                                        text = "• ${restriction.uppercase()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFF9800),
                                        modifier = Modifier.padding(12.dp)
                                    )
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
        
        // Snackbar para errores
        if (error != null) {
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(error ?: "")
            }
        }
    }
}
