package com.escom.practica3_1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.escom.practica3_1.ui.theme.ThemeType

class MainActivity : AppCompatActivity() {
    
    private val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    
    // Registrar el launcher para solicitar permisos de cámara
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCameraActivity()
        } else {
            showPermissionDeniedMessage("cámara")
        }
    }
    
    // Registrar el launcher para solicitar permisos de audio
    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startAudioRecorderActivity()
        } else {
            showPermissionDeniedMessage("micrófono")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cargar y aplicar el tema guardado
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val themeType = prefs.getInt("theme_type", 0)
        val themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Aplicar el modo (claro/oscuro)
        AppCompatDelegate.setDefaultNightMode(themeMode)

        // Aplicar el tema (IPN/ESCOM)
        setTheme(when (themeType) {
            1 -> R.style.Theme_Practica3_1_ESCOM
            else -> R.style.Theme_Practica3_1_IPN
        })
        
        setContentView(R.layout.activity_main)
        
        // Configurar botones
        findViewById<androidx.cardview.widget.CardView>(R.id.cardCamera).setOnClickListener {
            checkCameraPermission()
        }
        
        findViewById<androidx.cardview.widget.CardView>(R.id.cardAudio).setOnClickListener {
            checkAudioPermission()
        }
        
        findViewById<androidx.cardview.widget.CardView>(R.id.cardGallery).setOnClickListener {
            startGalleryActivity()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme_settings -> {
                startThemeSettingsActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun startThemeSettingsActivity() {
        val intent = Intent(this, ThemeSettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                startCameraActivity()
            }
            else -> {
                requestCameraPermissionLauncher.launch(CAMERA_PERMISSION)
            }
        }
    }
    
    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                startAudioRecorderActivity()
            }
            else -> {
                requestAudioPermissionLauncher.launch(RECORD_AUDIO_PERMISSION)
            }
        }
    }
    
    private fun showPermissionDeniedMessage(permissionType: String) {
        Toast.makeText(
            this,
            "Se requiere permiso de $permissionType para usar esta función",
            Toast.LENGTH_LONG
        ).show()
    }
    
    private fun startCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
    
    private fun startAudioRecorderActivity() {
        val intent = Intent(this, AudioRecorderActivity::class.java)
        startActivity(intent)
    }
    
    private fun startGalleryActivity() {
        val intent = Intent(this, GalleryActivity::class.java)
        startActivity(intent)
    }
}
