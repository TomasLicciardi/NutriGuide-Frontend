package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo simplificado para los elementos de la lista de historial.
 * Contiene solo la información básica que devuelve el endpoint GET /history/
 */
data class HistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("is_suitable") val isSuitable: Boolean
)
