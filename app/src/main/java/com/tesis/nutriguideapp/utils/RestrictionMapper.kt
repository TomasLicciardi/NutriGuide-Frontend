package com.tesis.nutriguideapp.utils

object RestrictionMapper {
    private val displayToApi = mapOf(
        "Sin gluten" to "sin_tacc",
        "Sin lactosa" to "sin_lactosa",
        "Sin frutos secos" to "sin_frutos_secos",
        "Vegano" to "vegano"
    )

    private val apiToDisplay = displayToApi.entries.associate { (k, v) -> v to k }

    fun toApiName(displayName: String): String = displayToApi[displayName] ?: displayName
    fun toDisplayName(apiName: String): String = apiToDisplay[apiName] ?: apiName

    fun toApiNames(displayNames: Collection<String>): List<String> = displayNames.map { toApiName(it) }
    fun toDisplayNames(apiNames: Collection<String>): List<String> = apiNames.map { toDisplayName(it) }

    val allDisplayNames = listOf("Sin gluten", "Sin lactosa", "Vegano", "Sin frutos secos")
}
