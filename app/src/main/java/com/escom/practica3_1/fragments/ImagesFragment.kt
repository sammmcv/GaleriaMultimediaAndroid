package com.escom.practica3_1.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.escom.practica3_1.ImageViewerActivity
import com.escom.practica3_1.R
import com.escom.practica3_1.utils.FileManager
import java.io.File

class ImagesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var fileManager: FileManager
    private lateinit var adapter: ImageAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Corregir los IDs para que coincidan con los del layout
        recyclerView = view.findViewById(R.id.recyclerViewImages)
        emptyText = view.findViewById(R.id.textEmptyImages)
        
        fileManager = FileManager(requireContext())
        
        // Configurar RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = ImageAdapter(emptyList()) { imageFile ->
            openImageViewer(imageFile)
        }
        recyclerView.adapter = adapter
        
        // Cargar imágenes
        loadImages()
    }
    
    override fun onResume() {
        super.onResume()
        loadImages()
    }
    
    private fun loadImages() {
        // Usar el método correcto de FileManager
        val imageFiles = fileManager.getImageFiles()
        adapter.updateImages(imageFiles)
        
        if (imageFiles.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun openImageViewer(imageFile: File) {
        // Fix: Correct Intent creation syntax
        val intent = Intent(requireContext(), ImageViewerActivity::class.java)
        intent.putExtra("IMAGE_PATH", imageFile.absolutePath)
        startActivity(intent)
    }
    
    // Clase interna para el adaptador
    inner class ImageAdapter(
        private var images: List<File>,
        private val onImageClick: (File) -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
        
        fun updateImages(newImages: List<File>) {
            images = newImages
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
            return ImageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imageFile = images[position]
            holder.bind(imageFile)
        }
        
        override fun getItemCount(): Int = images.size
        
        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            
            fun bind(imageFile: File) {
                Glide.with(requireContext())
                    .load(imageFile)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(imageView)
                
                itemView.setOnClickListener {
                    onImageClick(imageFile)
                }
            }
        }
    }
}