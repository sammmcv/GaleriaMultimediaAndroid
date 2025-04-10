package com.escom.practica3_1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.escom.practica3_1.R
import com.escom.practica3_1.dialogs.FileMetadataDialog
import com.escom.practica3_1.utils.MetadataManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioAdapter(
    private val context: Context,
    private var audioFiles: List<File>,
    private val listener: AudioClickListener
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {
    
    private val metadataManager = MetadataManager(context)
    
    interface AudioClickListener {
        fun onAudioClick(audioFile: File)
        fun onAudioLongClick(audioFile: File): Boolean
        fun onShowMetadata(audioFile: File)
    }
    
    fun updateAudioFiles(newAudioFiles: List<File>) {
        audioFiles = newAudioFiles
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = audioFiles[position]
        holder.bind(audioFile)
    }
    
    override fun getItemCount(): Int = audioFiles.size
    
    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconAudio)
        private val textName: TextView = itemView.findViewById(R.id.textAudioName)
        private val textDate: TextView = itemView.findViewById(R.id.textAudioDate)
        private val infoButton: ImageView = itemView.findViewById(R.id.buttonInfo)
        
        fun bind(audioFile: File) {
            // Configurar icono
            iconView.setImageResource(R.drawable.ic_audio_placeholder)
            
            // Configurar nombre
            textName.text = audioFile.name
            
            // Configurar fecha
            val date = Date(audioFile.lastModified())
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            textDate.text = dateFormat.format(date)
            
            // Configurar click listeners
            itemView.setOnClickListener {
                listener.onAudioClick(audioFile)
            }
            
            itemView.setOnLongClickListener {
                listener.onAudioLongClick(audioFile)
            }
            
            // Configurar botón de información
            infoButton.setOnClickListener {
                listener.onShowMetadata(audioFile)
            }
        }
    }
}