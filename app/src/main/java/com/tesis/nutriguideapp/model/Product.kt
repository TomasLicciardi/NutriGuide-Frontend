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
    val ingredients: List<ProductIngredientItem> = emptyList(),
    val declaration: ProductDeclaration = ProductDeclaration(),
    val userVerdict: Boolean = true,
    val overallConfidence: Float = 0f
)

data class Restriction(
    val apto: Boolean,
    val motivo: String? = null,
    val fuente: String = "ingredient_analysis",
    val confidence: Float = 0f,
    val ingredienteDisparador: String? = null
)

data class ProductIngredientItem(
    @SerializedName("name_es") val nameEs: String = "",
    @SerializedName("name_en") val nameEn: String = "",
    val category: String = "",
    val origin: String = "unknown",
    @SerializedName("function_tag") val functionTag: String? = null,
    @SerializedName("is_flavoring") val isFlavoring: Boolean = false,
    @SerializedName("flavoring_type") val flavoringType: String? = null,
    @SerializedName("target_sensory") val targetSensory: String? = null,
    val allergens: List<String> = emptyList(),
    val contains: List<String> = emptyList(),
    @SerializedName("derived_from") val derivedFrom: List<String> = emptyList(),
    val sources: List<String> = emptyList(),
    val confidence: Float = 0f,
    @SerializedName("description_es") val descriptionEs: String? = null
)

data class ProductDeclaration(
    val contains: List<String> = emptyList(),
    @SerializedName("may_contain") val mayContain: List<String> = emptyList(),
    @SerializedName("positive_claims") val positiveClaims: List<String> = emptyList(),
    @SerializedName("raw_text") val rawText: String? = null
)

class ProductAnalysisDeserializer : JsonDeserializer<ProductAnalysis> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProductAnalysis {
        try {
            if (json.isJsonNull) return ProductAnalysis()

            val jsonObject = when {
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    try {
                        JsonParser.parseString(json.asString).asJsonObject
                    } catch (e: Exception) {
                        Log.e("ProductAnalysisDeser", "Error parseando JSON string: ${e.message}")
                        return ProductAnalysis()
                    }
                }
                json.isJsonObject -> json.asJsonObject
                else -> return ProductAnalysis()
            }

            val restrictionsMap = mutableMapOf<String, Restriction>()
            jsonObject.get("restrictions")?.takeIf { it.isJsonObject }?.asJsonObject?.entrySet()?.forEach { entry ->
                try {
                    val r = entry.value.asJsonObject
                    restrictionsMap[entry.key] = Restriction(
                        apto = r.get("apto")?.asBoolean ?: true,
                        motivo = r.get("motivo")?.takeIf { !it.isJsonNull }?.asString,
                        fuente = r.get("fuente")?.takeIf { !it.isJsonNull }?.asString ?: "ingredient_analysis",
                        confidence = r.get("confidence")?.asFloat ?: 0f,
                        ingredienteDisparador = r.get("ingrediente_disparador")?.takeIf { !it.isJsonNull }?.asString
                    )
                } catch (e: Exception) {
                    Log.e("ProductAnalysisDeser", "Error restriction ${entry.key}: ${e.message}")
                }
            }

            val ingredientsList = mutableListOf<ProductIngredientItem>()
            jsonObject.get("ingredients")?.takeIf { it.isJsonArray }?.asJsonArray?.forEach { element ->
                try {
                    val ing = element.asJsonObject
                    ingredientsList.add(
                        ProductIngredientItem(
                            nameEs = ing.get("name_es")?.asString ?: "",
                            nameEn = ing.get("name_en")?.takeIf { !it.isJsonNull }?.asString ?: "",
                            category = ing.get("category")?.asString ?: "",
                            origin = ing.get("origin")?.asString ?: "unknown",
                            functionTag = ing.get("function_tag")?.takeIf { !it.isJsonNull }?.asString,
                            isFlavoring = ing.get("is_flavoring")?.asBoolean ?: false,
                            flavoringType = ing.get("flavoring_type")?.takeIf { !it.isJsonNull }?.asString,
                            targetSensory = ing.get("target_sensory")?.takeIf { !it.isJsonNull }?.asString,
                            allergens = ing.get("allergens")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                            contains = ing.get("contains")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                            derivedFrom = ing.get("derived_from")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                            sources = ing.get("sources")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                            confidence = ing.get("confidence")?.asFloat ?: 0f,
                            descriptionEs = ing.get("description_es")?.takeIf { !it.isJsonNull }?.asString
                        )
                    )
                } catch (e: Exception) {
                    Log.e("ProductAnalysisDeser", "Error ingredient: ${e.message}")
                }
            }

            val declaration = jsonObject.get("declaration")?.takeIf { it.isJsonObject }?.asJsonObject?.let { d ->
                ProductDeclaration(
                    contains = d.get("contains")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                    mayContain = d.get("may_contain")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                    positiveClaims = d.get("positive_claims")?.takeIf { it.isJsonArray }?.asJsonArray?.map { it.asString } ?: emptyList(),
                    rawText = d.get("raw_text")?.takeIf { !it.isJsonNull }?.asString
                )
            } ?: ProductDeclaration()

            return ProductAnalysis(
                restrictions = restrictionsMap,
                ingredients = ingredientsList,
                declaration = declaration,
                userVerdict = jsonObject.get("user_verdict")?.asBoolean ?: true,
                overallConfidence = jsonObject.get("overall_confidence")?.asFloat ?: 0f
            )
        } catch (e: Exception) {
            Log.e("ProductAnalysisDeser", "Error en deserialización: ${e.message}", e)
            return ProductAnalysis()
        }
    }
}
