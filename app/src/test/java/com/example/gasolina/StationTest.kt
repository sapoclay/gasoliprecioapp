package com.example.gasolina

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para la clase Station
 */
class StationTest {

    private lateinit var station: Station

    @Before
    fun setup() {
        station = Station(
            id = "12345",
            name = "Gasolinera Test",
            locality = "Madrid",
            address = "Calle Test 123",
            province = "Madrid",
            autonomousCommunity = "Comunidad de Madrid",
            prices = mapOf(
                "Gasolina 95 E5" to "1.50 €/litro",
                "Gasóleo A" to "1.40 €/litro"
            )
        )
    }

    @Test
    fun `station should have correct properties`() {
        assertThat(station.id).isEqualTo("12345")
        assertThat(station.name).isEqualTo("Gasolinera Test")
        assertThat(station.locality).isEqualTo("Madrid")
        assertThat(station.address).isEqualTo("Calle Test 123")
        assertThat(station.province).isEqualTo("Madrid")
        assertThat(station.autonomousCommunity).isEqualTo("Comunidad de Madrid")
    }

    @Test
    fun `station should have prices map`() {
        assertThat(station.prices).isNotEmpty()
        assertThat(station.prices).hasSize(2)
        assertThat(station.prices).containsKey("Gasolina 95 E5")
        assertThat(station.prices).containsKey("Gasóleo A")
    }

    @Test
    fun `station with empty prices should work`() {
        val emptyStation = Station(
            id = "1",
            name = "Test",
            locality = "Test",
            address = "Test",
            province = "Test",
            autonomousCommunity = "Test",
            prices = emptyMap()
        )

        assertThat(emptyStation.prices).isEmpty()
    }

    @Test
    fun `station should handle null values`() {
        val nullStation = Station(
            id = "1",
            name = null,
            locality = null,
            address = null,
            province = "Madrid",
            autonomousCommunity = null,
            prices = emptyMap()
        )

        assertThat(nullStation.name).isNull()
        assertThat(nullStation.locality).isNull()
        assertThat(nullStation.address).isNull()
        assertThat(nullStation.province).isNotNull()
    }
}

