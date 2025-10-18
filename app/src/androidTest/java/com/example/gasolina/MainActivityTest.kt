package com.example.gasolina

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI para MainActivity usando Espresso
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Before
    fun setup() {
        // Cargar estaciones de prueba en el repositorio
        val testStations = listOf(
            Station(
                id = "test1",
                name = "Repsol Test",
                locality = "Madrid",
                address = "Calle Test 1",
                province = "Madrid",
                autonomousCommunity = "Madrid",
                prices = mapOf("Gasolina 95 E5" to "1.50 €/litro")
            ),
            Station(
                id = "test2",
                name = "Cepsa Test",
                locality = "Barcelona",
                address = "Calle Test 2",
                province = "Barcelona",
                autonomousCommunity = "Cataluña",
                prices = mapOf("Gasóleo A" to "1.40 €/litro")
            )
        )
        StationsRepository.setStations(testStations)
    }

    @Test
    fun testMainActivityLaunches() {
        ActivityScenario.launch(MainActivity::class.java)

        // Verificar que los elementos principales están presentes
        onView(withId(R.id.searchView)).check(matches(isDisplayed()))
        onView(withId(R.id.btnFavorites)).check(matches(isDisplayed()))
        onView(withId(R.id.btnMap)).check(matches(isDisplayed()))
        onView(withId(R.id.btnAbout)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchViewIsVisible() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.searchView))
            .check(matches(isDisplayed()))
            .check(matches(withHint("Buscar gasolinera")))
    }

    @Test
    fun testFavoritesButtonIsClickable() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.btnFavorites))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText("⭐ Favoritos")))
    }

    @Test
    fun testMapButtonIsClickable() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.btnMap))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText("🗺️ Mapa")))
    }

    @Test
    fun testAboutButtonIsClickable() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.btnAbout))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText("ℹ️ About")))
    }

    @Test
    fun testSearchFunctionality() {
        ActivityScenario.launch(MainActivity::class.java)

        // Esperar a que se carguen los datos
        Thread.sleep(1000)

        // Realizar una búsqueda
        onView(withId(R.id.searchView))
            .perform(click())
            .perform(typeText("Madrid"))

        // Verificar que el RecyclerView sigue visible
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testRecyclerViewIsScrollable() {
        ActivityScenario.launch(MainActivity::class.java)

        // Esperar a que se carguen los datos
        Thread.sleep(1000)

        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
            .perform(swipeUp())
    }
}

