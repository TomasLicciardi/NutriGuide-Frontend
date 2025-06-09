package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("is_suitable") val suitable: Boolean,
    @SerializedName("result_json") val resultJson: ProductAnalysis,
    @SerializedName("message") val message: String? = null
)

data class ProductAnalysis(
    @SerializedName("ingredientes") val ingredientes: String,
    @SerializedName("puede_contener") val puedeContener: String?,
    @SerializedName("texto_detectado") val textoDetectado: String,
    @SerializedName("clasificacion") val clasificacion: Map<String, Restriction>
)

data class Restriction(
    @SerializedName("apto") val apto: Boolean,
    @SerializedName("razon") val razon: String?
)
