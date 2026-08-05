package com.tesis.nutriguideapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tesis.nutriguideapp.model.AnalysisResult
import com.tesis.nutriguideapp.ui.theme.Green40
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.utils.RestrictionMapper
import com.tesis.nutriguideapp.viewmodel.CameraViewModel
import java.io.File
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor: Executor = ContextCompat.getMainExecutor(context)

    val imageUri by viewModel.imageUri.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val analyzeSuccess by viewModel.analyzeSuccess.collectAsState()
    val analysisResponse by viewModel.analysisResponse.collectAsState()

    val analysisState by viewModel.analysisState.collectAsState()
    val showErrorModal by viewModel.showErrorModal.collectAsState()

    var showTipsModal by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "Se requieren permisos de cámara para esta funcionalidad",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        try {
            hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error al verificar permisos: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            if (!analyzeSuccess) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capturar Etiqueta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Green40)
                        }
                    }

                    imageUri != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Imagen capturada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { viewModel.clearImage() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Descartar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Descartar")
                                }

                                Button(
                                    onClick = { viewModel.analyzeImage(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green40
                                    )
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Analizar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analizar")
                                }
                            }
                        }
                    }

                    hasCameraPermission -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CameraPreview(
                                context = context,
                                lifecycleOwner = lifecycleOwner,
                                imageCapture = imageCapture
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                                    .align(Alignment.BottomCenter),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.takePicture(
                                            imageCapture = imageCapture,
                                            outputDirectory = getOutputDirectory(context),
                                            executor = executor
                                        )
                                    },
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(Green40, CircleShape)
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Camera,
                                        contentDescription = "Capturar",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Se requieren permisos de cámara para esta funcionalidad",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green40
                                    )
                                ) {
                                    Text("Conceder Permisos")
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    // Dialog de resultado exitoso
    if (analyzeSuccess && analysisResponse != null) {
        val response = analysisResponse!!
        var selectedTrigger by remember { mutableStateOf<com.tesis.nutriguideapp.model.TriggerIngredient?>(null) }
        com.tesis.nutriguideapp.ui.components.TriggerExplanationDialog(
            trigger = selectedTrigger,
            onDismiss = { selectedTrigger = null }
        )
        AlertDialog(
            onDismissRequest = { },
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
                    IconButton(onClick = {
                        viewModel.clearAnalysisAndRetakePhoto()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (response.userVerdict) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (response.userVerdict) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = "Aptitud",
                                tint = if (response.userVerdict) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (response.userVerdict) "Producto APTO" else "Producto NO APTO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = if (response.userVerdict) "Seguro según tus restricciones" else "Contiene ingredientes restringidos",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    val restrictionsNotSuitable = response.restrictions.filter { !it.value.apto }
                    if (restrictionsNotSuitable.isNotEmpty()) {
                        Text(
                            text = "Restricciones detectadas:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Color(0xFFFF9800)
                        )
                        restrictionsNotSuitable.entries.take(3).forEach { (key, value) ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = RestrictionMapper.toDisplayName(key).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFF9800)
                                    )
                                    if (!value.motivo.isNullOrBlank()) {
                                        Text(
                                            text = value.motivo,
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.clearAnalysisAndRetakePhoto()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inicio")
                    }
                    OutlinedButton(
                        onClick = { viewModel.clearAnalysisAndRetakePhoto() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Otra foto", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Otra Foto")
                    }
                }
            },
            dismissButton = { }
        )
    }

    // Modales de errores específicos
    when (val state = analysisState) {
        is AnalysisResult.ImageError -> {
            val (title, color) = when (state.errorType) {
                "invalid_image" -> "Imagen no válida" to Color(0xFFFF5722)
                "poor_quality" -> "Imagen borrosa" to Color(0xFFFF9800)
                "no_ingredients" -> "Ingredientes no detectados" to Color(0xFF9C27B0)
                else -> "Error" to Color.Red
            }
            SimpleErrorModal(
                isVisible = showErrorModal,
                title = title,
                message = state.message,
                instructions = state.instructions,
                primaryButtonText = "Tomar otra foto",
                onPrimaryClick = { viewModel.dismissErrorModal(); viewModel.clearAnalysisAndRetakePhoto() },
                secondaryButtonText = "Ver Tips",
                onSecondaryClick = { viewModel.dismissErrorModal(); showTipsModal = true },
                onDismiss = { viewModel.dismissErrorModal(); viewModel.clearAnalysisAndRetakePhoto() },
                primaryColor = color
            )
        }
        is AnalysisResult.LowConfidenceError -> {
            SimpleErrorModal(
                isVisible = showErrorModal,
                title = "Imagen no se visualiza correctamente",
                message = state.message,
                instructions = state.instructions,
                primaryButtonText = "Tomar otra foto",
                onPrimaryClick = { viewModel.dismissErrorModal(); viewModel.clearAnalysisAndRetakePhoto() },
                secondaryButtonText = "Ver Tips",
                onSecondaryClick = { viewModel.dismissErrorModal(); showTipsModal = true },
                onDismiss = { viewModel.dismissErrorModal(); viewModel.clearAnalysisAndRetakePhoto() },
                primaryColor = Color(0xFFFF9800)
            )
        }
        is AnalysisResult.ServerError -> {
            SimpleErrorModal(
                isVisible = showErrorModal,
                title = "Error del servidor",
                message = state.message,
                instructions = state.instructions,
                primaryButtonText = "Reintentar",
                onPrimaryClick = { viewModel.dismissErrorModal(); viewModel.analyzeImage(context) },
                secondaryButtonText = "Cancelar",
                onSecondaryClick = { viewModel.dismissErrorModal() },
                onDismiss = { viewModel.dismissErrorModal() },
                primaryColor = Color(0xFFF44336)
            )
        }
        is AnalysisResult.NetworkError -> {
            SimpleErrorModal(
                isVisible = showErrorModal,
                title = "Error de conexión",
                message = state.message,
                instructions = state.instructions,
                primaryButtonText = "Reintentar",
                onPrimaryClick = { viewModel.dismissErrorModal(); viewModel.analyzeImage(context) },
                secondaryButtonText = "Cancelar",
                onSecondaryClick = { viewModel.dismissErrorModal() },
                onDismiss = { viewModel.dismissErrorModal() },
                primaryColor = Color(0xFF607D8B)
            )
        }
        is AnalysisResult.RateLimitError -> {
            SimpleErrorModal(
                isVisible = showErrorModal,
                title = "Límite de solicitudes",
                message = state.message,
                instructions = state.instructions,
                primaryButtonText = "Reintentar",
                onPrimaryClick = { viewModel.dismissErrorModal(); viewModel.analyzeImage(context) },
                secondaryButtonText = "Cancelar",
                onSecondaryClick = { viewModel.dismissErrorModal() },
                onDismiss = { viewModel.dismissErrorModal() },
                primaryColor = Color(0xFF2196F3)
            )
        }
        else -> Unit
    }

    if (showTipsModal) {
        SimpleTipsModal(onDismiss = { showTipsModal = false })
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCapture: ImageCapture
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (_: Exception) { }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, context.packageName).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}
