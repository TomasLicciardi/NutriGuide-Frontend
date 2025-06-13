package com.tesis.nutriguideapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.ui.theme.WhiteBackground
import com.tesis.nutriguideapp.ui.theme.YellowSecondary
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
    val uploading by viewModel.uploading
    val analysisResponse by viewModel.analysisResponse
    val error by viewModel.error
    
    // Launcher para selector de archivos nativo (no Google Photos)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setImageUri(uri)
    }
    
    // Launcher alternativo usando intent personalizado para mostrar más opciones
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
            // Área para la imagen con mejor manejo de escala
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
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
            
            // Mostrar resultado del análisis si existe
            if (analysisResponse != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Título del análisis
                        Text(
                            text = "Resultado del análisis",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
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
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (analysisResponse?.suitable == true) 
                                            "Producto APTO" 
                                        else 
                                            "Producto NO APTO",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (analysisResponse?.suitable == true) 
                                            "Según tus restricciones alimentarias" 
                                        else 
                                            "Contiene ingredientes restringidos",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }                        // Restricciones detectadas
                        val restrictionsNotSuitable = remember {
                            try {
                                analysisResponse?.resultJson?.clasificacion?.filter { 
                                    !it.value.apto && !it.value.razon.isNullOrEmpty() 
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("UploadScreen", "Error al filtrar restricciones: ${e.message}")
                                null
                            }
                        }
                        if (!restrictionsNotSuitable.isNullOrEmpty()) {
                            Text(
                                text = "Restricciones detectadas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            restrictionsNotSuitable.forEach { (restriction, details) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Restricción",
                                            tint = Color(0xFFF57C00),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = restriction.replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            details.razon?.let { razon ->
                                                Text(
                                                    text = razon,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }                        // Ingredientes
                        val ingredientesState = remember {
                            try {
                                val ingredientes = analysisResponse?.resultJson?.ingredientes
                                if (!ingredientes.isNullOrEmpty()) {
                                    IngredientState.Available(ingredientes)
                                } else {
                                    IngredientState.Empty
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("UploadScreen", "Error al procesar ingredientes: ${e.message}", e)
                                IngredientState.Error("Error al procesar los ingredientes: ${e.message}")
                            }
                        }
                        
                        when (ingredientesState) {
                            is IngredientState.Available -> {
                                Text(
                                    text = "Ingredientes detectados",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F5F5)
                                    )
                                ) {
                                    Text(
                                        text = ingredientesState.ingredientes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            is IngredientState.Empty -> {
                                Text(
                                    text = "Ingredientes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    )
                                ) {
                                    Text(
                                        text = "No se detectaron ingredientes",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            is IngredientState.Error -> {
                                Text(
                                    text = "Error en ingredientes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)
                                    )
                                ) {
                                    Text(
                                        text = ingredientesState.errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        // Puede contener
                        val puedeContenerState = remember {
                            try {
                                val puedeContener = analysisResponse?.resultJson?.puedeContener
                                if (!puedeContener.isNullOrEmpty()) {
                                    PuedeContenerState.Available(puedeContener)
                                } else {
                                    PuedeContenerState.Empty
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("UploadScreen", "Error al procesar 'puede contener': ${e.message}", e)
                                PuedeContenerState.Error("Error al procesar 'puede contener': ${e.message}")
                            }
                        }
                        
                        when (puedeContenerState) {
                            is PuedeContenerState.Available -> {
                                Text(
                                    text = "Puede contener",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    )
                                ) {
                                    Text(
                                        text = puedeContenerState.contenido,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            is PuedeContenerState.Empty -> {
                                // No hacer nada, simplemente no mostrar sección
                            }
                            is PuedeContenerState.Error -> {
                                Text(
                                    text = "Error en 'puede contener'",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)
                                    )
                                ) {
                                    Text(
                                        text = puedeContenerState.errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        // Ahora el botón de guardado debe guardar directamente 
                        // porque el análisis ya se guardó en el backend
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Guardado",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Producto analizado y guardado",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "ID del producto: ${analysisResponse?.productId}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botón para volver al inicio
                        Button(
                            onClick = { navController?.navigate("home") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Volver al inicio",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "VOLVER AL INICIO",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar para errores
        if (error != null) {
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(error ?: "")
            }
        }
    }
}

// Estados para manejar diferentes casos de ingredientes
sealed class IngredientState {
    data class Available(val ingredientes: String) : IngredientState()
    data class Error(val errorMessage: String) : IngredientState()
    object Empty : IngredientState()
}

// Estados para manejar diferentes casos de "puede contener"
sealed class PuedeContenerState {
    data class Available(val contenido: String) : PuedeContenerState()
    object Empty : PuedeContenerState()
    data class Error(val errorMessage: String) : PuedeContenerState()
}
