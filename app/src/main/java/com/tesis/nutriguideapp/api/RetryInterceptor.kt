package com.tesis.nutriguideapp.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor que implementa una política de reintentos para peticiones fallidas.
 * Útil para manejar errores temporales de red o problemas de conectividad.
 */
class RetryInterceptor : Interceptor {
    
    companion object {
        private const val MAX_RETRIES = 3
        private const val TAG = "RetryInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        // Intentar la petición hasta MAX_RETRIES veces
        for (attempt in 1..MAX_RETRIES) {
            try {
                // Si no es el primer intento, esperar antes de reintentar
                if (attempt > 1) {
                    val waitTime = calculateWaitTime(attempt)
                    Log.d(TAG, "Reintentando petición a ${request.url} (intento $attempt/$MAX_RETRIES) después de $waitTime ms")
                    Thread.sleep(waitTime)
                }
                
                // Ejecutar la petición
                response = chain.proceed(request.newBuilder().build())
                
                // Si la petición fue exitosa, retornar la respuesta
                if (response.isSuccessful) {
                    return response
                } else {
                    // Cerrar el cuerpo de la respuesta para liberar recursos
                    response.close()
                    
                    // Solo reintentar en errores del servidor (códigos 5xx)
                    if (response.code >= 500) {
                        Log.d(TAG, "Error ${response.code} en petición a ${request.url}, reintentando ($attempt/$MAX_RETRIES)")
                        continue
                    } else {
                        // Para otros códigos de error, no reintentar
                        return response
                    }
                }
            } catch (e: IOException) {
                // Guardar la excepción para lanzarla si se agotan los reintentos
                exception = e
                Log.e(TAG, "Error de red en intento $attempt/$MAX_RETRIES para ${request.url}: ${e.message}")
            }
        }
        
        // Si llegamos aquí, todos los intentos han fallado
        throw exception ?: IOException("Error desconocido después de $MAX_RETRIES intentos")
    }
    
    /**
     * Calcula el tiempo de espera usando backoff exponencial.
     * Cada reintento espera más tiempo que el anterior.
     */
    private fun calculateWaitTime(attempt: Int): Long {
        // Backoff exponencial con jitter: 2^attempt * 100ms + random(0-100ms)
        return (Math.pow(2.0, attempt.toDouble()).toLong() * 100) + (0..100).random()
    }
}
