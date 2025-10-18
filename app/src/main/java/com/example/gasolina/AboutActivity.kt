package com.example.gasolina

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Configurar la ActionBar con layout personalizado PRIMERO
        supportActionBar?.apply {
            displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.actionbar_custom)
            elevation = 4f
        }

        // Cambiar el título en el custom view
        supportActionBar?.customView?.findViewById<TextView>(R.id.actionbar_title)?.text = "ℹ️ Acerca de"

        // Habilitar el botón de navegación después
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val btnGitHub = findViewById<Button>(R.id.btnGitHub)

        // Al pulsar el botón, abrir el navegador con el enlace de GitHub
        btnGitHub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sapoclay/gasoliprecioapp"))
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
