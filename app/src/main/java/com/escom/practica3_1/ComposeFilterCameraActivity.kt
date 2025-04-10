package com.escom.practica3_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.escom.practica3_1.ui.theme.Practica3Theme
import com.escom.practica3_1.utils.FilterAnalyzer
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class ComposeFilterCameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Using setContent to create a Composable context
        setContent {
            // This is now in a Composable context
            ComposeFilterCameraContent()
        }
    }
}

@Composable
fun ComposeFilterCameraContent() {
    // Using the theme inside a Composable function
    Practica3Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FilterCameraScreen()
        }
    }
}

@Composable
fun FilterCameraScreen(modifier: Modifier = Modifier) {
    var currentFilter by remember { mutableStateOf(FilterAnalyzer.FilterType.NONE) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Mostrar la vista previa de la cámara con el filtro seleccionado
        CameraPreviewWithFilter(filterType = currentFilter)
        
        // Controles para cambiar el filtro
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterButton("Normal") { currentFilter = FilterAnalyzer.FilterType.NONE }
                FilterButton("Grises") { currentFilter = FilterAnalyzer.FilterType.GRAYSCALE }
                FilterButton("Sepia") { currentFilter = FilterAnalyzer.FilterType.SEPIA }
                FilterButton("Negativo") { currentFilter = FilterAnalyzer.FilterType.NEGATIVE }
            }
        }
    }
}

@Composable
fun FilterButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text)
    }
}

@Composable
fun CameraPreviewWithFilter(filterType: FilterAnalyzer.FilterType) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val filterAnalyzer = remember { mutableStateOf<FilterAnalyzer?>(null) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                // Configurar el preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // Configurar el analizador de imágenes para aplicar filtros
                val analyzer = FilterAnalyzer(previewView, filterType)
                filterAnalyzer.value = analyzer
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                    }
                
                // Seleccionar cámara trasera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    // Desenlazar cualquier caso de uso anterior
                    cameraProvider.unbindAll()
                    
                    // Enlazar casos de uso a la cámara
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
            
            previewView
        },
        modifier = Modifier.fillMaxSize(),
        update = {
            // Actualizar el filtro cuando cambie
            filterAnalyzer.value?.updateFilter(filterType)
        }
    )
}