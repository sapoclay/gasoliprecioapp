package com.example.gasolina

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StationsAdapter(
    private var items: MutableList<ListItem>,
    private val favoritesManager: FavoritesManager,
    private val onStationClick: (Station) -> Unit,
    private val onFavoriteChanged: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var originalItems: List<ListItem> = items.toList()
    private val allStationsByProvince = mutableMapOf<String, List<ListItem.StationItem>>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_STATION = 1
    }

    class HeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProvince: TextView = itemView.findViewById(R.id.tvProvince)
        val tvCount: TextView = itemView.findViewById(R.id.tvStationCount)
        val tvExpandIcon: TextView = itemView.findViewById(R.id.tvExpandIcon)
    }

    class StationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLocality: TextView = itemView.findViewById(R.id.tvLocality)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val btnFavorite: TextView = itemView.findViewById(R.id.btnFavorite)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.StationItem -> TYPE_STATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                HeaderVH(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station, parent, false)
                StationVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> {
                val vh = holder as HeaderVH
                vh.tvProvince.text = item.province
                vh.tvCount.text = "(${item.stationCount} estaciones)"
                vh.tvExpandIcon.text = if (item.isExpanded) "▼" else "▶"

                vh.itemView.setOnClickListener {
                    toggleProvince(position, item)
                }
            }
            is ListItem.StationItem -> {
                val vh = holder as StationVH
                val s = item.station
                vh.tvName.text = s.name ?: "-"
                vh.tvLocality.text = s.locality ?: "-"
                vh.tvAddress.text = s.address ?: "-"

                // Actualizar icono de favorito
                val isFavorite = favoritesManager.isFavorite(s.id)
                vh.btnFavorite.text = if (isFavorite) "⭐" else "☆"

                // Click en la estación (excluye el botón de favorito)
                vh.itemView.setOnClickListener {
                    onStationClick(s)
                }

                // Click en el botón de favorito
                vh.btnFavorite.setOnClickListener {
                    favoritesManager.toggleFavorite(s.id)
                    notifyItemChanged(position)
                    onFavoriteChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun toggleProvince(position: Int, header: ListItem.Header) {
        header.isExpanded = !header.isExpanded

        if (header.isExpanded) {
            // Expandir: insertar estaciones después del header
            val stationsToAdd = allStationsByProvince[header.province] ?: emptyList()
            items.addAll(position + 1, stationsToAdd)
            notifyItemChanged(position) // Actualizar icono
            notifyItemRangeInserted(position + 1, stationsToAdd.size)
        } else {
            // Colapsar: remover estaciones
            var count = 0
            var i = position + 1
            while (i < items.size && items[i] is ListItem.StationItem) {
                count++
                i++
            }
            repeat(count) {
                items.removeAt(position + 1)
            }
            notifyItemChanged(position) // Actualizar icono
            notifyItemRangeRemoved(position + 1, count)
        }
    }

    fun setStations(stations: List<Station>) {
        // Agrupar por provincia
        val grouped = stations.groupBy { it.province ?: "Sin provincia" }

        // Guardar todas las estaciones por provincia
        allStationsByProvince.clear()
        grouped.forEach { (province, stationList) ->
            allStationsByProvince[province] = stationList.map { ListItem.StationItem(it) }
        }

        // Crear lista solo con headers (todos colapsados por defecto)
        val newItems = mutableListOf<ListItem>()
        for ((province, stationList) in grouped.entries.sortedBy { it.key }) {
            newItems.add(ListItem.Header(
                province = province,
                isExpanded = false,
                stationCount = stationList.size
            ))
        }

        this.originalItems = newItems.toList()
        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        if (query.isBlank()) {
            // Restaurar headers colapsados
            items = originalItems.toMutableList()
            notifyDataSetChanged()
            return
        }

        val q = query.lowercase()
        val filtered = mutableListOf<ListItem>()

        // Filtrar estaciones por todas las categorías
        val allStations = allStationsByProvince.values.flatten()
        val filteredStations = allStations.filter { item ->
            val s = item.station
            val name = s.name?.lowercase() ?: ""
            val loc = s.locality?.lowercase() ?: ""
            val addr = s.address?.lowercase() ?: ""
            val prov = s.province?.lowercase() ?: ""
            val autoComm = s.autonomousCommunity?.lowercase() ?: ""
            val pricesText = s.prices.entries.joinToString(" ") {
                "${it.key.lowercase()} ${it.value.lowercase()}"
            }

            name.contains(q) || loc.contains(q) || addr.contains(q) ||
            prov.contains(q) || autoComm.contains(q) || pricesText.contains(q)
        }

        // Reagrupar con headers expandidos automáticamente al buscar
        val grouped = filteredStations.groupBy { it.station.province ?: "Sin provincia" }
        for ((province, stationList) in grouped.entries.sortedBy { it.key }) {
            filtered.add(ListItem.Header(
                province = province,
                isExpanded = true,
                stationCount = stationList.size
            ))
            filtered.addAll(stationList)
        }

        items = filtered
        notifyDataSetChanged()
    }
}
