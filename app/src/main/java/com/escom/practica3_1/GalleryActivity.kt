package com.escom.practica3_1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
// Cambia esta importación
// import com.escom.practica3_1.fragments.AudioFragment
import com.escom.practica3_1.AudioFragment
import com.escom.practica3_1.fragments.ImagesFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class GalleryActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        
        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Galería"
        
        // Inicializar vistas
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        // Configurar el adaptador del ViewPager
        val pagerAdapter = GalleryPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        // Conectar TabLayout con ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Imágenes"
                1 -> "Audio"
                2 -> "Papelera"
                else -> null
            }
        }.attach()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    // Adaptador para el ViewPager
    private inner class GalleryPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3 // Ahora son 3 pestañas
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ImagesFragment()
                1 -> AudioFragment()
                2 -> TrashFragment()
                else -> ImagesFragment()
            }
        }
    }
}