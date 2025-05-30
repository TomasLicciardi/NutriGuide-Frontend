package com.tesis.nutriguideapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.ui.theme.GreenPrimary
import com.tesis.nutriguideapp.ui.theme.WhiteBackground
import com.tesis.nutriguideapp.utils.ConnectionTester
import kotlinx.coroutines.launch
import java.net.InetAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDiagnosticScreen(
    context: Context,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var connectionStatus by remember { mutableStateOf("No probado") }
    var connectionDetails by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val baseUrl = RetrofitInstance.BASE_URL
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Diagnóstico de Conexión",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Información básica
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Información de Conexión",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text("URL del servidor: $baseUrl")
                    Text("Estado: $connectionStatus")
                }
            }
            
            // Detalles de la prueba
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Detalles del Diagnóstico",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (connectionDetails.isNotEmpty()) {
                        Text(
                            text = connectionDetails,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    } else {
                        Text("No hay detalles disponibles. Ejecuta la prueba primero.")
                    }
                }
            }
            
            // Botones
            Button(
                onClick = {
                    isLoading = true
                    connectionStatus = "Probando..."
                    coroutineScope.launch {
                        try {
                            var details = "--- Prueba de conexión ---\n"
                            details += "Hora: ${java.util.Date()}\n"
                            details += "URL: $baseUrl\n\n"
                            
                            // Probar el servidor
                            details += "Probando conexión al servidor...\n"
                            var connectionSuccess = false
                            ConnectionTester.testBackendConnection(context, false) { success, message ->
                                connectionSuccess = success
                                details += "Resultado: $message\n\n"
                            }
                            
                            // Probar resolución DNS
                            details += "Probando resolución DNS...\n"
                            try {
                                val host = java.net.URL(baseUrl).host
                                val ip = InetAddress.getByName(host)
                                details += "Resolución DNS exitosa: $host -> ${ip.hostAddress}\n\n"
                            } catch (e: Exception) {
                                details += "Error en resolución DNS: ${e.message}\n\n"
                            }
                            
                            // Mostrar estado final
                            connectionStatus = if (connectionSuccess) "Conectado" else "Error de conexión"
                            connectionDetails = details
                            isLoading = false
                        } catch (e: Exception) {
                            connectionStatus = "Error en la prueba"
                            connectionDetails = "Error al ejecutar la prueba: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading
            ) {
                Text("Ejecutar prueba completa")
            }
            
            Button(
                onClick = { onBackClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("Volver")
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        }
    }
}
