package com.escom.practica3_1

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.escom.practica3_1.utils.FileManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import java.io.File
import java.io.IOException

class AudioRecorderActivity : AppCompatActivity() {
    private lateinit var recordButton: FloatingActionButton
    private lateinit var timerButton: MaterialButton
    private lateinit var sensitivityButton: MaterialButton
    private lateinit var timerOptions: LinearLayout
    private lateinit var sensitivityOptions: LinearLayout
    private lateinit var timerCountdown: TextView
    private lateinit var sensitivitySlider: Slider
    private lateinit var recordingStatus: TextView
    private lateinit var fileManager: FileManager
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    
    // Configuración de grabación
    private var timerDuration = 0 // 0 significa sin temporizador
    private var sensitivityLevel = 50 // Valor por defecto (0-100)
    private var countDownTimer: CountDownTimer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_recorder)
        
        // Inicializar el administrador de archivos
        fileManager = FileManager(this)
        
        // Inicializar vistas
        recordButton = findViewById(R.id.record_button)
        timerButton = findViewById(R.id.timer_button)
        sensitivityButton = findViewById(R.id.sensitivity_button)
        timerOptions = findViewById(R.id.timer_options)
        sensitivityOptions = findViewById(R.id.sensitivity_options)
        timerCountdown = findViewById(R.id.timer_countdown)
        sensitivitySlider = findViewById(R.id.sensitivity_slider)
        recordingStatus = findViewById(R.id.recording_status)
        
        // Solicitar permisos de micrófono
        if (allPermissionsGranted()) {
            setupAudioRecorder()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        
        // Configurar botón de grabación
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                if (timerDuration > 0) {
                    startCountdownAndRecord()
                } else {
                    startRecording()
                }
            }
        }
        
        // Configurar botón de temporizador
        timerButton.setOnClickListener {
            if (timerOptions.visibility == View.VISIBLE) {
                timerOptions.visibility = View.GONE
            } else {
                timerOptions.visibility = View.VISIBLE
                sensitivityOptions.visibility = View.GONE
            }
        }
        
        // Configurar botón de sensibilidad
        sensitivityButton.setOnClickListener {
            if (sensitivityOptions.visibility == View.VISIBLE) {
                sensitivityOptions.visibility = View.GONE
            } else {
                sensitivityOptions.visibility = View.VISIBLE
                timerOptions.visibility = View.GONE
            }
        }
        
        // Configurar opciones de temporizador
        setupTimerOptions()
        
        // Configurar opciones de sensibilidad
        setupSensitivityOptions()
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
    
    private fun setupSensitivityOptions() {
        sensitivitySlider.addOnChangeListener { _, value, _ ->
            sensitivityLevel = value.toInt()
            updateSensitivityButtonText()
        }
    }
    
    private fun updateSensitivityButtonText() {
        sensitivityButton.text = "${sensitivityLevel}%"
    }
    
    private fun setupAudioRecorder() {
        // Inicializar el MediaRecorder
        try {
            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear MediaRecorder", e)
            Toast.makeText(this, "Error al inicializar el grabador de audio", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun startCountdownAndRecord() {
        timerCountdown.visibility = View.VISIBLE
        recordButton.isEnabled = false
        
        countDownTimer = object : CountDownTimer(timerDuration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000 + 1
                timerCountdown.text = secondsRemaining.toString()
            }
            
            override fun onFinish() {
                timerCountdown.visibility = View.GONE
                recordButton.isEnabled = true
                startRecording()
            }
        }.start()
    }
    
    private fun startRecording() {
        try {
            // Crear archivo para guardar el audio
            audioFile = fileManager.createAudioFile()
            
            // Configurar el MediaRecorder
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                
                // Aplicar nivel de sensibilidad (ganancia)
                // Nota: La API de MediaRecorder no tiene un método directo para ajustar la sensibilidad
                // En una implementación real, esto podría requerir procesamiento de audio más avanzado
                
                prepare()
                start()
                
                isRecording = true
                updateRecordingUI(true)
                
                // Si hay un temporizador de grabación, iniciarlo
                if (timerDuration > 0) {
                    startRecordingTimer()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error al preparar MediaRecorder", e)
            Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startRecordingTimer() {
        // Esta función podría implementarse para detener automáticamente la grabación después de un tiempo
        // Por ahora, no implementamos esta funcionalidad
    }
    
    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
            }
            
            isRecording = false
            updateRecordingUI(false)
            
            // Notificar al usuario que la grabación se ha guardado
            audioFile?.let {
                val msg = "Grabación guardada: ${it.name}"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            }
            
            // Aquí podríamos abrir el reproductor de audio o volver a la galería
            finish()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al detener la grabación", e)
            Toast.makeText(this, "Error al finalizar la grabación", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateRecordingUI(isRecording: Boolean) {
        if (isRecording) {
            recordButton.setImageResource(android.R.drawable.ic_media_pause)
            recordingStatus.text = "Grabando..."
            recordingStatus.visibility = View.VISIBLE
        } else {
            recordButton.setImageResource(android.R.drawable.ic_btn_speak_now)
            recordingStatus.visibility = View.GONE
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
                setupAudioRecorder()
            } else {
                Toast.makeText(this, "Se requieren permisos para usar el micrófono", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
        countDownTimer?.cancel()
    }
    
    companion object {
        private const val TAG = "AudioRecorderActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    }
}