package com.tesis.nutriguideapp.test

import com.google.gson.Gson
import com.tesis.nutriguideapp.model.BackendErrorResponse

/**
 * Test simple para verificar parsing de errores del backend
 */
fun main() {
    val gson = Gson()
    
    // Simular la respuesta exacta que debería enviar el backend para el error 422
    val backendResponse422 = """
    {
        "error": "low_confidence",
        "message": "Confianza 0.0% (❌) - Umbral normal: 85.0%",
        "instructions": "Toma una foto más clara de la etiqueta completa con mejor iluminación y enfoque."
    }
    """.trimIndent()
    
    println("JSON del backend:")
    println(backendResponse422)
    println()
    
    try {
        val errorResponse = gson.fromJson(backendResponse422, BackendErrorResponse::class.java)
        println("Parseo exitoso!")
        println("Error response: $errorResponse")
        
        val errorData = errorResponse.getErrorData()
        println("Error data:")
        println("  - Error: ${errorData.error}")
        println("  - Message: ${errorData.message}")
        println("  - Instructions: ${errorData.instructions}")
        
    } catch (e: Exception) {
        println("Error al parsear: ${e.message}")
        e.printStackTrace()
    }
}
