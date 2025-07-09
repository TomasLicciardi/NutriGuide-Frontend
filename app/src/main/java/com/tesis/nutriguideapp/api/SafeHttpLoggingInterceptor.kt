package com.tesis.nutriguideapp.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Un interceptor HTTP que registra información sobre las solicitudes y respuestas HTTP,
 * pero que maneja de forma segura las respuestas de error para evitar que se cierre la conexión.
 */
class SafeHttpLoggingInterceptor : Interceptor {
    private val TAG = "SafeHttpLogging"
    private val logger = HttpLoggingInterceptor.Logger.DEFAULT

    /**
     * Nivel de logging
     */
    enum class Level {
        NONE, BASIC, HEADERS, BODY
    }

    var level = Level.NONE

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // No loggear si el nivel es NONE
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        // Registrar información de la solicitud
        val logBuilder = StringBuilder()
        logBuilder.append("--> ${request.method} ${request.url}")
        
        val connection = chain.connection()
        logBuilder.append(" ${connection?.protocol() ?: ""}")
        logger.log(logBuilder.toString())

        val requestTime = System.nanoTime()
        var response: Response
        
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- ERROR: ${e.message}")
            throw e
        }
        
        val responseTime = System.nanoTime()
        val tookMs = TimeUnit.NANOSECONDS.toMillis(responseTime - requestTime)

        val responseBody = response.body
        val contentLength = responseBody?.contentLength() ?: 0
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        
        logBuilder.clear()
        logBuilder.append("<-- ${response.code} ${response.message} ${response.request.url} ($tookMs ms, $bodySize)")
        logger.log(logBuilder.toString())

        // CLAVE: Para respuestas exitosas, NO MODIFICAMOS la respuesta
        if (level == Level.BODY && responseBody != null) {
            // Para respuestas EXITOSAS (2xx), solo logeamos sin modificar
            if (response.isSuccessful) {
                try {
                    // Usamos peekBody que no consume el cuerpo
                    val peekBody = response.peekBody(Long.MAX_VALUE)
                    logger.log("")
                    logger.log(peekBody.string())
                    
                    // IMPORTANTE: Retornamos la respuesta original sin modificarla
                    return response
                } catch (e: Exception) {
                    Log.e(TAG, "Error al leer cuerpo exitoso: ${e.message}", e)
                    // En caso de error, retornamos la respuesta original
                    return response
                }
            } else {
                // Para respuestas de ERROR, intentamos reconstruir el cuerpo
                try {
                    // Intentamos usar peekBody primero (no consume el cuerpo)
                    val errorBodyString = try {
                        val peekBody = response.peekBody(Long.MAX_VALUE)
                        peekBody.string()
                    } catch (e: Exception) {
                        // Si falla, intentamos leer el cuerpo directamente
                        try {
                            responseBody.string()
                        } catch (e2: Exception) {
                            Log.e(TAG, "No se pudo leer el cuerpo de error: ${e2.message}", e2)
                            null
                        }
                    }
                    
                    if (errorBodyString != null) {
                        logger.log("")
                        logger.log("ERROR BODY: $errorBodyString")
                        
                        // Reconstruimos el cuerpo para evitar problemas
                        try {
                            val newErrorBody = ResponseBody.create(
                                responseBody.contentType(), 
                                errorBodyString
                            )
                            
                            response = response.newBuilder()
                                .body(newErrorBody)
                                .build()
                                
                            Log.d(TAG, "Reconstruido cuerpo de error ${response.code}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al reconstruir cuerpo: ${e.message}", e)
                        }
                    } else {
                        // Si no podemos leer el cuerpo, creamos uno genérico
                        logger.log("ERROR (cuerpo no leído): Código ${response.code}")
                        
                        try {
                            // 🆕 Crear un error más específico según el código de estado
                            val errorJson = when (response.code) {
                                422 -> """{"error":"low_confidence","message":"Análisis con baja confianza","instructions":"Toma una foto más clara de la etiqueta completa"}"""
                                400 -> """{"error":"invalid_image","message":"Imagen no válida","instructions":"Toma una foto de la etiqueta nutricional del producto"}"""
                                else -> """{"error":"server_error","message":"Error del servidor","instructions":"Intenta nuevamente en unos momentos"}"""
                            }
                            
                            val emptyBody = ResponseBody.create(
                                "application/json".toMediaTypeOrNull(),
                                errorJson
                            )
                            
                            response = response.newBuilder()
                                .body(emptyBody)
                                .build()
                                
                            Log.d(TAG, "Creado cuerpo específico para error ${response.code}: $errorJson")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al crear cuerpo específico: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar respuesta de error: ${e.message}", e)
                    // Si todo falla, al menos registramos el error
                }
            }
        }

        return response
    }
}

/**
 * Método auxiliar que crea un interceptor que maneja de forma segura las respuestas
 * Usar este método en lugar de crear el interceptor directamente
 */
fun createSafeLoggingInterceptor(level: SafeHttpLoggingInterceptor.Level = SafeHttpLoggingInterceptor.Level.BODY): SafeHttpLoggingInterceptor {
    return SafeHttpLoggingInterceptor().apply {
        this.level = level
    }
}
