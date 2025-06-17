package com.tesis.nutriguideapp.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    
    /**
     * Formatea una fecha ISO string (2025-06-13T21:28:20.167042) 
     * a formato legible (13/06/2025 21:28)
     */
    fun formatDate(dateString: String): String {
        return try {
            // Parsear la fecha ISO
            val localDateTime = LocalDateTime.parse(dateString)
            
            // Formatear a DD/MM/YYYY HH:MM
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
            localDateTime.format(formatter)
        } catch (e: Exception) {
            // Si falla el parseo, devolver la fecha original
            dateString
        }
    }
    
    /**
     * Formatea solo la fecha sin hora (DD/MM/YYYY)
     */
    fun formatDateOnly(dateString: String): String {
        return try {
            val localDateTime = LocalDateTime.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            localDateTime.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }
    
    /**
     * Formatea solo la hora (HH:MM)
     */
    fun formatTimeOnly(dateString: String): String {
        return try {
            val localDateTime = LocalDateTime.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            localDateTime.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }
}
