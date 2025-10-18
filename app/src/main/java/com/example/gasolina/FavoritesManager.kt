package com.example.gasolina

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "gasolina_favorites",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_FAVORITES = "favorite_stations"
    }

    // Obtener todos los IDs de estaciones favoritas
    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    // Verificar si una estación es favorita
    fun isFavorite(stationId: String): Boolean {
        return getFavorites().contains(stationId)
    }

    // Agregar una estación a favoritos
    fun addFavorite(stationId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(stationId)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    // Eliminar una estación de favoritos
    fun removeFavorite(stationId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(stationId)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    // Alternar el estado de favorito
    fun toggleFavorite(stationId: String): Boolean {
        return if (isFavorite(stationId)) {
            removeFavorite(stationId)
            false
        } else {
            addFavorite(stationId)
            true
        }
    }
}

