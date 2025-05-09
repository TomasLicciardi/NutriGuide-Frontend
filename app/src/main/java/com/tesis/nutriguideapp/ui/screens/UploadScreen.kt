package com.tesis.nutriguideapp.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tesis.nutriguideapp.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

@Composable
fun UploadScreen() {
    var name by remember { mutableStateOf("") }
    var resultJson by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Subir Producto", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = resultJson,
            onValueChange = { resultJson = it },
            label = { Text("Resultado (JSON)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar Imagen")
        }

        imageUri?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Imagen seleccionada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (imageUri != null && name.isNotBlank() && resultJson.isNotBlank()) {
                    coroutineScope.launch(Dispatchers.IO) {
                        uploading = true
                        val contentResolver = context.contentResolver
                        val inputStream = contentResolver.openInputStream(imageUri!!)
                        val file = File(context.cacheDir, UUID.randomUUID().toString() + ".jpg")
                        file.outputStream().use { inputStream?.copyTo(it) }

                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                        val nameBody = MultipartBody.Part.createFormData("name", name)
                        val jsonBody = MultipartBody.Part.createFormData("result_json", resultJson)
                        val historyIdBody = MultipartBody.Part.createFormData("history_id", "1") // hardcoded por ahora

                        val response = RetrofitInstance.retrofit
                            .create(com.tesis.nutriguideapp.api.ProductService::class.java)
                            .createProduct(nameBody, jsonBody, historyIdBody, imagePart)

                        uploading = false
                    }
                }
            },
            enabled = !uploading
        ) {
            Text(if (uploading) "Subiendo..." else "Subir Producto")
        }
    }
}
