package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("is_suitable") val suitable: Boolean,
    @SerializedName("result_json") val resultJson: ProductAnalysis,
    @SerializedName("message") val message: String? = null
)

// 🆕 MODELO PARA ERRORES DEL BACKEND
// Estructura principal que puede tener 'detail' anidado o directo
data class BackendErrorResponse(
    @SerializedName("detail") val detail: BackendErrorDetail? = null,
    // Campos directos para compatibilidad
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("instructions") val instructions: String? = null
) {
    // Función helper para obtener los datos de error de forma unificada
    fun getErrorData(): BackendErrorDetail {
        return detail ?: BackendErrorDetail(
            error = error ?: "unknown_error",
            message = message ?: "Error desconocido",
            instructions = instructions ?: "Intenta nuevamente"
        )
    }
}

data class BackendErrorDetail(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
    @SerializedName("instructions") val instructions: String
)

// 🆕 SEALED CLASS PARA ESTADOS DE ANÁLISIS
sealed class AnalysisResult {
    object Loading : AnalysisResult()
    data class Success(val response: AnalysisResponse) : AnalysisResult()
    data class ImageError(
        val errorType: String,
        val message: String,
        val instructions: String
    ) : AnalysisResult()
    data class LowConfidenceError(
        val message: String,
        val instructions: String
    ) : AnalysisResult()
    data class RateLimitError(
        val message: String = "Demasiadas solicitudes",
        val instructions: String = "Espera unos minutos antes de intentar nuevamente"
    ) : AnalysisResult()
    data class ServerError(
        val message: String = "Error del servidor",
        val instructions: String = "Intenta nuevamente en unos momentos"
    ) : AnalysisResult()
    data class NetworkError(
        val message: String,
        val instructions: String = "Verifica tu conexión a internet"
    ) : AnalysisResult()
}
