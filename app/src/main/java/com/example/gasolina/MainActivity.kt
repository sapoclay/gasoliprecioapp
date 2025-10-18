package com.example.gasolina

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: StationsAdapter
    private lateinit var favoritesManager: FavoritesManager
    private var allStations: List<Station> = emptyList()
    private val url = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/"

    // Mapeo de IDs de comunidades autónomas a sus nombres
    private val ccaaMap = mapOf(
        "01" to "Andalucía",
        "02" to "Aragón",
        "03" to "Asturias, Principado de",
        "04" to "Balears, Illes",
        "05" to "Canarias",
        "06" to "Cantabria",
        "07" to "Castilla y León",
        "08" to "Castilla-La Mancha",
        "09" to "Cataluña",
        "10" to "Comunitat Valenciana",
        "11" to "Extremadura",
        "12" to "Galicia",
        "13" to "Madrid, Comunidad de",
        "14" to "Murcia, Región de",
        "15" to "Navarra, Comunidad Foral de",
        "16" to "País Vasco",
        "17" to "Rioja, La",
        "18" to "Ceuta",
        "19" to "Melilla"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnFavorites = findViewById<Button>(R.id.btnFavorites)
        val btnAbout = findViewById<Button>(R.id.btnAbout)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        favoritesManager = FavoritesManager(this)

        // Configurar el SearchView para que muestre el placeholder
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = "Buscar gasolinera"

        // Configurar la ActionBar con layout personalizado
        supportActionBar?.apply {
            displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.actionbar_custom)
            elevation = 4f
        }

        adapter = StationsAdapter(mutableListOf(), favoritesManager, { station ->
            showStationDetails(station)
        })
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        btnFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        btnAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val stations = fetchStations()
            progressBar.visibility = View.GONE

            if (stations != null) {
                allStations = stations
                StationsRepository.setStations(stations)
                adapter.setStations(stations)
            } else {
                Toast.makeText(this@MainActivity, "Error al cargar datos", Toast.LENGTH_LONG).show()
            }
        }
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
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Verificar si Google Maps está instalado
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Si Google Maps no está instalado, abrir en el navegador
            val browserUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }

        Toast.makeText(this, "Abriendo en Google Maps...", Toast.LENGTH_SHORT).show()
    }

    private suspend fun fetchStations(): List<Station>? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
            }

            try {
                val code = conn.responseCode
                if (code != HttpURLConnection.HTTP_OK) return@withContext null

                val text = conn.inputStream.bufferedReader().use(BufferedReader::readText)

                // Parse JSON
                val root = JSONObject(text)

                val array = extractArray(root) ?: return@withContext null

                val list = mutableListOf<Station>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue

                    // DEBUG: Imprimir todos los campos del primer objeto para ver qué campos están disponibles
                    if (i == 0) {
                        android.util.Log.d("GasolinaAPI", "=== Campos disponibles en el JSON ===")
                        val keys = obj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = obj.opt(key)
                            android.util.Log.d("GasolinaAPI", "Campo: '$key' = '$value'")
                        }
                        android.util.Log.d("GasolinaAPI", "====================================")
                    }

                    // Extraer ID único de la estación
                    val id = getString(obj, listOf("IDEESS", "IDEstacion", "id", "Id")) ?: "station_$i"
                    val name = getString(obj, listOf("Rótulo", "Rotulo", "rotulo"))
                    val locality = getString(obj, listOf("Municipio", "municipio", "Localidad", "localidad"))
                    val address = getString(obj, listOf("Dirección", "Direccion", "direccion"))
                    val province = getString(obj, listOf("Provincia", "provincia"))

                    // Obtener el ID de la comunidad autónoma y convertirlo a nombre
                    val ccaaId = getString(obj, listOf("IDCCAA", "IDComAutonoma", "Id CCAA"))
                    val autonomousCommunity = if (ccaaId != null) {
                        // Formatear el ID con ceros a la izquierda si es necesario
                        val formattedId = ccaaId.padStart(2, '0')
                        ccaaMap[formattedId] ?: ccaaId
                    } else {
                        // Si no hay ID, intentar buscar directamente el nombre
                        getString(obj, listOf(
                            "Comunidad Autónoma",
                            "ComunidadAutonoma",
                            "C.C.A.A.",
                            "CCAA"
                        )) ?: "Sin comunidad"
                    }

                    // Extraer coordenadas geográficas
                    val latitudeStr = getString(obj, listOf("Latitud", "latitud", "lat"))
                    val longitudeStr = getString(obj, listOf("Longitud (WGS84)", "Longitud", "longitud", "lon", "lng"))

                    // Convertir coordenadas (la API usa coma como separador decimal)
                    val latitude = latitudeStr?.replace(",", ".")?.toDoubleOrNull()
                    val longitude = longitudeStr?.replace(",", ".")?.toDoubleOrNull()

                    // Extraer todos los precios de productos
                    val prices = extractPrices(obj)

                    list.add(Station(
                        id = id,
                        name = name ?: "-",
                        locality = locality ?: "-",
                        address = address ?: "-",
                        province = province ?: "Sin provincia",
                        autonomousCommunity = autonomousCommunity ?: "Sin comunidad",
                        prices = prices,
                        latitude = latitude,
                        longitude = longitude
                    ))
                }

                // Ordenar por provincia y luego por localidad
                list.sortWith(compareBy({ it.province ?: "" }, { it.locality ?: "" }, { it.name ?: "" }))
                return@withContext list
            } finally {
                conn.disconnect()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun extractPrices(obj: JSONObject): Map<String, String> {
        val prices = mutableMapOf<String, String>()

        // Lista de campos de precios según la API del Ministerio
        // Formato: Campo en API -> Nombre a mostrar
        val priceFields = listOf(
            // Gasolinas
            "Precio Gasolina 95 E5" to "Gasolina 95 E5",
            "Precio Gasolina 98 E5" to "Gasolina 98 E5",
            "Precio Gasolina 95 E10" to "Gasolina 95 E10",
            "Precio Gasolina 98 E10" to "Gasolina 98 E10",
            "Precio Gasolina 95 E5 Premium" to "Gasolina 95 E5 Premium",

            // Gasóleos
            "Precio Gasóleo A" to "Gasóleo A",
            "Precio Gasóleo B" to "Gasóleo B",
            "Precio Gasóleo Premium" to "Gasóleo Premium",

            // Biocarburantes
            "Precio Biodiesel" to "Biodiesel",
            "Precio Bioetanol" to "Bioetanol",

            // Gases
            "Precio Gases licuados del petróleo" to "GLP (Gases Licuados del Petróleo)",
            "Precio Gas Natural Comprimido" to "Gas Natural Comprimido",
            "Precio Gas Natural Licuado" to "Gas Natural Licuado",

            // Otros
            "Precio Hidrogeno" to "Hidrógeno"
        )

        for ((apiField, displayName) in priceFields) {
            // Intentar obtener el valor con el nombre exacto y variaciones sin espacios/acentos
            val value = getString(obj, listOf(
                apiField,
                apiField.replace(" ", ""),
                apiField.replace("ó", "o").replace("Ó", "O"),
                apiField.replace(" ", "").replace("ó", "o").replace("Ó", "O")
            ))

            if (!value.isNullOrBlank() && value != "-" && value != "0" && value.replace(",", ".").toDoubleOrNull() != null) {
                // Normalizar el formato del precio (reemplazar coma por punto si es necesario)
                val normalizedPrice = value.replace(",", ".")
                prices[displayName] = "$normalizedPrice €/litro"
            }
        }

        return prices
    }

    private fun extractArray(root: JSONObject): JSONArray? {
        val candidates = listOf("ListaEESSPrecio", "ListadoEESSPrecio", "ListaEESS", "List")
        for (c in candidates) {
            if (root.has(c)) {
                val v = root.opt(c)
                if (v is JSONArray) return v
            }
        }
        // Buscar la primera propiedad que sea array
        val keys = root.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = root.opt(k)
            if (v is JSONArray) return v
        }
        return null
    }

    private fun getString(obj: JSONObject, keys: List<String>): String? {
        for (k in keys) {
            if (obj.has(k) && !obj.isNull(k)) {
                val v = obj.opt(k)
                if (v != null && v.toString().isNotBlank()) return v.toString().trim()
            }
        }
        return null
    }
}
