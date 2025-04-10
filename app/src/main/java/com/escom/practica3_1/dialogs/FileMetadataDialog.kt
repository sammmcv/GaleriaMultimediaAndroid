package com.escom.practica3_1.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.escom.practica3_1.R
import com.escom.practica3_1.utils.FileMetadata
import com.escom.practica3_1.utils.MetadataManager
import java.io.File

class FileMetadataDialog : DialogFragment() {
    private lateinit var file: File
    private lateinit var metadataManager: MetadataManager
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        metadataManager = MetadataManager(context)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val file = arguments?.getSerializable(ARG_FILE) as? File
            ?: throw IllegalArgumentException("Debe proporcionar un archivo")
        
        this.file = file
        
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_file_metadata, null)
        
        setupViews(view)
        
        return builder.setView(view)
            .setTitle("Detalles del archivo")
            .setPositiveButton("Cerrar") { _, _ -> dismiss() }
            .create()
    }
    
    private fun setupViews(view: View) {
        val metadata = metadataManager.getMetadata(file)
        
        // Configurar vistas básicas
        view.findViewById<TextView>(R.id.textFileName).text = metadata.fileName
        view.findViewById<TextView>(R.id.textFileSize).text = metadata.fileSize
        view.findViewById<TextView>(R.id.textCreationDate).text = metadata.creationDate
        view.findViewById<TextView>(R.id.textLocation).text = metadata.location
        
        // Configurar duración (solo para audio)
        val layoutDuration = view.findViewById<LinearLayout>(R.id.layoutDuration)
        if (metadata.duration.isNotEmpty()) {
            layoutDuration.visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.textDuration).text = metadata.duration
        }
        
        // Configurar resolución (solo para imágenes)
        val layoutResolution = view.findViewById<LinearLayout>(R.id.layoutResolution)
        if (metadata.resolution.isNotEmpty()) {
            layoutResolution.visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.textResolution).text = metadata.resolution
        }
        
        // Configurar información adicional
        if (metadata.additionalInfo.isNotEmpty()) {
            view.findViewById<TextView>(R.id.textAdditionalInfoTitle).visibility = View.VISIBLE
            val layoutAdditionalInfo = view.findViewById<LinearLayout>(R.id.layoutAdditionalInfo)
            
            for ((key, value) in metadata.additionalInfo) {
                // Fix: Use LayoutInflater from context instead of direct inflater reference
                val infoView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_metadata_info, layoutAdditionalInfo, false)
                infoView.findViewById<TextView>(R.id.textInfoKey).text = key
                infoView.findViewById<TextView>(R.id.textInfoValue).text = value
                layoutAdditionalInfo.addView(infoView)
            }
        }
    }
    
    companion object {
        private const val ARG_FILE = "arg_file"
        
        fun newInstance(file: File): FileMetadataDialog {
            val args = Bundle().apply {
                putSerializable(ARG_FILE, file)
            }
            return FileMetadataDialog().apply {
                arguments = args
            }
        }
    }
}