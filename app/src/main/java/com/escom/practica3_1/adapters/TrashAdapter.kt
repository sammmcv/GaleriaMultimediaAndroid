package com.escom.practica3_1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.escom.practica3_1.R
import com.escom.practica3_1.models.TrashItem
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class TrashAdapter(
    private val context: Context,
    private var items: List<TrashItem>,
    private val listener: TrashItemListener
) : RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {
    
    interface TrashItemListener {
        fun onRestore(item: TrashItem)
        fun onDelete(item: TrashItem)
    }
    
    fun updateItems(newItems: List<TrashItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_trash, parent, false)
        return TrashViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TrashViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class TrashViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewTrash)
        private val textName: TextView = itemView.findViewById(R.id.textTrashName)
        private val textDate: TextView = itemView.findViewById(R.id.textTrashDate)
        private val textExpiry: TextView = itemView.findViewById(R.id.textTrashExpiry)
        private val buttonRestore: MaterialButton = itemView.findViewById(R.id.buttonRestore)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)
        
        fun bind(item: TrashItem) {
            // Formatear fechas
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val deletedDateStr = dateFormat.format(item.deletedDate)
            val expiryDateStr = dateFormat.format(item.expiryDate)
            
            // Configurar vistas
            textName.text = item.name
            textDate.text = "Eliminado: $deletedDateStr"
            textExpiry.text = "Se eliminará: $expiryDateStr"
            
            // Cargar imagen o icono según el tipo
            if (item.type == "image") {
                Glide.with(context)
                    .load(item.uri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageView)
            } else {
                // Icono para audio
                imageView.setImageResource(R.drawable.ic_audio_placeholder)
            }
            
            // Configurar botones
            buttonRestore.setOnClickListener {
                listener.onRestore(item)
            }
            
            buttonDelete.setOnClickListener {
                listener.onDelete(item)
            }
        }
    }
}