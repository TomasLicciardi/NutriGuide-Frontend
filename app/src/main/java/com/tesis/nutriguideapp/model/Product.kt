package com.tesis.nutriguideapp.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("result_json") val resultJson: String,
    @SerializedName("date") val date: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_suitable") val isSuitable: Boolean = false
) {
    // Método para parsear el resultado JSON
    fun getAnalysisResults(): Map<String, Any> {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(resultJson, type)
    }
    
    // Método para obtener ingredientes
    fun getIngredients(): List<String> {
        val results = getAnalysisResults()
        return (results["ingredients"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }
    
    // Método para obtener restricciones detectadas
    fun getRestrictionsDetected(): List<String> {
        val results = getAnalysisResults()
        return (results["restrictions_detected"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }
    
    // Método para obtener el texto detectado en la imagen
    fun getTextDetected(): String {
        val results = getAnalysisResults()
        return (results["text_detected"] as? String) ?: ""
    }
}
