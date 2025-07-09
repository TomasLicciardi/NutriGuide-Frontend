package com.tesis.nutriguideapp.test

import com.tesis.nutriguideapp.model.*

/**
 * Archivo de prueba para verificar la integración con el backend
 */

fun testBackendErrorParsing() {
    // Simular respuesta de error del backend para imagen inválida
    val invalidImageError = BackendErrorResponse(
        detail = BackendErrorDetail(
            error = "invalid_image",
            message = "La imagen no corresponde a una etiqueta nutricional válida",
            instructions = "Toma una foto de la etiqueta nutricional del producto"
        )
    )
    
    println("Error de imagen inválida: ${invalidImageError.detail?.message}")
    
    // Simular respuesta de error para mala calidad
    val poorQualityError = BackendErrorResponse(
        detail = BackendErrorDetail(
            error = "poor_quality", 
            message = "La imagen está borrosa o es difícil de leer",
            instructions = "Mejora la calidad de la imagen: usa mejor iluminación"
        )
    )
    
    println("Error de calidad: ${poorQualityError.detail?.message}")
    
    // 🆕 Simular respuesta de error 422 directo (sin wrapper detail)
    val lowConfidenceErrorDirect = BackendErrorResponse(
        error = "low_confidence",
        message = "Confianza 0.0% (❌) - Umbral normal: 85.0%",
        instructions = "Toma una foto más clara de la etiqueta completa con mejor iluminación y enfoque."
    )
    
    println("Error de confianza directo: ${lowConfidenceErrorDirect.getErrorData().message}")
    println("Instrucciones: ${lowConfidenceErrorDirect.getErrorData().instructions}")
    
    // Verificar que la función helper funciona correctamente
    val errorData = lowConfidenceErrorDirect.getErrorData()
    println("Tipo de error: ${errorData.error}")
    println("Mensaje: ${errorData.message}")
    println("Instrucciones: ${errorData.instructions}")
    
    // Simular análisis exitoso (usando la estructura correcta de ProductAnalysis)
    val successResponse = AnalysisResponse(
        productId = 123,
        suitable = true,
        resultJson = ProductAnalysis(
            ingredientes = "Leche, azúcar, cacao",
            puedeContener = "Frutos secos",
            textoDetectado = "Ingredientes: Leche, azúcar...",
            clasificacion = mapOf(
                "vegano" to Restriction(
                    apto = false, 
                    razon = "Contiene leche",
                    confidence = 0.95f
                )
            )
        ),
        message = "Análisis completado exitosamente"
    )
    
    println("Análisis exitoso: Producto ${successResponse.productId}")
}

fun testAnalysisResultStates() {
    // Probar diferentes estados
    val loadingState = AnalysisResult.Loading
    println("Estado: Loading")
    
    val successState = AnalysisResult.Success(
        AnalysisResponse(
            productId = 1,
            suitable = true,
            resultJson = ProductAnalysis(
                ingredientes = "Harina, agua, sal",
                puedeContener = "Gluten",
                textoDetectado = "Ingredientes: Harina...",
                clasificacion = mapOf(
                    "celiaco" to Restriction(
                        apto = false,
                        razon = "Contiene gluten",
                        confidence = 0.9f
                    )
                )
            ),
            message = "OK"
        )
    )
    println("Estado: Success")
    
    val imageErrorState = AnalysisResult.ImageError(
        errorType = "invalid_image",
        message = "Imagen no válida",
        instructions = "Toma una foto de la etiqueta"
    )
    println("Estado: ImageError - ${imageErrorState.errorType}")
    
    val lowConfidenceState = AnalysisResult.LowConfidenceError(
        message = "Análisis con baja confianza",
        instructions = "Toma una foto más clara"
    )
    println("Estado: LowConfidenceError")
    
    val serverErrorState = AnalysisResult.ServerError()
    println("Estado: ServerError")
    
    val networkErrorState = AnalysisResult.NetworkError(
        message = "Sin conexión a internet"
    )
    println("Estado: NetworkError")
    
    val rateLimitState = AnalysisResult.RateLimitError()
    println("Estado: RateLimitError")
}
