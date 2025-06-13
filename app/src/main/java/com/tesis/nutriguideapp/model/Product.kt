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
) {    // Método para obtener ingredientes usando la nueva estructura
    fun getIngredients(): List<String> {
        return try {
            resultJson.ingredientes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            android.util.Log.e("Product", "Error al procesar ingredientes: ${e.message}", e)
            listOf("Error al procesar ingredientes")
        }
    }
    
    // Método para obtener restricciones detectadas usando la nueva estructura
    fun getRestrictionsDetected(): List<Restriction> {
        return try {
            resultJson.clasificacion.values.toList()
        } catch (e: Exception) {
            android.util.Log.e("Product", "Error al obtener restricciones: ${e.message}", e)
            emptyList()
        }
    }
    
    // Método para obtener el texto detectado en la imagen
    fun getTextDetected(): String {
        return try {
            resultJson.textoDetectado.ifEmpty { "No se detectó texto" }
        } catch (e: Exception) {
            android.util.Log.e("Product", "Error al obtener texto detectado: ${e.message}", e)
            "Error al procesar texto detectado"
        }
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
            
            // Validación de JSON nulo
            if (json.isJsonNull) {
                android.util.Log.e("ProductAnalysisDeserializer", "JSON es null")
                throw JsonParseException("JSON es null")
            }
            
            // Verificar si es un string que contiene JSON (caso especial cuando el backend serializa doblemente)
            val jsonObject = if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                try {
                    // Intentar parsear el string como JSON
                    android.util.Log.d("ProductAnalysisDeserializer", "Detectado JSON como string, intentando parsear")
                    val parser = com.google.gson.JsonParser()
                    parser.parse(json.asString).asJsonObject
                } catch (e: Exception) {
                    android.util.Log.e("ProductAnalysisDeserializer", "Error al parsear JSON desde string: ${e.message}")
                    json.asJsonObject
                }
            } else {
                json.asJsonObject
            }
            
            // Imprimir estructura JSON completa para diagnóstico
            android.util.Log.d("ProductAnalysisDeserializer", "Estructura JSON completa: $jsonObject")
            
            // Extraer campos con valores por defecto si no existen
            val ingredientes = try {
                jsonObject.get("ingredientes")?.asString ?: ""
            } catch (e: Exception) {
                android.util.Log.e("ProductAnalysisDeserializer", "Error al extraer ingredientes: ${e.message}")
                ""
            }
            
            val puedeContener = try {
                jsonObject.get("puede_contener")?.let {
                    if (it.isJsonNull) null else it.asString
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductAnalysisDeserializer", "Error al extraer puede_contener: ${e.message}")
                null
            }
            
            val textoDetectado = try {
                jsonObject.get("texto_detectado")?.asString ?: ""
            } catch (e: Exception) {
                android.util.Log.e("ProductAnalysisDeserializer", "Error al extraer texto_detectado: ${e.message}")
                ""
            }
            
            android.util.Log.d("ProductAnalysisDeserializer", "Campos básicos extraídos - ingredientes: ${ingredientes.take(50)}, puedeContener: ${puedeContener?.take(50) ?: "null"}, textoDetectado: ${textoDetectado.take(50)}")
            
            // Extraer clasificacion - puede ser un objeto vacío
            val clasificacionMap = mutableMapOf<String, Restriction>()
            try {
                jsonObject.get("clasificacion")?.let { clasificacionElement ->
                    if (!clasificacionElement.isJsonNull && clasificacionElement.isJsonObject) {
                        val clasificacionObj = clasificacionElement.asJsonObject
                        clasificacionObj.entrySet().forEach { entry ->
                            try {
                                if (entry.value.isJsonObject) {
                                    val restrictionObj = entry.value.asJsonObject
                                    val apto = restrictionObj.get("apto")?.asBoolean ?: true
                                    val razon = restrictionObj.get("razon")?.let {
                                        if (it.isJsonNull) null else it.asString
                                    }
                                    clasificacionMap[entry.key] = Restriction(apto, razon)
                                } else {
                                    // Si no es un objeto, intentar interpretarlo
                                    android.util.Log.w("ProductAnalysisDeserializer", "La restricción ${entry.key} no es un objeto JSON: ${entry.value}")
                                    clasificacionMap[entry.key] = Restriction(true, "Formato no válido")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProductAnalysisDeserializer", "Error al procesar restricción ${entry.key}: ${e.message}", e)
                                // Agregar restricción por defecto en caso de error
                                clasificacionMap[entry.key] = Restriction(true, "Error al procesar: ${e.message}")
                            }
                        }
                    } else {
                        android.util.Log.w("ProductAnalysisDeserializer", "Campo 'clasificacion' no es un objeto JSON o es null")
                    }
                } ?: run {
                    android.util.Log.w("ProductAnalysisDeserializer", "Campo 'clasificacion' no encontrado en el JSON")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductAnalysisDeserializer", "Error al procesar mapa de clasificaciones: ${e.message}", e)
            }
            
            android.util.Log.d("ProductAnalysisDeserializer", "Clasificaciones procesadas (${clasificacionMap.size}): ${clasificacionMap.keys}")
            
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
                ingredientes = "Error al procesar ingredientes: ${e.message}",
                puedeContener = null,
                textoDetectado = "Error al procesar texto",
                clasificacion = mapOf("error" to Restriction(false, "Error al deserializar JSON: ${e.message}"))
            )
        }
    }
}
