package com.tesis.nutriguideapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tesis.nutriguideapp.api.ProductService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(productId: Int) {
    var product by remember { mutableStateOf<Product?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(productId) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val service = RetrofitInstance.retrofit.create(ProductService::class.java)
                product = service.getProductById(productId)
            } catch (e: Exception) {
                // Manejo de errores (puedes mostrar un Snackbar o Log)
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Detalle del Producto", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        product?.let { p ->
            p.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter("http://10.0.2.2:8000$url"),
                    contentDescription = "Imagen del producto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Nombre: ${p.name ?: "Sin nombre"}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Resultado del análisis:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(p.resultJson)
        } ?: Text("Cargando producto...")
    }
}
