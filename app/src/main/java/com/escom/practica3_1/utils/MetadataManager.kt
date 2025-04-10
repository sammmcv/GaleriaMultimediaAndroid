package com.escom.practica3_1.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileMetadata(
    val fileName: String,
    val fileSize: String,
    val creationDate: String,
    val location: String = "No disponible",
    val tags: List<String> = emptyList(),
    val duration: String = "", // Solo para audio
    val resolution: String = "", // Solo para imágenes
    val additionalInfo: Map<String, String> = emptyMap()
)

class MetadataManager(private val context: Context) {
    
    fun getMetadata(file: File): FileMetadata {
        val fileName = file.name
        val fileSize = formatFileSize(file.length())
        val creationDate = formatDate(file.lastModified())
        
        return when {
            isImageFile(fileName) -> getImageMetadata(file, fileName, fileSize, creationDate)
            isAudioFile(fileName) -> getAudioMetadata(file, fileName, fileSize, creationDate)
            else -> FileMetadata(fileName, fileSize, creationDate)
        }
    }
    
    private fun getImageMetadata(file: File, fileName: String, fileSize: String, creationDate: String): FileMetadata {
        try {
            val exif = ExifInterface(file.absolutePath)
            
            // Obtener ubicación
            val latLong = FloatArray(2)
            val hasLocation = exif.getLatLong(latLong)
            val location = if (hasLocation) {
                "Lat: ${latLong[0]}, Long: ${latLong[1]}"
            } else {
                "No disponible"
            }
            
            // Obtener resolución
            val width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) ?: "?"
            val height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) ?: "?"
            val resolution = "$width x $height"
            
            // Información adicional
            val additionalInfo = mutableMapOf<String, String>()
            exif.getAttribute(ExifInterface.TAG_MAKE)?.let { additionalInfo["Fabricante"] = it }
            exif.getAttribute(ExifInterface.TAG_MODEL)?.let { additionalInfo["Modelo"] = it }
            exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { additionalInfo["Tiempo de exposición"] = it }
            exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE)?.let { additionalInfo["Apertura"] = it }
            
            return FileMetadata(
                fileName = fileName,
                fileSize = fileSize,
                creationDate = creationDate,
                location = location,
                resolution = resolution,
                additionalInfo = additionalInfo
            )
        } catch (e: IOException) {
            return FileMetadata(fileName, fileSize, creationDate)
        }
    }
    
    private fun getAudioMetadata(file: File, fileName: String, fileSize: String, creationDate: String): FileMetadata {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            
            // Obtener duración
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            val duration = formatDuration(durationMs)
            
            // Información adicional
            val additionalInfo = mutableMapOf<String, String>()
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.let { 
                additionalInfo["Bitrate"] = "${it.toInt() / 1000} kbps" 
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let { 
                additionalInfo["Título"] = it 
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let { 
                additionalInfo["Artista"] = it 
            }
            
            retriever.release()
            
            return FileMetadata(
                fileName = fileName,
                fileSize = fileSize,
                creationDate = creationDate,
                duration = duration,
                additionalInfo = additionalInfo
            )
        } catch (e: Exception) {
            return FileMetadata(fileName, fileSize, creationDate)
        }
    }
    
    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        return when {
            kb < 1024 -> String.format("%.2f KB", kb)
            else -> String.format("%.2f MB", kb / 1024)
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    private fun isImageFile(fileName: String): Boolean {
        val lowerCaseName = fileName.lowercase()
        return lowerCaseName.endsWith(".jpg") || 
               lowerCaseName.endsWith(".jpeg") || 
               lowerCaseName.endsWith(".png")
    }
    
    private fun isAudioFile(fileName: String): Boolean {
        val lowerCaseName = fileName.lowercase()
        return lowerCaseName.endsWith(".mp3") || 
               lowerCaseName.endsWith(".wav") || 
               lowerCaseName.endsWith(".aac") ||
               lowerCaseName.endsWith(".m4a")
    }
}