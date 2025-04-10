package com.escom.practica3_1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.escom.practica3_1.adapters.TrashAdapter
import com.escom.practica3_1.models.TrashItem
import com.escom.practica3_1.utils.TrashManager
import java.text.SimpleDateFormat
import java.util.*

class TrashFragment : Fragment(), TrashAdapter.TrashItemListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var trashAdapter: TrashAdapter
    private lateinit var trashManager: TrashManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trash, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerViewTrash)
        trashManager = TrashManager(requireContext())
        
        // Configurar el RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        trashAdapter = TrashAdapter(requireContext(), emptyList(), this)
        recyclerView.adapter = trashAdapter
        
        // Limpiar elementos expirados
        trashManager.cleanupExpiredItems()
        
        // Cargar elementos de la papelera
        loadTrashItems()
        
        return view
    }
    
    private fun loadTrashItems() {
        val items = trashManager.getTrashItems()
        trashAdapter.updateItems(items)
    }
    
    override fun onRestore(item: TrashItem) {
        if (trashManager.restoreFromTrash(item)) {
            Toast.makeText(context, "Elemento restaurado", Toast.LENGTH_SHORT).show()
            loadTrashItems()
        } else {
            Toast.makeText(context, "Error al restaurar el elemento", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDelete(item: TrashItem) {
        if (trashManager.deletePermanently(item)) {
            Toast.makeText(context, "Elemento eliminado permanentemente", Toast.LENGTH_SHORT).show()
            loadTrashItems()
        } else {
            Toast.makeText(context, "Error al eliminar el elemento", Toast.LENGTH_SHORT).show()
        }
    }
}