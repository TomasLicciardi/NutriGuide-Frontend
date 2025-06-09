package com.tesis.nutriguideapp.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
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
        try {
            android.util.Log.d("ProductAnalysisDeserializer", "Deserializando JSON: $json")
              if (json.isJsonNull) {
                android.util.Log.e("ProductAnalysisDeserializer", "JSON es null")
                throw JsonParseException("JSON es null")
            }
            
            val jsonObject = json.asJsonObject
            
            // Extraer campos con valores por defecto si no existen
            val ingredientes = jsonObject.get("ingredientes")?.asString ?: ""
            val puedeContener = jsonObject.get("puede_contener")?.asString
            val textoDetectado = jsonObject.get("texto_detectado")?.asString ?: ""
            
            android.util.Log.d("ProductAnalysisDeserializer", "Campos básicos extraídos - ingredientes: ${ingredientes.take(50)}, textoDetectado: ${textoDetectado.take(50)}")
            
            // Extraer clasificacion - puede ser un objeto vacío
            val clasificacionMap = mutableMapOf<String, Restriction>()
            jsonObject.get("clasificacion")?.asJsonObject?.let { clasificacionObj ->
                clasificacionObj.entrySet().forEach { entry ->
                    try {
                        val restrictionObj = entry.value.asJsonObject
                        val apto = restrictionObj.get("apto")?.asBoolean ?: true
                        val razon = restrictionObj.get("razon")?.asString
                        clasificacionMap[entry.key] = Restriction(apto, razon)
                    } catch (e: Exception) {
                        android.util.Log.e("ProductAnalysisDeserializer", "Error al procesar restricción ${entry.key}: ${e.message}")
                        // Agregar restricción por defecto en caso de error
                        clasificacionMap[entry.key] = Restriction(true, "Error al procesar")
                    }
                }
            }
            
            android.util.Log.d("ProductAnalysisDeserializer", "Clasificaciones procesadas: ${clasificacionMap.keys}")
            
            val result = ProductAnalysis(
                ingredientes = ingredientes,
                puedeContener = puedeContener,
                textoDetectado = textoDetectado,
                clasificacion = clasificacionMap
            )
            
            android.util.Log.d("ProductAnalysisDeserializer", "Deserialización exitosa")
            return result
            
        } catch (e: Exception) {
            android.util.Log.e("ProductAnalysisDeserializer", "Error en deserialización: ${e.message}", e)
            // Retornar objeto por defecto en caso de error
            return ProductAnalysis(
                ingredientes = "Error al procesar ingredientes",
                puedeContener = null,
                textoDetectado = "Error al procesar texto",
                clasificacion = emptyMap()
            )
        }
    }
}
