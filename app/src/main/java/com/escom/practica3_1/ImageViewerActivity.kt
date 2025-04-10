package com.escom.practica3_1

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.escom.practica3_1.utils.TrashManager
import java.io.File
import java.io.FileOutputStream

class ImageViewerActivity : AppCompatActivity() {
    
    private lateinit var imageFile: File
    private lateinit var trashManager: TrashManager
    private var currentRotation = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        
        // Initialize trash manager
        trashManager = TrashManager(this)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val imagePath = intent.getStringExtra("IMAGE_PATH")
        if (imagePath != null) {
            imageFile = File(imagePath)
            if (imageFile.exists()) {
                displayImage(imageFile)
            } else {
                Toast.makeText(this, "No se pudo encontrar la imagen", Toast.LENGTH_SHORT).show()
                finish() // Close activity if file doesn't exist
            }
        } else {
            Toast.makeText(this, "Ruta de imagen no proporcionada", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no path provided
        }
    }
    
    private fun displayImage(imageFile: File) {
        try {
            // Try to use SubsamplingScaleImageView for high-quality image viewing
            val scaleImageView = findViewById<SubsamplingScaleImageView>(R.id.scaleImageView)
            scaleImageView.setImage(ImageSource.uri(imageFile.absolutePath))
        } catch (e: Exception) {
            // Fallback to regular ImageView with Glide
            val fallbackImageView = findViewById<ImageView>(R.id.fallbackImageView)
            fallbackImageView.visibility = ImageView.VISIBLE
            
            Glide.with(this)
                .load(imageFile)
                .into(fallbackImageView)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_viewer_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_rotate_left -> {
                rotateImage(-90)
                true
            }
            R.id.action_rotate_right -> {
                rotateImage(90)
                true
            }
            R.id.action_share -> {
                shareImage()
                true
            }
            R.id.action_delete -> {
                moveToTrash()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun rotateImage(degrees: Int) {
        try {
            // Update current rotation
            currentRotation = (currentRotation + degrees) % 360
            
            // Load the bitmap
            val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
            
            // Create a matrix for the rotation
            val matrix = Matrix()
            matrix.postRotate(currentRotation.toFloat())
            
            // Create a new bitmap with the rotation applied
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            
            // Save the rotated bitmap back to the file
            val outputStream = FileOutputStream(imageFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Reload the image
            displayImage(imageFile)
            
            Toast.makeText(this, "Imagen rotada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al rotar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareImage() {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                imageFile
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Compartir imagen"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun moveToTrash() {
        if (trashManager.moveToTrash(imageFile, "image")) {
            Toast.makeText(this, "Imagen movida a la papelera", Toast.LENGTH_SHORT).show()
            finish() // Close the activity after moving to trash
        } else {
            Toast.makeText(this, "Error al mover a la papelera", Toast.LENGTH_SHORT).show()
        }
    }
}