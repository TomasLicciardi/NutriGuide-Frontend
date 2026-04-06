package com.tesis.nutriguideapp.model

import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("result_json") val resultJson: ProductAnalysis,
    @SerializedName("date") val date: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_type") val imageType: String?,
    @SerializedName("is_suitable") val isSuitable: Boolean = false
) {
    fun getIngredientNames(): List<String> {
        return try {
            resultJson.ingredients.map { it.nameEs }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("Product", "Error al procesar ingredientes: ${e.message}", e)
            listOf("Error al procesar ingredientes")
        }
    }

    fun getRestrictions(): Map<String, Restriction> {
        return resultJson.restrictions
    }
}

data class ProductAnalysis(
    val restrictions: Map<String, Restriction> = emptyMap(),
    val ingredients: List<ProductIngredientItem> = emptyList()
)

data class Restriction(
    val apto: Boolean,
    val motivo: String? = null
)

data class ProductIngredientItem(
    @SerializedName("name_es") val nameEs: String = "",
    @SerializedName("name_en") val nameEn: String = "",
    val category: String = "",
    val origin: String? = null,
    @SerializedName("resolved_by") val resolvedBy: String = "unresolved",
    val confidence: Float = 0f
)

class ProductAnalysisDeserializer : JsonDeserializer<ProductAnalysis> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProductAnalysis {
        try {
            Log.d("ProductAnalysisDeser", "Deserializando JSON: $json")

            if (json.isJsonNull) {
                return ProductAnalysis()
            }

            val jsonObject = if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                try {
                    JsonParser().parse(json.asString).asJsonObject
                } catch (e: Exception) {
                    Log.e("ProductAnalysisDeser", "Error al parsear JSON string: ${e.message}")
                    return ProductAnalysis()
                }
            } else if (json.isJsonObject) {
                json.asJsonObject
            } else {
                return ProductAnalysis()
            }

            val restrictionsMap = mutableMapOf<String, Restriction>()
            try {
                jsonObject.get("restrictions")?.let { restrictionsElement ->
                    if (!restrictionsElement.isJsonNull && restrictionsElement.isJsonObject) {
                        val restrictionsObj = restrictionsElement.asJsonObject
                        restrictionsObj.entrySet().forEach { entry ->
                            try {
                                if (entry.value.isJsonObject) {
                                    val rObj = entry.value.asJsonObject
                                    val apto = rObj.get("apto")?.asBoolean ?: true
                                    val motivo = rObj.get("motivo")?.let {
                                        if (it.isJsonNull) null else it.asString
                                    }
                                    restrictionsMap[entry.key] = Restriction(apto, motivo)
                                }
                            } catch (e: Exception) {
                                Log.e("ProductAnalysisDeser", "Error restriction ${entry.key}: ${e.message}")
                                restrictionsMap[entry.key] = Restriction(true, "Error: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductAnalysisDeser", "Error processing restrictions: ${e.message}")
            }

            val ingredientsList = mutableListOf<ProductIngredientItem>()
            try {
                jsonObject.get("ingredients")?.let { ingredientsElement ->
                    if (!ingredientsElement.isJsonNull && ingredientsElement.isJsonArray) {
                        val ingredientsArray = ingredientsElement.asJsonArray
                        ingredientsArray.forEach { element ->
                            try {
                                if (element.isJsonObject) {
                                    val ingObj = element.asJsonObject
                                    ingredientsList.add(
                                        ProductIngredientItem(
                                            nameEs = ingObj.get("name_es")?.asString ?: "",
                                            nameEn = ingObj.get("name_en")?.asString ?: "",
                                            category = ingObj.get("category")?.asString ?: "",
                                            origin = ingObj.get("origin")?.let {
                                                if (it.isJsonNull) null else it.asString
                                            },
                                            resolvedBy = ingObj.get("resolved_by")?.asString ?: "unresolved",
                                            confidence = ingObj.get("confidence")?.asFloat ?: 0f
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("ProductAnalysisDeser", "Error ingredient: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductAnalysisDeser", "Error processing ingredients: ${e.message}")
            }

            return ProductAnalysis(
                restrictions = restrictionsMap,
                ingredients = ingredientsList
            )
        } catch (e: Exception) {
            Log.e("ProductAnalysisDeser", "Error en deserialización: ${e.message}", e)
            return ProductAnalysis()
        }
    }
}
