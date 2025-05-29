package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("suitable") val suitable: Boolean,
    @SerializedName("restrictions_detected") val restrictionsDetected: List<String>,
    @SerializedName("text_detected") val textDetected: String,
    @SerializedName("analysis_details") val analysisDetails: Map<String, Any>
)
