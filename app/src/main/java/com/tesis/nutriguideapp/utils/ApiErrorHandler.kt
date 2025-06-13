package com.tesis.nutriguideapp.utils

import android.util.Log
import okio.Buffer
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Utilidad para manejar errores de API y proporcionar mensajes de error amigables
 */
object ApiErrorHandler {
    
    /**
     * Procesa un error HTTP y devuelve un mensaje de error amigable
     */    fun getHttpErrorMessage(code: Int, defaultMessage: String? = null): String {
        return when(code) {
            400 -> "Solicitud incorrecta. Por favor, revisa los datos enviados."
            401 -> "No autorizado. Email o contraseña incorrectos."
            403 -> "Acceso prohibido. No tienes permisos para realizar esta acción."
            404 -> "Recurso no encontrado."
            409 -> "Conflicto. Este recurso ya existe."
            422 -> "Los datos enviados no son válidos. Por favor, verifica los campos."
            500 -> "Error interno del servidor. Por favor, intenta más tarde."
            502 -> "Error de comunicación con el servidor."
            503 -> "Servicio no disponible temporalmente."
            504 -> "Tiempo de espera agotado. El servidor está tardando demasiado."
            else -> defaultMessage ?: "Error desconocido (código $code)"
        }
    }
    
    /**
     * Devuelve un mensaje específico según el contexto para errores de validación (422)
     */
    fun getValidationErrorMessage(tag: String): String {
        return when {
            tag.contains("LoginViewModel", ignoreCase = true) -> 
                "Email o contraseña incorrectos. Por favor, verifica tus datos."
            tag.contains("RegisterViewModel", ignoreCase = true) -> 
                "Los datos de registro no son válidos. Verifica todos los campos."
            tag.contains("EditProfileViewModel", ignoreCase = true) -> 
                "La contraseña actual es incorrecta."
            tag.contains("ForgotPasswordViewModel", ignoreCase = true) -> 
                "El formato del correo electrónico no es válido."
            tag.contains("ResetPasswordViewModel", ignoreCase = true) -> 
                "La nueva contraseña no cumple con los requisitos de seguridad."
            tag.contains("RestriccionesViewModel", ignoreCase = true) -> 
                "No se pudieron actualizar las restricciones. Verifica los datos."
            tag.contains("HomeViewModel", ignoreCase = true) -> 
                "Error al cargar los datos del perfil. Por favor intenta de nuevo."
            tag.contains("ProfileViewModel", ignoreCase = true) -> 
                "Error al cargar la información del perfil. Por favor intenta de nuevo."
            else -> 
                "Los datos enviados no son válidos. Por favor, verifica los campos."
        }
    }
      /**
     * Método de prueba para verificar que el manejo de errores funciona correctamente
     */
    fun testErrorHandling(errorType: String): String {
        return when (errorType.lowercase()) {
            "401", "unauthorized" -> getHttpErrorMessage(401)
            "404", "notfound" -> getHttpErrorMessage(404)
            "422", "validation" -> getValidationErrorMessage("LoginViewModel")
            "timeout" -> handleApiError(SocketTimeoutException("Timeout de prueba"), "TestErrorHandling")
            "connection" -> handleApiError(IOException("Error de conexión de prueba"), "TestErrorHandling")
            "json" -> handleApiError(com.google.gson.JsonSyntaxException("Error JSON de prueba"), "TestErrorHandling")
            "http" -> handleApiError(retrofit2.HttpException(okhttp3.ResponseBody.create(null, "{}").let {
                retrofit2.Response.error<Any>(500, it)
            }), "TestErrorHandling")
            else -> "Tipo de error desconocido para prueba: $errorType"
        }
    }
    
