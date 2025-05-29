package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.ui.theme.WhiteBackground
import com.tesis.nutriguideapp.ui.theme.YellowSecondary
import com.tesis.nutriguideapp.viewmodel.RestriccionesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestriccionesScreen(
    navController: NavController,
    context: Context,
    viewModel: RestriccionesViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val userRestrictions by viewModel.userRestrictions
    val availableRestrictions by viewModel.availableRestrictions
    val loading by viewModel.loading
    val error by viewModel.error
    val success by viewModel.success
    
    // Cargar restricciones al iniciar
    LaunchedEffect(Unit) {
        viewModel.getUserRestrictions(context)
    }
    
    // Mostrar mensajes de error o éxito
    LaunchedEffect(error, success) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
        success?.let {
            snackbarHostState.showSnackbar(it)
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
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            // Barra superior con título y botón de regreso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = GreenPrimary
                    )
                }
                
                Text(
                    text = "RESTRICCIONES ALIMENTICIAS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Tarjeta con las restricciones
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = WhiteBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Selecciona tus restricciones alimenticias",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Estas restricciones se usarán para analizar si los productos son aptos para ti.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Lista de restricciones disponibles
                    availableRestrictions.forEach { restriction ->
                        val isSelected = userRestrictions.contains(restriction)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleRestriction(restriction) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GreenPrimary,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            
                            Text(
                                text = restriction,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(start = 48.dp))
                    }
                }
            }
            
            // Resumen de restricciones seleccionadas
            if (userRestrictions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GreenPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Restricciones seleccionadas (${userRestrictions.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = GreenPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = userRestrictions.joinToString(", "),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Botón guardar
            Button(
                onClick = {
                    viewModel.saveRestrictions(context) {
                        // Navegar de regreso o mostrar éxito
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Restricciones guardadas correctamente")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                )
            ) {
                if (loading) {
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
                    Text("GUARDAR RESTRICCIONES")
                }
            }
        }
        
        // Snackbar para mensajes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
