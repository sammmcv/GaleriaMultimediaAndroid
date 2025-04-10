package com.escom.practica3_1.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.escom.practica3_1.models.TrashItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class TrashManager(private val context: Context) {
    private val TAG = "TrashManager"
    private val PREFS_NAME = "trash_prefs"
    private val TRASH_ITEMS_KEY = "trash_items"
    private val TRASH_FOLDER = "trash"
    private val RETENTION_DAYS = 30L // Días que los elementos permanecen en la papelera
    
    // Create a custom Gson instance with the UriTypeAdapter
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
        .create()
        
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Directorio de la papelera
    private val trashDir: File by lazy {
        val dir = File(context.filesDir, TRASH_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    // Obtener todos los elementos de la papelera
    fun getTrashItems(): List<TrashItem> {
        val itemsJson = prefs.getString(TRASH_ITEMS_KEY, "[]")
        val type = object : TypeToken<List<TrashItem>>() {}.type
        return gson.fromJson(itemsJson, type) ?: emptyList()
    }
    
    // Guardar la lista de elementos en la papelera
    private fun saveTrashItems(items: List<TrashItem>) {
        val itemsJson = gson.toJson(items)
        prefs.edit().putString(TRASH_ITEMS_KEY, itemsJson).apply()
    }
    
    // Mover un archivo a la papelera
    fun moveToTrash(file: File, type: String): Boolean {
        try {
            // Crear un ID único para el elemento
            val id = UUID.randomUUID().toString()
            
            // Crear el archivo de destino en la papelera
            val trashFile = File(trashDir, id + "_" + file.name)
            
            // Copiar el archivo a la papelera
            file.copyTo(trashFile, true)
            
            // Calcular la fecha de expiración
            val now = Date()
            val calendar = Calendar.getInstance()
            calendar.time = now
            calendar.add(Calendar.DAY_OF_YEAR, RETENTION_DAYS.toInt())
            val expiryDate = calendar.time
            
            // Crear el elemento de la papelera
            val trashItem = TrashItem(
                id = id,
                uri = Uri.fromFile(trashFile),
                originalPath = file.absolutePath,
                name = file.name,
                type = type,
                deletedDate = now,
                expiryDate = expiryDate
            )
            
            // Añadir a la lista de elementos en la papelera
            val items = getTrashItems().toMutableList()
            items.add(trashItem)
            saveTrashItems(items)
            
            // Eliminar el archivo original
            return file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error al mover a la papelera: ${e.message}", e)
            return false
        }
    }
    
    // Restaurar un elemento de la papelera
    fun restoreFromTrash(trashItem: TrashItem): Boolean {
        try {
            // Verificar si el URI es nulo
            if (trashItem.uri == null || trashItem.uri.path == null) {
                Log.e(TAG, "URI nulo o path nulo al restaurar")
                return false
            }
            
            // Obtener el archivo en la papelera
            val trashFile = File(trashItem.uri.path!!)
            if (!trashFile.exists()) {
                Log.e(TAG, "El archivo en la papelera no existe: ${trashFile.absolutePath}")
                
                // Eliminar el elemento de la lista aunque no exista el archivo
                val items = getTrashItems().toMutableList()
                items.removeIf { it.id == trashItem.id }
                saveTrashItems(items)
                
                return false
            }
            
            // Crear el archivo de destino (original)
            val originalFile = File(trashItem.originalPath)
            
            // Asegurarse de que el directorio existe
            originalFile.parentFile?.mkdirs()
            
            // Copiar el archivo de vuelta a su ubicación original
            trashFile.copyTo(originalFile, true)
            
            // Eliminar el archivo de la papelera
            trashFile.delete()
            
            // Eliminar el elemento de la lista
            val items = getTrashItems().toMutableList()
            items.removeIf { it.id == trashItem.id }
            saveTrashItems(items)
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar de la papelera: ${e.message}", e)
            return false
        }
    }
    
    // Eliminar permanentemente un elemento de la papelera
    fun deletePermanently(trashItem: TrashItem): Boolean {
        try {
            // Verificar si el URI es nulo
            if (trashItem.uri != null) {
                // Obtener el archivo en la papelera
                val path = trashItem.uri.path
                if (path != null) {
                    val trashFile = File(path)
                    // Eliminar el archivo si existe
                    if (trashFile.exists()) {
                        trashFile.delete()
                    }
                }
            }
            
            // Eliminar el elemento de la lista independientemente de si el archivo existe
            val items = getTrashItems().toMutableList()
            items.removeIf { it.id == trashItem.id }
            saveTrashItems(items)
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar permanentemente: ${e.message}", e)
            return false
        }
    }
    
    // Limpiar elementos expirados
    fun cleanupExpiredItems() {
        val now = Date()
        val items = getTrashItems().toMutableList()
        val expiredItems = items.filter { it.expiryDate.before(now) }
        
        for (item in expiredItems) {
            deletePermanently(item)
        }
    }
    
    // Add this method to clear all trash data (use with caution)
    fun clearTrashData() {
        prefs.edit().remove(TRASH_ITEMS_KEY).apply()
        // Optionally delete all files in trash directory
        if (trashDir.exists()) {
            trashDir.listFiles()?.forEach { it.delete() }
        }
    }
}