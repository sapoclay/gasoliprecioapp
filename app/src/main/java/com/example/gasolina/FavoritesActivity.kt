package com.example.gasolina

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FavoritesActivity : AppCompatActivity() {

    private lateinit var adapter: FavoritesAdapter
    private lateinit var favoritesManager: FavoritesManager
    private var allStations: List<Station> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // Configurar la ActionBar con layout personalizado PRIMERO
        supportActionBar?.apply {
            displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.actionbar_custom)
            elevation = 4f
        }

        // Cambiar el título en el custom view
        supportActionBar?.customView?.findViewById<TextView>(R.id.actionbar_title)?.text = "⭐ Favoritas"

        // Habilitar el botón de navegación después
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val recycler = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        val btnBack = findViewById<Button>(R.id.btnBack)

        favoritesManager = FavoritesManager(this)

        adapter = FavoritesAdapter(
            mutableListOf(),
            { station ->
                // Click en la estación: mostrar detalles
                showStationDetails(station)
            },
            { station ->
                // Click en eliminar: confirmar y eliminar
                confirmDeleteFavorite(station)
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        // Obtener las estaciones desde el repositorio compartido
        allStations = StationsRepository.getAllStations()
        loadFavorites()
    }

    private fun confirmDeleteFavorite(station: Station) {
        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Eliminar favorito")
            .setMessage("¿Deseas eliminar '${station.name}' de tus favoritos?")
            .setPositiveButton("Eliminar") { _, _ ->
                favoritesManager.removeFavorite(station.id)
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                loadFavorites()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadFavorites() {
        val favoriteIds = favoritesManager.getFavorites()
        val favoriteStations = allStations.filter { favoriteIds.contains(it.id) }

        if (favoriteStations.isEmpty()) {
            Toast.makeText(this, "No tienes estaciones favoritas", Toast.LENGTH_SHORT).show()
        }

        adapter.setStations(favoriteStations)
    }

    private fun showStationDetails(station: Station) {
        // Inflar el layout personalizado
        val dialogView = layoutInflater.inflate(R.layout.dialog_station_details, null)

        // Configurar los datos
        dialogView.findViewById<TextView>(R.id.tvDialogName).text = station.name ?: "-"
        dialogView.findViewById<TextView>(R.id.tvDialogLocality).text = station.locality ?: "-"
        dialogView.findViewById<TextView>(R.id.tvDialogProvince).text = station.province ?: "-"
        dialogView.findViewById<TextView>(R.id.tvDialogAutonomousCommunity).text = station.autonomousCommunity ?: "-"
        dialogView.findViewById<TextView>(R.id.tvDialogAddress).text = station.address ?: "-"

        // Configurar click en la dirección para abrir Google Maps
        val layoutAddress = dialogView.findViewById<LinearLayout>(R.id.layoutAddress)
        layoutAddress.setOnClickListener {
            openInGoogleMaps(station)
        }

        // Configurar los precios
        val layoutPrices = dialogView.findViewById<LinearLayout>(R.id.layoutPrices)
        val tvNoPrices = dialogView.findViewById<TextView>(R.id.tvNoPrices)

        if (station.prices.isEmpty()) {
            tvNoPrices.visibility = View.VISIBLE
            layoutPrices.visibility = View.GONE
        } else {
            tvNoPrices.visibility = View.GONE
            layoutPrices.visibility = View.VISIBLE
            layoutPrices.removeAllViews()

            // Agregar cada precio con su layout personalizado
            station.prices.entries.sortedBy { it.key }.forEach { (product, price) ->
                val priceView = layoutInflater.inflate(R.layout.item_price, layoutPrices, false)
                priceView.findViewById<TextView>(R.id.tvPriceProductName).text = product
                priceView.findViewById<TextView>(R.id.tvPriceValue).text = price
                layoutPrices.addView(priceView)
            }
        }

        // Crear y mostrar el diálogo con el tema correcto de Material3
        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .create()
            .show()
    }

    private fun openInGoogleMaps(station: Station) {
        // Construir la dirección completa
        val address = buildString {
            append(station.address ?: "")
            append(", ")
            append(station.locality ?: "")
            append(", ")
            append(station.province ?: "")
            append(", España")
        }

        // Crear URI para Google Maps con la dirección
        val encodedAddress = android.net.Uri.encode(address)
        val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=$encodedAddress")

        // Crear Intent para abrir Google Maps
        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Verificar si Google Maps está instalado
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Si Google Maps no está instalado, abrir en el navegador
            val browserUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress")
            val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }

        Toast.makeText(this, "Abriendo en Google Maps...", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
