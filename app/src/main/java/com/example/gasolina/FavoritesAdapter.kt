package com.example.gasolina

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private var items: MutableList<ListItem>,
    private val onStationClick: (Station) -> Unit,
    private val onDeleteClick: (Station) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allStationsByCommunity = mutableMapOf<String, List<ListItem.StationItem>>()

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
        val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
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
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station_favorite, parent, false)
                StationVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> {
                val vh = holder as HeaderVH
                vh.tvProvince.text = item.communityName
                vh.tvCount.text = "(${item.stationCount} favoritas)"
                vh.tvExpandIcon.text = if (item.isExpanded) "▼" else "▶"

                vh.itemView.setOnClickListener {
                    toggleCommunity(position, item)
                }
            }
            is ListItem.StationItem -> {
                val vh = holder as StationVH
                val s = item.station
                vh.tvName.text = s.name ?: "-"
                vh.tvLocality.text = s.locality ?: "-"
                vh.tvAddress.text = s.address ?: "-"

                // La estrella siempre está rellena en favoritos
                vh.btnFavorite.text = "⭐"

                // Click en la estación para ver detalles
                vh.itemView.setOnClickListener {
                    onStationClick(s)
                }

                // Click en la estrella para quitar de favoritos
                vh.btnFavorite.setOnClickListener {
                    onDeleteClick(s)
                }

                // Click en el botón de eliminar
                vh.btnDelete.setOnClickListener {
                    onDeleteClick(s)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun toggleCommunity(position: Int, header: ListItem.Header) {
        header.isExpanded = !header.isExpanded

        if (header.isExpanded) {
            // Expandir: insertar estaciones después del header
            val stationsToAdd = allStationsByCommunity[header.communityName] ?: emptyList()
            items.addAll(position + 1, stationsToAdd)
            notifyItemChanged(position)
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
            notifyItemChanged(position)
            notifyItemRangeRemoved(position + 1, count)
        }
    }

    fun setStations(stations: List<Station>) {
        // Agrupar por comunidad autónoma
        val grouped = stations.groupBy { it.autonomousCommunity ?: "Sin comunidad" }

        // Guardar todas las estaciones por comunidad autónoma
        allStationsByCommunity.clear()
        grouped.forEach { (community, stationList) ->
            allStationsByCommunity[community] = stationList.map { ListItem.StationItem(it) }
        }

        // Crear lista solo con headers (todos colapsados por defecto)
        val newItems = mutableListOf<ListItem>()
        for ((community, stationList) in grouped.entries.sortedBy { it.key }) {
            newItems.add(ListItem.Header(
                communityName = community,
                isExpanded = false,
                stationCount = stationList.size
            ))
        }

        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
