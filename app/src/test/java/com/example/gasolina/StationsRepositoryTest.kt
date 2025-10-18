package com.example.gasolina

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para StationsRepository
 */
class StationsRepositoryTest {

    private val testStations = listOf(
        Station(
            id = "1",
            name = "Repsol",
            locality = "Madrid",
            address = "Calle 1",
            province = "Madrid",
            autonomousCommunity = "Madrid",
            prices = mapOf("Gasolina 95 E5" to "1.50 €/litro")
        ),
        Station(
            id = "2",
            name = "Cepsa",
            locality = "Barcelona",
            address = "Calle 2",
            province = "Barcelona",
            autonomousCommunity = "Cataluña",
            prices = mapOf("Gasóleo A" to "1.40 €/litro")
        ),
        Station(
            id = "3",
            name = "BP",
            locality = "Valencia",
            address = "Calle 3",
            province = "Valencia",
            autonomousCommunity = "Valencia",
            prices = emptyMap()
        )
    )

    @Before
    fun setup() {
        // Limpiar el repositorio antes de cada test
        StationsRepository.setStations(emptyList())
    }

    @Test
    fun `should store stations correctly`() {
        StationsRepository.setStations(testStations)

        val retrieved = StationsRepository.getAllStations()
        assertThat(retrieved).hasSize(3)
        assertThat(retrieved).isEqualTo(testStations)
    }

    @Test
    fun `should return empty list when no stations set`() {
        val stations = StationsRepository.getAllStations()
        assertThat(stations).isEmpty()
    }

    @Test
    fun `should replace stations when set multiple times`() {
        StationsRepository.setStations(testStations)
        assertThat(StationsRepository.getAllStations()).hasSize(3)

        val newStations = listOf(testStations[0])
        StationsRepository.setStations(newStations)

        assertThat(StationsRepository.getAllStations()).hasSize(1)
        assertThat(StationsRepository.getAllStations()[0].id).isEqualTo("1")
    }

    @Test
    fun `should get favorite stations correctly`() {
        StationsRepository.setStations(testStations)

        // Mock del FavoritesManager (simplificado para el test)
        // En un test real, usarías Mockito para esto
        val mockFavoritesManager = object : FavoritesManager(null) {
            override fun getFavorites(): Set<String> {
                return setOf("1", "3")
            }
        }

        val favorites = StationsRepository.getFavoriteStations(mockFavoritesManager)

        assertThat(favorites).hasSize(2)
        assertThat(favorites.map { it.id }).containsExactly("1", "3")
    }
}

