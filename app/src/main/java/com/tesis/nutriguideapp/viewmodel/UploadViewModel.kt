package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.AnalysisService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.AnalysisResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

class UploadViewModel : ViewModel() {
    
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    private val _analyzing = mutableStateOf(false)
    val analyzing: State<Boolean> = _analyzing

    private val _uploading = mutableStateOf(false)
    val uploading: State<Boolean> = _uploading

    private val _analysisResponse = mutableStateOf<AnalysisResponse?>(null)
    val analysisResponse: State<AnalysisResponse?> = _analysisResponse

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
        _analysisResponse.value = null
        _error.value = null
    }

    fun analyzeImage(context: Context) {
        val uri = _imageUri.value ?: run {
            _error.value = "Por favor, selecciona una imagen"
            return
        }

        _analyzing.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Preparar la imagen para enviar
                android.util.Log.d("UploadViewModel", "Preparando imagen para análisis")
                val file = prepareImageFile(context, uri)
                android.util.Log.d("UploadViewModel", "Imagen preparada: ${file.length()} bytes")
                
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Llamada al API para análisis
                android.util.Log.d("UploadViewModel", "Iniciando petición de análisis")
                val service = RetrofitInstance.getAuthenticatedRetrofit(context)
                    .create(AnalysisService::class.java)
                val response = service.analyzeImage(imagePart)
                
                if (response.isSuccessful) {
                    val analysisResult = response.body()
                    _analysisResponse.value = analysisResult
                    android.util.Log.d("UploadViewModel", "Análisis exitoso: ${analysisResult?.productId}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Error al analizar la imagen: ${response.code()} - ${response.message()}\n$errorBody"
                    android.util.Log.e("UploadViewModel", "Error HTTP al analizar imagen: ${response.code()} - ${response.message()}")
                    android.util.Log.e("UploadViewModel", "Error body: $errorBody")
                }
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado. La petición tardó demasiado en completarse."
                android.util.Log.e("UploadViewModel", "Timeout al analizar imagen", e)
            } catch (e: java.io.IOException) {
                _error.value = "Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}"
                android.util.Log.e("UploadViewModel", "Error de IO al analizar imagen", e)
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
                android.util.Log.e("UploadViewModel", "Error general al analizar imagen", e)
            } finally {
                _analyzing.value = false
            }
        }
    }

    fun uploadProduct(context: Context, onSuccess: () -> Unit) {
        // Como el backend ya guarda el producto durante el análisis,
        // solo necesitamos navegar de vuelta al home
        onSuccess()
        resetForm()
    }
    
    private fun prepareImageFile(context: Context, uri: Uri): File {
        // Intentar optimizar la imagen primero
        val optimizedFile = com.tesis.nutriguideapp.utils.ImageOptimizer.optimizeImage(context, uri)
        
        // Si la optimización fue exitosa, usar el archivo optimizado
        if (optimizedFile != null) {
            android.util.Log.d("UploadViewModel", "Imagen optimizada: ${optimizedFile.length() / 1024} KB")
            return optimizedFile
        }
        
        // Si la optimización falló, usar el método original
        android.util.Log.d("UploadViewModel", "La optimización falló, usando el método original")
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, UUID.randomUUID().toString() + ".jpg")
        file.outputStream().use { inputStream?.copyTo(it) }
        android.util.Log.d("UploadViewModel", "Imagen original: ${file.length() / 1024} KB")
        return file
    }
    
    // Resetear el formulario
    private fun resetForm() {
        _imageUri.value = null
        _analysisResponse.value = null
        _analyzing.value = false
        _uploading.value = false
        _error.value = null
    }
}