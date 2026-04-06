package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    @SerializedName("user_verdict") val userVerdict: Boolean,
    @SerializedName("restrictions") val restrictions: Map<String, RestrictionDetail> = emptyMap(),
    @SerializedName("ingredients") val ingredients: List<IngredientDetail> = emptyList(),
    @SerializedName("allergen_warnings") val allergenWarnings: String? = null,
    @SerializedName("overall_confidence") val overallConfidence: Float = 0f,
    @SerializedName("processing_time") val processingTime: Float? = null,
    @SerializedName("stats") val stats: Map<String, Int>? = null
)

data class RestrictionDetail(
    @SerializedName("apto") val apto: Boolean,
    @SerializedName("motivo") val motivo: String? = null
)

data class IngredientDetail(
    @SerializedName("name_es") val nameEs: String = "",
    @SerializedName("name_en") val nameEn: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("origin") val origin: String? = null,
    @SerializedName("function_tag") val functionTag: String? = null,
    @SerializedName("description_es") val descriptionEs: String? = null,
    @SerializedName("is_tacc_safe") val isTaccSafe: Boolean? = null,
    @SerializedName("is_lactose_safe") val isLactoseSafe: Boolean? = null,
    @SerializedName("is_nut_safe") val isNutSafe: Boolean? = null,
    @SerializedName("is_vegan_safe") val isVeganSafe: Boolean? = null,
    @SerializedName("confidence") val confidence: Float = 0f,
    @SerializedName("resolved_by") val resolvedBy: String = "unresolved",
    @SerializedName("evidence") val evidence: List<String> = emptyList()
)

data class BackendErrorResponse(
    @SerializedName("detail") val detail: Any? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("instructions") val instructions: String? = null
) {
    fun getErrorData(): BackendErrorDetail {
        if (detail is Map<*, *>) {
            val map = detail as Map<*, *>
            return BackendErrorDetail(
                error = map["error"]?.toString() ?: error ?: "unknown_error",
                message = map["message"]?.toString() ?: message ?: "Error desconocido",
                instructions = map["instructions"]?.toString() ?: instructions ?: "Intenta nuevamente"
            )
        }
        return BackendErrorDetail(
            error = error ?: "unknown_error",
            message = detail?.toString() ?: message ?: "Error desconocido",
            instructions = instructions ?: "Intenta nuevamente"
        )
    }
}

data class BackendErrorDetail(
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
    @SerializedName("instructions") val instructions: String = "Intenta nuevamente"
)

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
