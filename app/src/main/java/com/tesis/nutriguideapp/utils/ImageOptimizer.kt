package com.tesis.nutriguideapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Utilidad para optimizar imágenes antes de subirlas al servidor.
 * Reduce el tamaño de las imágenes para mejorar el rendimiento de la red.
 */
object ImageOptimizer {
    private const val TAG = "ImageOptimizer"
    private const val MAX_WIDTH = 1200
    private const val MAX_HEIGHT = 1200
    private const val QUALITY = 85
    
    /**
     * Comprime y optimiza una imagen desde una Uri.
     * 
     * @param context El contexto de la aplicación
     * @param imageUri La Uri de la imagen a optimizar
     * @return Un archivo temporal con la imagen optimizada, o null si ocurre un error
     */
    fun optimizeImage(context: Context, imageUri: Uri): File? {
        try {
            // Abrir el flujo de entrada desde la Uri
            val inputStream = context.contentResolver.openInputStream(imageUri)
            
            // Decodificar las dimensiones de la imagen sin cargarla completamente
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // Calcular el factor de escala para redimensionar la imagen
            val (width, height) = calculateScaledDimensions(options.outWidth, options.outHeight)
            
            // Reabrir el flujo de entrada
            val inputStream2 = context.contentResolver.openInputStream(imageUri)
            
            // Decodificar la imagen con el tamaño calculado
            val bitmap = BitmapFactory.decodeStream(inputStream2)
            inputStream2?.close()
            
            if (bitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen")
                return null
            }
            
            // Redimensionar la imagen si es necesario
            val scaledBitmap = if (bitmap.width > width || bitmap.height > height) {
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }
            
            // Comprimir la imagen
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            
            // Liberar recursos si se creó un nuevo bitmap
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            
            // Crear un archivo temporal para la imagen optimizada
            val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.close()
            
            Log.d(TAG, "Imagen optimizada: original=${options.outWidth}x${options.outHeight}, " +
                      "nueva=${width}x${height}, tamaño=${file.length() / 1024}KB")
            
            return file
        } catch (e: IOException) {
            Log.e(TAG, "Error al optimizar la imagen", e)
            return null
        }
    }
    
    /**
     * Calcula las dimensiones escaladas manteniendo la relación de aspecto.
     * 
     * @param originalWidth Ancho original de la imagen
     * @param originalHeight Alto original de la imagen
     * @return Par con el ancho y alto escalados
     */
    private fun calculateScaledDimensions(originalWidth: Int, originalHeight: Int): Pair<Int, Int> {
        var width = originalWidth
        var height = originalHeight
        
        // Reducir las dimensiones manteniendo la relación de aspecto
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            val aspectRatio = width.toFloat() / height.toFloat()
            
            if (width > height) {
                width = MAX_WIDTH
                height = (width / aspectRatio).toInt()
            } else {
                height = MAX_HEIGHT
                width = (height * aspectRatio).toInt()
            }
        }
        
        return Pair(width, height)
    }
}
