package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

/**
 * Schema del endpoint /analysis/.
 *
 * Pipeline fact base / rule base — los predicados operan sobre IngredientFacts
 * con trazabilidad de fuente por tag.
 */
data class AnalysisResponse(
    @SerializedName("user_verdict") val userVerdict: Boolean,
    @SerializedName("restrictions") val restrictions: Map<String, RestrictionDetail> = emptyMap(),
    @SerializedName("ingredients") val ingredients: List<IngredientDetail> = emptyList(),
    @SerializedName("declaration") val declaration: LegalDeclaration = LegalDeclaration(),
    @SerializedName("overall_confidence") val overallConfidence: Float = 0f,
    @SerializedName("stats") val stats: AnalysisStats? = null
)

data class RestrictionDetail(
    @SerializedName("apto") val apto: Boolean,
    @SerializedName("motivo") val motivo: String? = null,
    @SerializedName("fuente") val fuente: String = "ingredient_analysis",
    @SerializedName("confidence") val confidence: Float = 0f,
    @SerializedName("ingrediente_disparador") val ingredienteDisparador: String? = null
)

data class IngredientDetail(
    @SerializedName("name_es") val nameEs: String = "",
    @SerializedName("name_en") val nameEn: String? = null,
    @SerializedName("category") val category: String = "",
    @SerializedName("origin") val origin: String = "unknown",
    @SerializedName("function_tag") val functionTag: String? = null,
    @SerializedName("codex_ins_code") val codexInsCode: Int? = null,
    @SerializedName("codex_ins_subcode") val codexInsSubcode: String? = null,
    @SerializedName("is_flavoring") val isFlavoring: Boolean = false,
    @SerializedName("flavoring_type") val flavoringType: String? = null,
    @SerializedName("target_sensory") val targetSensory: String? = null,
    @SerializedName("allergens") val allergens: List<String> = emptyList(),
    @SerializedName("contains") val contains: List<String> = emptyList(),
    @SerializedName("derived_from") val derivedFrom: List<String> = emptyList(),
    @SerializedName("confidence") val confidence: Float = 0f,
    @SerializedName("sources") val sources: List<String> = emptyList(),
    @SerializedName("description_es") val descriptionEs: String? = null
)

data class LegalDeclaration(
    @SerializedName("contains") val contains: List<String> = emptyList(),
    @SerializedName("may_contain") val mayContain: List<String> = emptyList(),
    @SerializedName("positive_claims") val positiveClaims: List<String> = emptyList(),
    @SerializedName("raw_text") val rawText: String? = null
)

data class AnalysisStats(
    @SerializedName("total_ingredients") val totalIngredients: Int = 0,
    @SerializedName("total_flavorings") val totalFlavorings: Int = 0,
    @SerializedName("resolved_by_legal") val resolvedByLegal: Int = 0,
    @SerializedName("resolved_by_codex") val resolvedByCodex: Int = 0,
    @SerializedName("resolved_by_off") val resolvedByOff: Int = 0,
    @SerializedName("resolved_by_kb") val resolvedByKb: Int = 0,
    @SerializedName("resolved_by_gemini") val resolvedByGemini: Int = 0,
    @SerializedName("resolved_by_llm") val resolvedByLlm: Int = 0,
    @SerializedName("resolved_by_policy") val resolvedByPolicy: Int = 0,
    @SerializedName("unresolved") val unresolved: Int = 0,
    @SerializedName("gemini_calls") val geminiCalls: Int = 0,
    @SerializedName("processing_time_ms") val processingTimeMs: Float = 0f
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
