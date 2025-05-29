package com.tesis.nutriguideapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tesis.nutriguideapp.ui.theme.Green40
import com.tesis.nutriguideapp.ui.theme.Yellow40
import com.tesis.nutriguideapp.viewmodel.ProductDetailViewModel
import com.tesis.nutriguideapp.viewmodel.RestriccionesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    navController: NavController,
    viewModel: ProductDetailViewModel = viewModel(),
    restriccionesViewModel: RestriccionesViewModel = viewModel()
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val analysisDetails by viewModel.analysisDetails.collectAsState()
    val userRestrictions by restriccionesViewModel.userRestrictions
    
    var showFullAnalysis by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Cargar el producto y las restricciones del usuario cuando se inicia la pantalla
    LaunchedEffect(productId) {
        restriccionesViewModel.getUserRestrictions(navController.context)
        viewModel.loadProduct(navController.context, productId)
    }
    
    // Verificar si el producto es adecuado para el usuario cuando se cargan ambos datos
    LaunchedEffect(product, userRestrictions) {
        if (product != null && userRestrictions.isNotEmpty()) {
            viewModel.checkSuitabilityForUser(userRestrictions.toList())
        }
    }
    
    // Mostrar errores con Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = product?.name ?: "Detalles del Producto",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green40)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                ) {
                    product?.let { p ->
                        // Imagen del producto
                        p.imageUrl?.let { url ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(250.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter("http://10.0.2.2:8000$url"),
                                    contentDescription = "Imagen del producto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } ?: run {
                            // Si no hay imagen, mostrar un placeholder
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(120.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Sin imagen disponible",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        
                        // Información del producto
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Información del Producto",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Nombre del producto
                                Text(
                                    text = "Nombre: ${p.name ?: "Sin nombre"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Fecha de análisis si está disponible
                                p.date?.let { date ->
                                    Text(
                                        text = "Fecha de análisis: $date",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                // Resumen del análisis
                                Text(
                                    text = "Resumen del análisis:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Detalles rápidos del análisis
                                analysisDetails.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val textColor = if (value == true) Color.Red else Green40
                                        val bulletColor = if (value == true) Color.Red else Green40
                                        
                                        // Bullet point
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(bulletColor, shape = RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Texto del detalle
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textColor
                                        )
                                    }
                                }
                                
                                // Botón para mostrar el análisis completo
                                TextButton(
                                    onClick = { showFullAnalysis = !showFullAnalysis },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = if (showFullAnalysis) "Ocultar análisis completo" else "Ver análisis completo"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (showFullAnalysis) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
                                
                                // Análisis completo (texto JSON)
                                AnimatedVisibility(
                                    visible = showFullAnalysis,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Análisis completo:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = p.resultJson,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Mostrar si el producto es apto para el usuario
                        analysisDetails["Apto para usuario"]?.let { isAptoBool ->
                            val isApto = isAptoBool as? Boolean ?: false
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isApto) Green40 else Yellow40
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isApto) "✓ Este producto es apto para ti" else "⚠ Este producto no es recomendado",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Acciones adicionales
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Yellow40.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "¿Tienes dudas sobre este análisis?",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { /* Acción para contactar soporte */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green40
                                    )
                                ) {
                                    Text("Contactar soporte")
                                }
                            }
                        }
                    } ?: run {
                        // Si no hay producto
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No se pudo cargar la información del producto",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    )
}

