package com.escom.practica3_1

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.escom.practica3_1.adapters.AudioAdapter
import com.escom.practica3_1.dialogs.FileMetadataDialog  // Add this import
import com.escom.practica3_1.utils.FileManager
import com.escom.practica3_1.utils.TrashManager
import java.io.File

class AudioFragment : Fragment(), AudioAdapter.AudioClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var audioAdapter: AudioAdapter
    private lateinit var fileManager: FileManager
    private lateinit var trashManager: TrashManager
    private var mediaPlayer: MediaPlayer? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerViewAudio)
        fileManager = FileManager(requireContext())
        trashManager = TrashManager(requireContext())
        
        // Configurar el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        audioAdapter = AudioAdapter(requireContext(), emptyList(), this)
        recyclerView.adapter = audioAdapter
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        loadAudioFiles()
    }
    
    private fun loadAudioFiles() {
        val audioFiles = fileManager.getAudioFiles()
        audioAdapter.updateAudioFiles(audioFiles)
    }
    
    override fun onAudioClick(audioFile: File) {
        // Reproducir el audio directamente en lugar de abrir una nueva actividad
        playAudio(audioFile)
    }
    
    private fun playAudio(audioFile: File) {
        try {
            // Si ya hay algo reproduciéndose, detenerlo
            stopPlayback()
            
            // Crear y configurar el MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
                
                setOnCompletionListener {
                    stopPlayback()
                }
            }
            
            Toast.makeText(context, "Reproduciendo: ${audioFile.name}", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error al reproducir el audio", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
    
    override fun onAudioLongClick(audioFile: File): Boolean {
        // Mover a la papelera
        if (trashManager.moveToTrash(audioFile, "audio")) {
            Toast.makeText(context, "Audio movido a la papelera", Toast.LENGTH_SHORT).show()
            loadAudioFiles() // Recargar la lista
            return true
        } else {
            Toast.makeText(context, "Error al mover a la papelera", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    
    // Add the missing method implementation
    override fun onShowMetadata(audioFile: File) {
        val dialog = FileMetadataDialog.newInstance(audioFile)
        dialog.show(childFragmentManager, "metadata_dialog")
    }
    
    override fun onPause() {
        super.onPause()
        stopPlayback()
    }
}