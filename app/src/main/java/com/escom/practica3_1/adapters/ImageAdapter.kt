package com.escom.practica3_1.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.escom.practica3_1.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageAdapter(
    private val context: Context,
    private var images: List<File>,
    private val listener: ImageClickListener
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    interface ImageClickListener {
        fun onImageClick(imageFile: File)
        fun onImageLongClick(imageFile: File): Boolean
        fun onShowMetadata(imageFile: File)
    }

    fun updateImages(newImages: List<File>) {
        images = newImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageFile = images[position]
        holder.bind(imageFile)
    }

    override fun getItemCount(): Int = images.size
    
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val infoButton: ImageView = itemView.findViewById(R.id.buttonInfo)
    
        fun bind(imageFile: File) {
            // Load image with Glide
            Glide.with(context)
                .load(imageFile)
                .centerCrop()
                .into(imageView)
    
            // Set click listener for the image
            imageView.setOnClickListener {
                listener.onImageClick(imageFile)
            }
    
            // Set long click listener for the entire item
            itemView.setOnLongClickListener {
                listener.onImageLongClick(imageFile)
            }
    
            // Set click listener for the info button with explicit handling
            infoButton.setOnClickListener {
                Log.d("ImageAdapter", "Info button clicked for: ${imageFile.name}")
                listener.onShowMetadata(imageFile)
            }
            
            // Make sure the info button is visible and clickable
            infoButton.visibility = View.VISIBLE
            infoButton.isClickable = true
            infoButton.isFocusable = true
        }
    }
}