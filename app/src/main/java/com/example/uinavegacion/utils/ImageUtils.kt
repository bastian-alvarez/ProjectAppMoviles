package com.example.uinavegacion.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    
    /**
     * Convierte una imagen URI a Base64 con compresión
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen
     * @param maxSizeKB Tamaño máximo en KB (por defecto 500KB)
     * @return String en formato Base64 o null si falla
     */
    fun uriToBase64(context: Context, imageUri: Uri, maxSizeKB: Int = 500): String? {
        return try {
            Log.d("ImageUtils", "🖼️ Convirtiendo imagen a Base64: $imageUri")
            
            // Leer la imagen desde la URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("ImageUtils", "❌ No se pudo abrir el InputStream")
                return null
            }
            
            // Decodificar la imagen a Bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                Log.e("ImageUtils", "❌ No se pudo decodificar el Bitmap")
                return null
            }
            
            Log.d("ImageUtils", "📐 Tamaño original: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Corregir orientación si es necesario
            val rotatedBitmap = correctImageOrientation(context, imageUri, originalBitmap)
            
            // Comprimir la imagen
            val compressedBitmap = compressBitmap(rotatedBitmap, maxSizeKB)
            
            // Convertir a Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            val sizeKB = base64String.length / 1024
            
            Log.d("ImageUtils", "✅ Imagen convertida a Base64 (${sizeKB}KB)")
            
            // Liberar recursos
            if (rotatedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            compressedBitmap.recycle()
            
            // Retornar con prefijo data:image
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error al convertir imagen a Base64: ${e.message}", e)
            null
        }
    }
    
    /**
     * Corrige la orientación de la imagen según los metadatos EXIF
     */
    private fun correctImageOrientation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) return bitmap
            
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }
            
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            
            Log.d("ImageUtils", "🔄 Imagen rotada según EXIF: orientación=$orientation")
            rotatedBitmap
        } catch (e: Exception) {
            Log.w("ImageUtils", "⚠️ No se pudo corregir orientación: ${e.message}")
            bitmap
        }
    }
    
    /**
     * Comprime un Bitmap para que no exceda el tamaño máximo
     */
    private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        
        // Si la imagen es muy grande, reducir dimensiones
        val maxDimension = 1024
        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = maxDimension
                height = (maxDimension / ratio).toInt()
            } else {
                height = maxDimension
                width = (maxDimension * ratio).toInt()
            }
            
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            Log.d("ImageUtils", "📏 Imagen redimensionada a: ${width}x${height}")
            return scaledBitmap
        }
        
        return bitmap
    }
    
    /**
     * Convierte Base64 a Bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Remover el prefijo data:image si existe
            val cleanBase64 = base64String.substringAfter("base64,")
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error al convertir Base64 a Bitmap: ${e.message}", e)
            null
        }
    }
}

