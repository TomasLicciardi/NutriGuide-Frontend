package com.tesis.nutriguideapp.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("result_json") val resultJson: ProductAnalysis,
    @SerializedName("date") val date: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_type") val imageType: String?,
    @SerializedName("is_suitable") val isSuitable: Boolean = false
) {
    // Método para obtener ingredientes usando la nueva estructura
    fun getIngredients(): List<String> {
        return resultJson.ingredientes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    
    // Método para obtener restricciones detectadas usando la nueva estructura
    fun getRestrictionsDetected(): List<Restriction> {
        return resultJson.clasificacion.values.toList()
    }
    
    // Método para obtener el texto detectado en la imagen
    fun getTextDetected(): String {
        return resultJson.textoDetectado
    }
}

// Deserializador personalizado para ProductAnalysis
class ProductAnalysisDeserializer : JsonDeserializer<ProductAnalysis> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProductAnalysis {
        val jsonObject = json.asJsonObject
        
        // Extraer campos con valores por defecto si no existen
        val ingredientes = jsonObject.get("ingredientes")?.asString ?: ""
        val puedeContener = jsonObject.get("puede_contener")?.asString
        val textoDetectado = jsonObject.get("texto_detectado")?.asString ?: ""
        
        // Extraer clasificacion - puede ser un objeto vacío
        val clasificacionMap = mutableMapOf<String, Restriction>()
        jsonObject.get("clasificacion")?.asJsonObject?.let { clasificacionObj ->
            clasificacionObj.entrySet().forEach { entry ->
                val restrictionObj = entry.value.asJsonObject
                val apto = restrictionObj.get("apto")?.asBoolean ?: true
                val razon = restrictionObj.get("razon")?.asString
                clasificacionMap[entry.key] = Restriction(apto, razon)
            }
        }
        
        return ProductAnalysis(
            ingredientes = ingredientes,
            puedeContener = puedeContener,
            textoDetectado = textoDetectado,
            clasificacion = clasificacionMap
        )
    }
}
