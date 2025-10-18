package com.example.gasolina

import java.io.Serializable

data class Station(
    val id: String,  // ID único de la estación
    val name: String?,
    val locality: String?,
    val address: String?,
    val province: String?,
    val autonomousCommunity: String?,
    val prices: Map<String, String> = emptyMap(),
    val latitude: Double? = null,  // Latitud de la estación
    val longitude: Double? = null  // Longitud de la estación
) : Serializable

// Para manejar headers de provincia con estado de expansión y estaciones en el RecyclerView
sealed class ListItem {
    data class Header(val province: String, var isExpanded: Boolean = false, val stationCount: Int = 0) : ListItem()
    data class StationItem(val station: Station) : ListItem()
}
