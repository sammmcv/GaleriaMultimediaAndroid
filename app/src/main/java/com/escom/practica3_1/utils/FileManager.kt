package com.escom.practica3_1.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileManager(private val context: Context) {
    private val IMAGE_FOLDER = "images"
    private val AUDIO_FOLDER = "audio"
    
    // Directorio para imágenes
    private val imageDir: File by lazy {
        val dir = File(context.filesDir, IMAGE_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    // Directorio para audio
    private val audioDir: File by lazy {
        val dir = File(context.filesDir, AUDIO_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    // Crear un archivo para una nueva imagen
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"
        return File(imageDir, fileName)
    }
    
    // Crear un archivo para un nuevo audio
    fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUD_$timeStamp.mp3"
        return File(audioDir, fileName)
    }
    
    // Obtener un URI para un archivo
    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
    
    // Obtener todas las imágenes
    fun getImageFiles(): List<File> {
        return imageDir.listFiles()?.filter { 
            it.isFile && (it.name.lowercase().endsWith(".jpg") || 
            it.name.lowercase().endsWith(".jpeg") || 
            it.name.lowercase().endsWith(".png"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    // Obtener todos los archivos de audio
    fun getAudioFiles(): List<File> {
        return audioDir.listFiles()?.filter { 
            it.isFile && (it.name.lowercase().endsWith(".mp3") || 
            it.name.lowercase().endsWith(".wav") || 
            it.name.lowercase().endsWith(".aac") ||
            it.name.lowercase().endsWith(".m4a"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}