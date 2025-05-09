package com.tesis.nutriguideapp.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tesis.nutriguideapp.utils.TokenManager

@Composable
fun HomeScreen(navController: NavController, context: Context) {
    val tokenManager = TokenManager(context)
    val token = tokenManager.getToken()

    LaunchedEffect(Unit) {
        if (token == null) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bienvenido a NutriGuide", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.navigate("upload") }, modifier = Modifier.fillMaxWidth()) {
            Text("Subir Producto")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // TODO: Reemplazar esto con lo que el usuario realmente seleccionó en RestriccionesScreen
                val selectedRestrictions = setOf("sin gluten", "sin lactosa")
                val restrictionsParam = selectedRestrictions.joinToString(",")
                navController.navigate("history/$restrictionsParam")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Historial")
        }


        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.navigate("restricciones") }, modifier = Modifier.fillMaxWidth()) {
            Text("Configurar Restricciones")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                tokenManager.clearToken()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión")
        }
    }
}
