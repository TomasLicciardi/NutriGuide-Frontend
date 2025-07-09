package com.tesis.nutriguideapp.model

import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// -------------------------
// DATA CLASSES
// -------------------------

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
        return try {
            resultJson.ingredientes
                .orEmpty()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("Product", "Error al procesar ingredientes: ${e.message}", e)
            listOf("Error al procesar ingredientes")
        }
    }

    // Método para obtener restricciones detectadas usando la nueva estructura
    fun getRestrictionsDetected(): List<Restriction> {
        return try {
            resultJson.clasificacion.values.toList()
        } catch (e: Exception) {
            Log.e("Product", "Error al obtener restricciones: ${e.message}", e)
            emptyList()
        }
    }

    // Método para obtener el texto detectado en la imagen
    fun getTextDetected(): String {
        return try {
            resultJson.textoDetectado?.ifEmpty { "No se detectó texto" } ?: "No se detectó texto"
        } catch (e: Exception) {
            Log.e("Product", "Error al obtener texto detectado: ${e.message}", e)
            "Error al procesar texto detectado"
        }
    }
}

data class ProductAnalysis(
    val ingredientes: String = "",
    val puedeContener: String? = null,
    val textoDetectado: String = "",
    val clasificacion: Map<String, Restriction> = emptyMap()
)

data class Restriction(
    val apto: Boolean,
    val razon: String?,
    val confidence: Float? = null
)

// -------------------------
// CUSTOM DESERIALIZER
// -------------------------

class ProductAnalysisDeserializer : JsonDeserializer<ProductAnalysis> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProductAnalysis {
        try {
            Log.d("ProductAnalysisDeserializer", "Deserializando JSON: $json")

            if (json.isJsonNull) {
                Log.e("ProductAnalysisDeserializer", "JSON es null")
                throw JsonParseException("JSON es null")
            }

            val jsonObject = if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                try {
                    Log.d("ProductAnalysisDeserializer", "Detectado JSON como string, intentando parsear")
                    JsonParser().parse(json.asString).asJsonObject
                } catch (e: Exception) {
                    Log.e("ProductAnalysisDeserializer", "Error al parsear JSON desde string: ${e.message}")
                    json.asJsonObject
                }
            } else {
                json.asJsonObject
            }

            Log.d("ProductAnalysisDeserializer", "Estructura JSON completa: $jsonObject")

            val ingredientes = try {
                jsonObject.get("ingredientes")?.asString ?: ""
            } catch (e: Exception) {
                Log.e("ProductAnalysisDeserializer", "Error al extraer ingredientes: ${e.message}")
                ""
            }

            val puedeContener = try {
                jsonObject.get("puede_contener")?.let {
                    if (it.isJsonNull) null else it.asString
                }
            } catch (e: Exception) {
                Log.e("ProductAnalysisDeserializer", "Error al extraer puede_contener: ${e.message}")
                null
            }

            val textoDetectado = try {
                jsonObject.get("texto_detectado")?.asString ?: ""
            } catch (e: Exception) {
                Log.e("ProductAnalysisDeserializer", "Error al extraer texto_detectado: ${e.message}")
                ""
            }

            Log.d(
                "ProductAnalysisDeserializer",
                "Campos básicos extraídos - ingredientes: ${ingredientes.take(50)}, puedeContener: ${puedeContener?.take(50) ?: "null"}, textoDetectado: ${textoDetectado.take(50)}"
            )

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
                                    val confidence = restrictionObj.get("confidence")?.let {
                                        if (it.isJsonNull) null else it.asFloat
                                    }
                                    clasificacionMap[entry.key] =
                                        Restriction(apto, razon, confidence)
                                } else {
                                    Log.w(
                                        "ProductAnalysisDeserializer",
                                        "La restricción ${entry.key} no es un objeto JSON: ${entry.value}"
                                    )
                                    clasificacionMap[entry.key] =
                                        Restriction(true, "Formato no válido")
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "ProductAnalysisDeserializer",
                                    "Error al procesar restricción ${entry.key}: ${e.message}",
                                    e
                                )
                                clasificacionMap[entry.key] =
                                    Restriction(true, "Error al procesar: ${e.message}")
                            }
                        }
                    } else {
                        Log.w(
                            "ProductAnalysisDeserializer",
                            "Campo 'clasificacion' no es un objeto JSON o es null"
                        )
                    }
                } ?: run {
                    Log.w(
                        "ProductAnalysisDeserializer",
                        "Campo 'clasificacion' no encontrado en el JSON"
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "ProductAnalysisDeserializer",
                    "Error al procesar mapa de clasificaciones: ${e.message}",
                    e
                )
            }

            Log.d(
                "ProductAnalysisDeserializer",
                "Clasificaciones procesadas (${clasificacionMap.size}): ${clasificacionMap.keys}"
            )

            val result = ProductAnalysis(
                ingredientes = ingredientes,
                puedeContener = puedeContener,
                textoDetectado = textoDetectado,
                clasificacion = clasificacionMap
            )

            Log.d("ProductAnalysisDeserializer", "Deserialización exitosa")
            return result

        } catch (e: Exception) {
            Log.e(
                "ProductAnalysisDeserializer",
                "Error en deserialización: ${e.message}",
                e
            )
            return ProductAnalysis(
                ingredientes = "Error al procesar ingredientes: ${e.message}",
                puedeContener = null,
                textoDetectado = "Error al procesar texto",
                clasificacion = mapOf(
                    "error" to Restriction(
                        false,
                        "Error al deserializar JSON: ${e.message}",
                        confidence = null
                    )
                )
            )
        }
    }
}