    /**
     * Analiza un cuerpo de error JSON y extrae mensajes útiles
     */
    fun parseErrorBody(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        
        return try {
            val jsonObject = org.json.JSONObject(errorBody)
            
            // Varios formatos comunes de mensajes de error
            jsonObject.optString("detail", null) ?: 
            jsonObject.optString("message", null) ?:
            jsonObject.optString("error", null) ?:
            jsonObject.optString("error_description", null) ?:
            
            // Buscar errores de validación en formato de FastAPI
            if (jsonObject.has("detail") && jsonObject.get("detail") is org.json.JSONArray) {
                val detailArray = jsonObject.getJSONArray("detail")
                val errorBuilder = StringBuilder()
                
                for (i in 0 until detailArray.length()) {
                    val errorItem = detailArray.getJSONObject(i)
                    val msg = errorItem.optString("msg", "")
                    val loc = errorItem.optJSONArray("loc")
                    
                    if (loc != null && loc.length() > 0) {
                        val field = loc.getString(loc.length() - 1)
                        errorBuilder.append("$field: $msg. ")
                    } else {
                        errorBuilder.append("$msg. ")
                    }
                }
                
                errorBuilder.toString().trim()
            } else null
            
        } catch (e: Exception) {
            Log.e("ApiErrorHandler", "Error al parsear cuerpo JSON", e)
            null
        }
    }
      /**
     * Procesa cualquier excepción de red y devuelve un mensaje de error amigable
     */    fun handleApiError(throwable: Throwable, tag: String = "ApiError"): String {
        Log.e(tag, "Error en API: ${throwable.javaClass.simpleName} - ${throwable.message}", throwable)
        
        return when (throwable) {
            is HttpException -> {
                try {
                    val errorCode = throwable.code()
                    
                    // Enfoque seguro para leer el errorBody
                    val errorBody = try {
                        val responseErrorBody = throwable.response()?.errorBody()
                        if (responseErrorBody != null) {
                            try {
                                // Copiar el contenido antes de usarlo
                                val source = responseErrorBody.source()
                                source.request(Long.MAX_VALUE) // Buffer the entire body
                                val buffer = source.buffer.clone()
                                buffer.readUtf8()
                            } catch (e: Exception) {
                                Log.e(tag, "Error al leer el error body en HttpException", e)
                                null
                            }
                        } else null
                    } catch (e: Exception) {
                        Log.e(tag, "Error al acceder al errorBody en HttpException", e)
                        null
                    }
                      
                    // Intentar extraer el mensaje de error del body JSON si es posible
                    val extractedErrorMessage = try {
                        parseErrorBody(errorBody)
                    } catch (e: Exception) {
                        Log.e(tag, "Error al parsear respuesta de error", e)
                        null
                    }
                    
                    extractedErrorMessage ?: getHttpErrorMessage(errorCode, throwable.message())
                } catch (e: Exception) {
                    Log.e(tag, "Error al procesar HttpException", e)
                    "Error de comunicación con el servidor: ${throwable.message ?: "desconocido"}"
                }
            }
            is SocketTimeoutException -> "Tiempo de espera agotado. Inténtalo de nuevo más tarde."
            is IOException -> {
                if (throwable.cause is IllegalStateException && throwable.cause?.message?.contains("closed") == true) {
                    "Error de conexión: La conexión se cerró inesperadamente. Intenta de nuevo."
                } else {
                    "Error de conexión. Verifica tu conexión a internet."
                }
            }
            is IllegalStateException -> {
                if (throwable.message?.contains("closed") == true) {
                    "Error de conexión: La conexión se cerró inesperadamente. Intenta de nuevo."
                } else {
                    "Error interno de la aplicación: ${throwable.message ?: "desconocido"}"
                }
            }
            is NullPointerException -> "Error interno de la aplicación: Datos no disponibles"
            is com.google.gson.JsonSyntaxException -> "Error al procesar la respuesta del servidor."
            is com.google.gson.JsonIOException -> "Error en la comunicación con el servidor."
            is kotlin.UninitializedPropertyAccessException -> "Error interno: Datos no inicializados correctamente."
            else -> "Error inesperado: ${throwable.message ?: "desconocido"}"
        }
    }
      /**
     * Procesa la respuesta de la API y ejecuta acciones según el resultado
     */    
    suspend fun <T> processResponse(
        response: Response<T>,
        tag: String = "ApiResponse",
        onSuccess: suspend (T) -> Unit,
        onError: suspend (String) -> Unit
    ) {
        try {
            if (response.isSuccessful && response.body() != null) {                try {
                    // Para respuestas exitosas, procesamos el cuerpo directamente
                    Log.d(tag, "Respuesta exitosa (${response.code()})")
                    val body = response.body()
                    
                    // Punto crítico: Verificamos que el cuerpo no sea nulo antes de procesarlo
                    if (body != null) {
                        try {
                            // Intentamos procesar el cuerpo
                            onSuccess(body)
                        } catch (e: IllegalStateException) {
                            // Si hay un error "closed", esto puede indicar que el interceptor ya leyó el cuerpo
                            // pero la respuesta todavía es exitosa
                            Log.e(tag, "Error al procesar cuerpo de respuesta exitosa", e)
                            
                            if (e.message?.contains("closed") == true) {
                                // IMPORTANTE: En respuestas 200 OK con error "closed", 
                                // asumimos que es un login exitoso y permitimos continuar
                                if (response.code() == 200 && tag.contains("LoginViewModel", ignoreCase = true)) {
                                    Log.d(tag, "Respuesta de login exitosa (200 OK), continuando a pesar del error 'closed'")
                                    
                                    // Podríamos modificar esto para que extraiga datos específicos 
                                    // según el endpoint
                                    onSuccess(body)
                                } else {
                                    Log.w(tag, "Respuesta exitosa pero cuerpo cerrado - Código ${response.code()}")
                                    onError("La operación parece haber tenido éxito, pero ocurrió un error al procesar la respuesta.")
                                }
                            } else {
                                onError("Error al procesar respuesta exitosa: ${e.message ?: "desconocido"}")
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error general al procesar respuesta exitosa", e)
                            onError("Error al procesar los datos: ${e.message ?: "desconocido"}")
                        }
                    } else {
                        Log.e(tag, "Respuesta exitosa (${response.code()}) pero cuerpo nulo")
                        onError("Error al procesar los datos: Cuerpo de respuesta vacío")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error al procesar respuesta exitosa", e)
                    onError("Error al procesar los datos: ${e.message ?: "desconocido"}")
                }
            } else {
                // Log detallado del error para facilitar la depuración
                val errorCode = response.code()
                
                // IMPORTANTE: Usamos un enfoque más seguro para leer el errorBody
                var errorBodyString = "No hay cuerpo de error"
                try {
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        try {
                            // No podemos usar peekBody directamente, usamos una técnica segura
                            // para clonar el buffer y leer sin consumir
                            val source = errorBody.source()
                            source.request(Long.MAX_VALUE) // Buffer the entire body
                            val buffer = source.buffer.clone()
                            errorBodyString = buffer.readUtf8()
                        } catch (e: Exception) {
                            Log.e(tag, "Error al clonar buffer", e)
                            
                            // Si falla el método anterior, intentamos leer directamente
                            // (esto podría consumir el cuerpo)
                            try {
                                errorBodyString = errorBody.string()
                            } catch (e2: Exception) {
                                Log.e(tag, "No se pudo leer el cuerpo de error", e2)
                                errorBodyString = "Error de lectura: ${e2.message}"
                            }
                                
                                // Último intento: directo (puede fallar con "closed")
                                try {
                                    errorBodyString = errorBody.string()
                                } catch (e3: Exception) {
                                    Log.e(tag, "No se pudo leer el cuerpo de error", e3)
                                    errorBodyString = "Error de lectura: ${e3.message}"
                                }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error al acceder al cuerpo de error", e)
                }
                
                Log.e(tag, "Error en respuesta HTTP: Código=$errorCode, Mensaje=${response.message()}, Body=$errorBodyString")
                  
                // Intentar extraer el mensaje de error del body si es posible (formato JSON)
                val extractedErrorMessage = try {
                    parseErrorBody(errorBodyString) 
                } catch (e: Exception) {
                    Log.e(tag, "Error al parsear el cuerpo de error", e)
                    null
                }
                
                // Personalizar mensaje para errores de validación según el contexto
                val errorMsg = when {
                    extractedErrorMessage != null -> extractedErrorMessage
                    errorCode == 401 -> "Email o contraseña incorrectos. Por favor, verifica tus datos."
                    errorCode == 422 -> getValidationErrorMessage(tag)
                    else -> getHttpErrorMessage(errorCode, response.message())
                }
                
                Log.e(tag, "Error de respuesta: $errorCode - $errorMsg")
                
                // En caso de error 401 (no autorizado), verificar si es en un viewmodel que no sea LoginViewModel
                if (errorCode == 401 && !tag.contains("LoginViewModel", ignoreCase = true)) {
                    // Podríamos lanzar un evento para navegar al login, pero lo dejamos para el ViewModel
                    Log.w(tag, "Error de autenticación, posiblemente sesión expirada")
                }
                
                try {
                    onError(errorMsg)
                } catch (e: Exception) {
                    Log.e(tag, "Error al invocar callback onError", e)
                }
            }
        } catch (e: Exception) {
            // Capturar cualquier excepción que pueda ocurrir durante el procesamiento
            Log.e(tag, "Excepción inesperada al procesar respuesta", e)
            try {
                when (e) {
                    is IllegalStateException -> {
                        if (e.message?.contains("closed") == true) {
                            onError("Error de conexión: Se cerró la conexión. Por favor, intenta de nuevo.")
                        } else {
                            onError("Error interno: ${e.message ?: "desconocido"}")
                        }
                    }
                    else -> onError("Error inesperado: ${e.message ?: "desconocido"}")
                }
            } catch (innerE: Exception) {
                Log.e(tag, "Error secundario al procesar error", innerE)
            }
        }
    }
    
    /**
     * Método auxiliar para manejar respuestas de Retrofit de forma segura
     * específicamente diseñado para evitar problemas con el cuerpo cerrado
     */
    private fun <T> safeHandleResponse(
        response: Response<T>,
        tag: String = "ApiResponse",
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (!response.isSuccessful) {
                val errorMsg = getHttpErrorMessage(response.code(), response.message())
                Log.e(tag, "Error en respuesta: ${response.code()} - $errorMsg")
                onError(errorMsg)
                return
            }
            
            val body = response.body()
            if (body == null) {
                Log.e(tag, "Respuesta exitosa (${response.code()}) pero cuerpo nulo")
                onError("Error: Cuerpo de respuesta vacío")
                return
            }
            
            try {
                onSuccess(body)
            } catch (e: IllegalStateException) {
                // Específicamente para los errores "closed"
                if (e.message?.contains("closed") == true) {
                    Log.w(tag, "Respuesta exitosa pero cuerpo cerrado - Código ${response.code()}")
                    // En este caso, sabemos que la respuesta fue exitosa, así que intentamos
                    // continuar con el flujo exitoso a pesar del error
                    if (tag.contains("Login", ignoreCase = true) || tag.contains("Auth", ignoreCase = true)) {
                        Log.d(tag, "Operación de autenticación exitosa a pesar del error 'closed'")
                        // Aquí podemos intentar un flujo específico para login
                    } else {
                        onError("La operación parece haber tenido éxito, pero hubo un error al procesar la respuesta.")
                    }
                } else {
                    Log.e(tag, "Error al procesar respuesta exitosa", e)
                    onError("Error interno: ${e.message ?: "desconocido"}")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error al procesar respuesta exitosa", e)
                onError("Error al procesar la respuesta: ${e.message ?: "desconocido"}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error general al manejar respuesta", e)
            onError("Error inesperado: ${e.message ?: "desconocido"}")
        }
    }
}
