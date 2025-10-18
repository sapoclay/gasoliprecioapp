package com.example.gasolina

object StationsRepository {
    private var allStations: List<Station> = emptyList()

    fun setStations(stations: List<Station>) {
        allStations = stations
    }

    fun getAllStations(): List<Station> {
        return allStations
    }

    fun getFavoriteStations(favoritesManager: FavoritesManager): List<Station> {
        val favoriteIds = favoritesManager.getFavorites()
        return allStations.filter { favoriteIds.contains(it.id) }
    }
}

