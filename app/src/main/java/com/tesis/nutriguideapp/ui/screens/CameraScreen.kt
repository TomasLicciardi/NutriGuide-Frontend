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
import com.tesis.nutriguideapp.viewmodel.CameraViewModel
import kotlinx.coroutines.delay
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
    val productId by viewModel.productId.collectAsState()

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
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(analyzeSuccess, productId) {
        if (analyzeSuccess && productId != null) {
            delay(300)
            try {
                navController.navigate("product_detail/$productId") {
                    popUpTo("camera") { inclusive = true }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al navegar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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

    val currentAnalysisState = analysisState
    android.util.Log.d(
        "CameraScreen",
        "Evaluando estado para mostrar modales: $currentAnalysisState, Modal visible: $showErrorModal"
    )

    when (currentAnalysisState) {
        is AnalysisResult.ImageError -> {
            val (title, color) = when (currentAnalysisState.errorType) {
                "invalid_image" -> "❌ Imagen no válida" to Color(0xFFFF5722)
                "poor_quality" -> "⚠️ Imagen borrosa" to Color(0xFFFF9800)
                "no_ingredients" -> "🔍 Ingredientes no detectados" to Color(0xFF9C27B0)
                else -> "Error" to Color.Red
            }

            SimpleErrorModal(
                isVisible = showErrorModal,
                title = title,
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
                primaryColor = color
            )
        }

        is AnalysisResult.LowConfidenceError -> {
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
                onSecondaryClick = { viewModel.dismissErrorModal() },
                onDismiss = { viewModel.dismissErrorModal() },
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
                onSecondaryClick = { viewModel.dismissErrorModal() },
                onDismiss = { viewModel.dismissErrorModal() },
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
                onSecondaryClick = { viewModel.dismissErrorModal() },
                onDismiss = { viewModel.dismissErrorModal() },
                primaryColor = Color(0xFF2196F3)
            )
        }

        else -> Unit
    }

    if (showTipsModal) {
        SimpleTipsModal(
            onDismiss = { showTipsModal = false }
        )
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
                } catch (e: Exception) {
                    // Error al enlazar casos de uso
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, "NutriGuide").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}
