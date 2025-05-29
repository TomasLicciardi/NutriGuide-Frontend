package com.tesis.nutriguideapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val coroutineScope = rememberCoroutineScope()
    
    val imageUri by viewModel.imageUri
    val productName by viewModel.productName
    val analyzing by viewModel.analyzing
    val uploading by viewModel.uploading
    val analysisResponse by viewModel.analysisResponse
    val error by viewModel.error
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.setImageUri(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "ANALIZAR PRODUCTO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Área para la imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
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
                                color = Color.Gray
                            )
                        }
                    }
                    
                    if (analyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = GreenPrimary
                        )
                    }
                }
            }
              // Botón para seleccionar imagen
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Seleccionar imagen"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SELECCIONAR IMAGEN")
            }
            
            // Botón para usar cámara directamente
            OutlinedButton(
                onClick = { navController?.navigate("camera") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GreenPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Usar cámara"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("USAR CÁMARA")
            }
            
            // Botón para analizar
            Button(
                onClick = { viewModel.analyzeImage(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = imageUri != null && !analyzing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Analizar"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (analyzing) "ANALIZANDO..." else "ANALIZAR IMAGEN")
            }
            
            // Mostrar resultado del análisis si existe
            if (analysisResponse != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = WhiteBackground
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Nombre del producto
                        Text(
                            text = "Resultado del análisis",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Campo para editar el nombre
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { viewModel.setProductName(it) },
                            label = { Text("Nombre del producto") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Producto"
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        
                        // Aptitud del producto
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (analysisResponse?.suitable == true) 
                                    Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (analysisResponse?.suitable == true) 
                                        Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = "Aptitud",
                                    tint = if (analysisResponse?.suitable == true) 
                                        Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (analysisResponse?.suitable == true) 
                                        "Producto apto según tus restricciones" 
                                    else 
                                        "Producto NO apto según tus restricciones",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Restricciones detectadas
                        if (analysisResponse?.restrictionsDetected?.isNotEmpty() == true) {
                            Text(
                                text = "Restricciones detectadas:",
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                            
                            analysisResponse?.restrictionsDetected?.forEach { restriction ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Restricción",
                                        tint = Color(0xFFF44336),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(restriction)
                                }
                            }
                        }
                        
                        // Ingredientes
                        if (analysisResponse?.ingredients?.isNotEmpty() == true) {
                            Text(
                                text = "Ingredientes:",
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    analysisResponse?.ingredients?.joinToString(", ") ?: "",
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // Texto detectado
                        if (!analysisResponse?.textDetected.isNullOrEmpty()) {
                            Text(
                                text = "Texto detectado:",
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    analysisResponse?.textDetected ?: "",
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // Botón para guardar el producto
                        Button(
                            onClick = { 
                                viewModel.uploadProduct(context) {
                                    // Navegar a historial o mostrar éxito
                                    navController?.navigate("home")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            enabled = !uploading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary
                            )
                        ) {
                            if (uploading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Guardar"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GUARDAR PRODUCTO")
                            }
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
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
