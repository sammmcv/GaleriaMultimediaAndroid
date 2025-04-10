package com.escom.practica3_1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.escom.practica3_1.adapters.ImageAdapter
import com.escom.practica3_1.dialogs.FileMetadataDialog  // Add this import
import com.escom.practica3_1.utils.FileManager
import com.escom.practica3_1.utils.TrashManager
import java.io.File

class ImagesFragment : Fragment(), ImageAdapter.ImageClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var fileManager: FileManager
    private lateinit var trashManager: TrashManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_images, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerViewImages)
        fileManager = FileManager(requireContext())
        trashManager = TrashManager(requireContext())
        
        // Configurar el RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        imageAdapter = ImageAdapter(requireContext(), emptyList(), this)
        recyclerView.adapter = imageAdapter
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        loadImages()
    }
    
    private fun loadImages() {
        val imageFiles = fileManager.getImageFiles()
        imageAdapter.updateImages(imageFiles)
    }
    
    override fun onImageClick(imageFile: File) {
        // Fix: Correct Intent creation syntax
        val intent = Intent(requireContext(), ImageViewerActivity::class.java)
        intent.putExtra("IMAGE_PATH", imageFile.absolutePath)
        startActivity(intent)
    }
    
    override fun onImageLongClick(imageFile: File): Boolean {
        // Mover a la papelera
        if (trashManager.moveToTrash(imageFile, "image")) {
            Toast.makeText(context, "Imagen movida a la papelera", Toast.LENGTH_SHORT).show()
            loadImages() // Recargar la lista
            return true
        } else {
            Toast.makeText(context, "Error al mover a la papelera", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    
    // Add the missing method implementation
    override fun onShowMetadata(imageFile: File) {
        val dialog = FileMetadataDialog.newInstance(imageFile)
        dialog.show(childFragmentManager, "metadata_dialog")
    }
}