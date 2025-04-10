package com.escom.practica3_1

import java.io.FileOutputStream
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.escom.practica3_1.utils.FileManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RadioButton
import android.os.CountDownTimer
import android.widget.TextView
import androidx.camera.core.ImageCapture.OutputFileOptions
import java.io.File

// Añadir estas importaciones al principio del archivo
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.graphics.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

// Añadir esta importación junto con las demás
import android.graphics.drawable.BitmapDrawable

// Definir un enum para los tipos de filtros
enum class FilterType {
    NONE,
    GRAYSCALE,
    SEPIA,
    NEGATIVE
}

class CameraActivity : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView
    private lateinit var captureButton: FloatingActionButton
    private lateinit var flashButton: MaterialButton
    private lateinit var timerButton: MaterialButton
    private lateinit var filterButton: MaterialButton
    private lateinit var filterOptions: LinearLayout
    private lateinit var timerOptions: LinearLayout
    private lateinit var timerCountdown: TextView
    private lateinit var fileManager: FileManager
    
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    
    // Estado de la cámara
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var timerDuration = 0 // 0 significa sin temporizador
    
    // Añadir las variables faltantes
    private lateinit var imageAnalysis: ImageAnalysis
    private var currentFilterType = FilterType.NONE
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        
        // Inicializar el administrador de archivos
        fileManager = FileManager(this)
        
        // Inicializar vistas
        viewFinder = findViewById(R.id.viewFinder)
        captureButton = findViewById(R.id.capture_button)
        flashButton = findViewById(R.id.flash_button)
        timerButton = findViewById(R.id.timer_button)
        filterButton = findViewById(R.id.filter_button)
        filterOptions = findViewById(R.id.filter_options)
        timerOptions = findViewById(R.id.timer_options)
        timerCountdown = findViewById(R.id.timer_countdown)
        
        // Solicitar permisos de cámara
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        
        // Configurar botón de captura
        captureButton.setOnClickListener {
            if (timerDuration > 0) {
                startCountdownAndTakePhoto()
            } else {
                takePhoto()
            }
        }
        
        // Configurar botón de flash
        flashButton.setOnClickListener {
            cycleFlashMode()
        }
        
        // Configurar botón de temporizador
        timerButton.setOnClickListener {
            if (timerOptions.visibility == View.VISIBLE) {
                timerOptions.visibility = View.GONE
            } else {
                timerOptions.visibility = View.VISIBLE
                filterOptions.visibility = View.GONE
            }
        }
        
        // Configurar botón de filtros
        filterButton.setOnClickListener {
            if (filterOptions.visibility == View.VISIBLE) {
                filterOptions.visibility = View.GONE
            } else {
                filterOptions.visibility = View.VISIBLE
                timerOptions.visibility = View.GONE
            }
        }
        
        // Configurar opciones de temporizador
        setupTimerOptions()
        
        // Configurar opciones de filtro
        setupFilterOptions()
        
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    private fun setupTimerOptions() {
        val timerRadioGroup = findViewById<RadioGroup>(R.id.timer_radio_group)
        timerRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            timerDuration = when (checkedId) {
                R.id.timer_off -> 0
                R.id.timer_3s -> 3
                R.id.timer_5s -> 5
                R.id.timer_10s -> 10
                else -> 0
            }
            timerOptions.visibility = View.GONE
            updateTimerButtonText()
        }
    }
    
    private fun updateTimerButtonText() {
        timerButton.text = if (timerDuration > 0) "${timerDuration}s" else "OFF"
    }
    
    private fun setupFilterOptions() {
        val filterRadioGroup = findViewById<RadioGroup>(R.id.filter_radio_group)
        filterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilterType = when (checkedId) {
                R.id.filter_none -> FilterType.NONE
                R.id.filter_grayscale -> FilterType.GRAYSCALE
                R.id.filter_sepia -> FilterType.SEPIA
                R.id.filter_negative -> FilterType.NEGATIVE
                else -> FilterType.NONE
            }
            filterOptions.visibility = View.GONE
            
            // Make sure viewFinder is initialized before using it
            if (::viewFinder.isInitialized) {
                // Update the image analyzer with the new filter
                updateCameraWithFilter(currentFilterType)
            }
        }
    }
    
    // Add this helper method to update the camera with the selected filter
    private fun updateCameraWithFilter(filterType: FilterType) {
    // This method will be called when a filter is selected
    // It recreates the image analyzer with the new filter
    if (::imageAnalysis.isInitialized && ::cameraExecutor.isInitialized) {
        imageAnalysis.clearAnalyzer()
        imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalyzer(filterType))
    } else {
        // If camera isn't started yet, the filter will be applied when startCamera() is called
        Log.d(TAG, "Camera not initialized yet, filter will be applied when camera starts")
    }
    }
    
    private fun applyFilter(filterName: String) {
        Toast.makeText(this, "Filtro $filterName aplicado", Toast.LENGTH_SHORT).show()
    }
    
    private fun startCountdownAndTakePhoto() {
        timerCountdown.visibility = View.VISIBLE
        captureButton.isEnabled = false
        
        object : CountDownTimer(timerDuration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000 + 1
                timerCountdown.text = secondsRemaining.toString()
            }
            
            override fun onFinish() {
                timerCountdown.visibility = View.GONE
                captureButton.isEnabled = true
                takePhoto()
            }
        }.start()
    }
    
    private fun cycleFlashMode() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> {
                flashButton.text = "AUTO"
                ImageCapture.FLASH_MODE_AUTO
            }
            ImageCapture.FLASH_MODE_AUTO -> {
                flashButton.text = "ON"
                ImageCapture.FLASH_MODE_ON
            }
            else -> {
                flashButton.text = "OFF"
                ImageCapture.FLASH_MODE_OFF
            }
        }
        
        imageCapture?.flashMode = flashMode
    }
    
    private fun takePhoto() {
        // Verificar que la captura de imagen está inicializada
        val imageCapture = imageCapture ?: return
        
        // Crear opciones de salida para el archivo
        val photoFile = fileManager.createImageFile()
        val outputOptions = OutputFileOptions.Builder(photoFile).build()
        
        // Capturar la foto
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Si hay un filtro aplicado, procesamos la imagen
                    if (currentFilterType != FilterType.NONE && ::imageAnalysis.isInitialized) {
                        // Procesar la imagen con el filtro en un hilo secundario
                        Thread {
                            try {
                                // Cargar la imagen original
                                val originalBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                
                                // Aplicar el filtro seleccionado
                                val filteredBitmap = when (currentFilterType) {
                                    FilterType.GRAYSCALE -> applyGrayscaleFilter(originalBitmap)
                                    FilterType.SEPIA -> applySepiaFilter(originalBitmap)
                                    FilterType.NEGATIVE -> applyNegativeFilter(originalBitmap)
                                    else -> originalBitmap
                                }
                                
                                // Guardar la imagen filtrada
                                val outputStream = FileOutputStream(photoFile)
                                filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                outputStream.flush()
                                outputStream.close()
                                
                                // Notificar en el hilo principal
                                runOnUiThread {
                                    Toast.makeText(baseContext, "Foto guardada: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "Foto guardada con filtro: ${photoFile.absolutePath}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al aplicar filtro a la imagen guardada: ${e.message}", e)
                                runOnUiThread {
                                    Toast.makeText(baseContext, "Foto guardada sin filtro: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.start()
                    } else {
                        // Si no hay filtro, simplemente notificamos que se guardó
                        Toast.makeText(baseContext, "Foto guardada: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Foto guardada: ${photoFile.absolutePath}")
                    }
                    
                    
                    // Opcional: cerrar la actividad después de tomar la foto
                    // finish()
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Error al capturar foto: ${exception.message}", exception)
                    Toast.makeText(baseContext, "Error al capturar foto: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            // Obtener el proveedor de cámara
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            // Configurar la vista previa
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            
            // Configurar la captura de imagen
            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .build()
            
            // Configurar el análisis de imagen para aplicar filtros
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer(currentFilterType))
                }
            
            // Seleccionar la cámara trasera como predeterminada
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // Desenlazar cualquier caso de uso anterior
                cameraProvider.unbindAll()
                
                // Enlazar los casos de uso a la cámara
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Error al iniciar la cámara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    // Enumeración para los tipos de filtros
    enum class FilterType {
        NONE, GRAYSCALE, SEPIA, NEGATIVE
    }
    
    // Clase para analizar y aplicar filtros a las imágenes
    // Métodos de filtro implementados en la clase principal
    private fun applyGrayscaleFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
    
    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
    
    private fun applyNegativeFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
    
    private inner class ImageAnalyzer(private val filterType: FilterType) : ImageAnalysis.Analyzer {
        private var lastProcessedTimestamp = 0L
        
        override fun analyze(image: ImageProxy) {
            // Limitar la frecuencia de procesamiento para mejorar el rendimiento
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastProcessedTimestamp < 100) { // Procesar máximo 10 frames por segundo
                image.close()
                return
            }
            lastProcessedTimestamp = currentTime
            
            // Si no hay filtro, simplemente cerrar la imagen y retornar
            if (filterType == FilterType.NONE) {
                image.close()
                return
            }
            
            try {
                // Convertir la imagen a un bitmap
                val bitmap = image.toBitmap()
                
                // Aplicar el filtro seleccionado usando los métodos de la clase principal
                val filteredBitmap = when (filterType) {
                    FilterType.GRAYSCALE -> applyGrayscaleFilter(bitmap)
                    FilterType.SEPIA -> applySepiaFilter(bitmap)
                    FilterType.NEGATIVE -> applyNegativeFilter(bitmap)
                    FilterType.NONE -> bitmap
                }
                
                // Aplicar el bitmap filtrado a la vista previa en el hilo principal
                runOnUiThread {
                    viewFinder.overlay.clear()
                    
                    // Obtener dimensiones de la vista previa
                    val previewWidth = viewFinder.width
                    val previewHeight = viewFinder.height
                    
                    // Crear un bitmap escalado para que coincida con las dimensiones de la vista previa
                    // manteniendo la relación de aspecto
                    val scaledBitmap = scaleBitmapAndKeepRatio(filteredBitmap, previewWidth, previewHeight)
                    
                    // Crear un drawable con el bitmap escalado
                    val drawable = BitmapDrawable(resources, scaledBitmap)
                    
                    // Establecer los límites para que coincida con la vista previa
                    val left = (previewWidth - scaledBitmap.width) / 2
                    val top = (previewHeight - scaledBitmap.height) / 2
                    drawable.setBounds(left, top, left + scaledBitmap.width, top + scaledBitmap.height)
                    
                    // Añadir el drawable al overlay
                    viewFinder.overlay.add(drawable)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al aplicar filtro: ${e.message}", e)
            } finally {
                // Cerrar la imagen para liberar recursos
                image.close()
            }
        }
        
        private fun scaleBitmapAndKeepRatio(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap {
            val matrix = Matrix()
            
            // Calcular la escala
            val widthRatio = reqWidth.toFloat() / bitmap.width
            val heightRatio = reqHeight.toFloat() / bitmap.height
            val scale = Math.max(widthRatio, heightRatio)
            
            // Aplicar la escala
            matrix.postScale(scale, scale)
            
            // Crear el bitmap escalado
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        
        private fun ImageProxy.toBitmap(): Bitmap {
            val buffer = planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Se requieren permisos para usar la cámara", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    override fun onPause() {
        super.onPause()
        // Release camera resources when activity is paused
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera use cases", e)
        }
    }
    
    companion object {
        private const val TAG = "CameraActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}