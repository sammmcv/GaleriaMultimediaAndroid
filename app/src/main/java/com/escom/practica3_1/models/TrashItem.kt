package com.escom.practica3_1.models

import android.net.Uri
import java.util.Date

data class TrashItem(
    val id: String,
    val uri: Uri,
    val originalPath: String,
    val name: String,
    val type: String, // "image" o "audio"
    val deletedDate: Date,
    val expiryDate: Date // Fecha en que se eliminará permanentemente
)