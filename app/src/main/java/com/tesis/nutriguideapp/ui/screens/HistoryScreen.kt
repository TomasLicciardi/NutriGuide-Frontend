package com.tesis.nutriguideapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tesis.nutriguideapp.api.ProductService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    navController: NavController,
    selectedRestrictions: Set<String>
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val service = RetrofitInstance.retrofit.create(ProductService::class.java)
                val allProducts = service.getAllProducts()

                products = allProducts.filter { product ->
                    selectedRestrictions.all { restriction ->
                        product.resultJson.contains(restriction, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Historial de Productos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        products.forEach { product ->
            Text(
                text = product.name ?: "Sin nombre",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("product_detail/${product.id}")
                    }
                    .padding(vertical = 8.dp)
            )
            Divider()
        }
    }
}
