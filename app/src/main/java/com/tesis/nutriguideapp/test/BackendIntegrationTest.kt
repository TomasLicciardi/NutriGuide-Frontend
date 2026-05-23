package com.tesis.nutriguideapp.test

import com.tesis.nutriguideapp.model.*

fun testBackendErrorParsing() {
    val lowConfidenceErrorDirect = BackendErrorResponse(
        error = "low_confidence",
        message = "Confianza 0.0% - Umbral normal: 85.0%",
        instructions = "Toma una foto más clara de la etiqueta completa con mejor iluminación y enfoque."
    )

    println("Error de confianza directo: ${lowConfidenceErrorDirect.getErrorData().message}")
    println("Instrucciones: ${lowConfidenceErrorDirect.getErrorData().instructions}")

    val errorData = lowConfidenceErrorDirect.getErrorData()
    println("Tipo de error: ${errorData.error}")
    println("Mensaje: ${errorData.message}")
    println("Instrucciones: ${errorData.instructions}")

    val successResponse = AnalysisResponse(
        userVerdict = true,
        restrictions = mapOf(
            "sin_tacc" to RestrictionDetail(
                apto = true, motivo = null, fuente = "ingredient_analysis", confidence = 0.95f
            ),
            "vegano" to RestrictionDetail(
                apto = false, motivo = "Contiene leche", fuente = "legal_declaration",
                confidence = 0.99f, ingredienteDisparador = "leche"
            )
        ),
        ingredients = listOf(
            IngredientDetail(
                nameEs = "Leche", nameEn = "Milk", category = "BASE",
                origin = "animal", confidence = 0.95f,
                allergens = listOf("milk"), sources = listOf("codex_ins")
            ),
            IngredientDetail(
                nameEs = "Azúcar", nameEn = "Sugar", category = "BASE",
                origin = "plant", confidence = 0.90f,
                sources = listOf("kb_cache")
            )
        ),
        declaration = LegalDeclaration(contains = listOf("milk")),
        overallConfidence = 0.92f
    )

    println("Análisis exitoso: verdict=${successResponse.userVerdict}, confidence=${successResponse.overallConfidence}")
}

fun testAnalysisResultStates() {
    val loadingState = AnalysisResult.Loading
    println("Estado: Loading")

    val successState = AnalysisResult.Success(
        AnalysisResponse(
            userVerdict = false,
            restrictions = mapOf(
                "sin_tacc" to RestrictionDetail(
                    apto = false, motivo = "Contiene gluten",
                    fuente = "legal_declaration", confidence = 0.99f
                )
            ),
            ingredients = listOf(
                IngredientDetail(
                    nameEs = "Harina", nameEn = "Flour", category = "BASE",
                    origin = "plant", confidence = 0.9f
                )
            ),
            overallConfidence = 0.88f
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
