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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tesis.nutriguideapp.model.ProductAnalysis
import com.tesis.nutriguideapp.ui.theme.Green40
import com.tesis.nutriguideapp.ui.theme.Yellow40
import com.tesis.nutriguideapp.utils.CoilUtils
import com.tesis.nutriguideapp.utils.RestrictionMapper
import com.tesis.nutriguideapp.viewmodel.ProductDetailViewModel
import com.tesis.nutriguideapp.utils.DateFormatter
import com.tesis.nutriguideapp.viewmodel.RestriccionesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    navController: NavController,
    viewModel: ProductDetailViewModel = viewModel(),
    restriccionesViewModel: RestriccionesViewModel = viewModel()
) {    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val analysisDetails by viewModel.analysisDetails.collectAsState()
    val userRestrictions by restriccionesViewModel.userRestrictions
    val imageFile by viewModel.imageFile.collectAsState()
    
    val context = LocalContext.current
    var showFullAnalysis by remember { mutableStateOf(false) }
    var selectedTrigger by remember { mutableStateOf<com.tesis.nutriguideapp.model.TriggerIngredient?>(null) }
    val scrollState = rememberScrollState()

    com.tesis.nutriguideapp.ui.components.TriggerExplanationDialog(
        trigger = selectedTrigger,
        onDismiss = { selectedTrigger = null }
    )
    
    // Cargar el producto y las restricciones del usuario cuando se inicia la pantalla
    LaunchedEffect(productId) {
        restriccionesViewModel.getUserRestrictions(context)
        viewModel.loadProduct(context, productId)
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
        topBar = {            TopAppBar(
                title = { 
                    Text(
                        text = "Detalles del Producto",
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
                    product?.let { p ->                        // Imagen del producto
                        if (imageFile != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(250.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageFile),
                                    contentDescription = "Imagen del producto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else if (p.imageUrl != null) {
                            // Si no se pudo descargar la imagen, mostrar placeholder
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
                                        "Error al cargar imagen",
                                        style = MaterialTheme.typography.bodyLarge
                                    )                                }
                            }
                        } else {
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
                                Spacer(modifier = Modifier.height(16.dp))                                  // Fecha de análisis si está disponible
                                p.date?.let { date ->
                                    Text(
                                        text = "Fecha de análisis: ${DateFormatter.formatDate(date)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                                
                                // Ingredientes que dispararon cada restricción (tocables)
                                val blockedWithTriggers = p.resultJson.restrictions
                                    .filter { !it.value.apto && it.value.triggerIngredients.isNotEmpty() }
                                if (blockedWithTriggers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Ingredientes detectados (tocá para ver más):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    blockedWithTriggers.forEach { (apiName, restriction) ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = RestrictionMapper.toDisplayName(apiName),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFF9800)
                                        )
                                        com.tesis.nutriguideapp.ui.components.TriggerIngredientChips(
                                            triggers = restriction.triggerIngredients.take(6),
                                            onTriggerClick = { selectedTrigger = it }
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
                                        text = if (showFullAnalysis) "Ocultar ingredientes" else "Ver ingredientes"
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
                                ) {                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Ingredientes:",
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
                                                text = formatProductAnalysis(p.resultJson),
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
                            }                        }
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

private fun formatProductAnalysis(productAnalysis: ProductAnalysis): String {
    return buildString {
        try {
            if (productAnalysis.ingredients.isEmpty()) {
                appendLine("No disponible")
            } else {
                productAnalysis.ingredients.forEach { ingredient ->
                    val name = ingredient.nameEs.takeIf { it.isNotBlank() } ?: ingredient.nameEn
                    if (name.isNotBlank()) {
                        appendLine("• $name")
                    }
                }
            }
        } catch (e: Exception) {
            appendLine("Error al formatear análisis: ${e.message}")
        }
    }
}

