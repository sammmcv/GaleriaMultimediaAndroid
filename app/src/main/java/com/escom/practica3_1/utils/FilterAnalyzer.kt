package com.escom.practica3_1.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import java.nio.ByteBuffer

class FilterAnalyzer(
    private val previewView: PreviewView,
    private var filterType: FilterType = FilterType.NONE
) : ImageAnalysis.Analyzer {

    enum class FilterType {
        NONE,
        GRAYSCALE,
        SEPIA,
        NEGATIVE
    }

    fun updateFilter(newFilter: FilterType) {
        filterType = newFilter
    }

    override fun analyze(image: ImageProxy) {
        // Si no hay filtro, simplemente liberar la imagen y retornar
        if (filterType == FilterType.NONE) {
            image.close()
            return
        }

        // Attempt to get bitmap from the image
        val bitmap = image.toBitmap()
        if (bitmap != null) {
            // Aplicar el filtro seleccionado
            val filteredBitmap = when (filterType) {
                FilterType.GRAYSCALE -> applyGrayscaleFilter(bitmap)
                FilterType.SEPIA -> applySepiaFilter(bitmap)
                FilterType.NEGATIVE -> applyNegativeFilter(bitmap)
                FilterType.NONE -> bitmap // No debería llegar aquí
            }
            
            // Aplicar el bitmap filtrado a la vista previa
            previewView.post {
                // Limpiar cualquier overlay anterior
                previewView.overlay.clear()
                
                // Crear un drawable con el bitmap filtrado
                val drawable = BitmapDrawable(previewView.resources, filteredBitmap)
                
                // Ajustar el drawable para que coincida con las dimensiones de la vista previa
                // y mantener la orientación correcta
                drawable.setBounds(0, 0, previewView.width, previewView.height)
                
                // Aplicar una matriz de transformación para corregir la orientación
                val matrix = Matrix()
                
                // Determinar la rotación basada en la orientación de la imagen
                val rotation = image.imageInfo.rotationDegrees
                matrix.postRotate(rotation.toFloat(), previewView.width / 2f, previewView.height / 2f)
                
                // Si es cámara frontal, voltear horizontalmente
                if (image.imageInfo.rotationDegrees == 90 || image.imageInfo.rotationDegrees == 270) {
                    matrix.postScale(-1f, 1f, previewView.width / 2f, previewView.height / 2f)
                }
                
                // Aplicar la transformación
                drawable.paint.shader?.setLocalMatrix(matrix)
                
                // Añadir el drawable al overlay
                previewView.overlay.add(drawable)
            }
        }

        image.close()
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun applyGrayscaleFilter(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // 0 = blanco y negro
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        
        // Matriz para efecto sepia
        val sepiaMatrix = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        
        colorMatrix.set(sepiaMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun applyNegativeFilter(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        
        // Invertir colores para efecto negativo
        colorMatrix.set(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
}