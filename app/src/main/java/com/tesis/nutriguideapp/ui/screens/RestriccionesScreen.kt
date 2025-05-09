package com.tesis.nutriguideapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RestriccionesScreen() {
    val restrictions = listOf("Sin gluten", "Sin lactosa", "Baja en azúcar", "Vegetariano", "Vegano")
    var selectedRestrictions by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Selecciona tus Restricciones Alimenticias", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        restrictions.forEach { restriction ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(restriction)
                Checkbox(
                    checked = selectedRestrictions.contains(restriction),
                    onCheckedChange = {
                        selectedRestrictions = if (it) {
                            selectedRestrictions + restriction
                        } else {
                            selectedRestrictions - restriction
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Aquí guardamos las restricciones seleccionadas en el perfil del usuario
            // o podemos usarlas para filtrar productos.
            // Puedes agregar lógica para almacenarlas en una base de datos local
        }) {
            Text("Guardar Restricciones")
        }
    }
}
