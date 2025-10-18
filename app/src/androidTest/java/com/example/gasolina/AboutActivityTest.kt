package com.example.gasolina

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI para AboutActivity usando Espresso
 */
@RunWith(AndroidJUnit4::class)
class AboutActivityTest {

    @Test
    fun testAboutActivityLaunches() {
        ActivityScenario.launch(AboutActivity::class.java)

        // Verificar que los elementos principales están presentes
        onView(withId(R.id.imgAboutLogo)).check(matches(isDisplayed()))
        onView(withId(R.id.btnGitHub)).check(matches(isDisplayed()))
    }

    @Test
    fun testLogoIsVisible() {
        ActivityScenario.launch(AboutActivity::class.java)

        onView(withId(R.id.imgAboutLogo))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testGitHubButtonIsClickable() {
        ActivityScenario.launch(AboutActivity::class.java)

        onView(withId(R.id.btnGitHub))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText("🔗 Ver en GitHub")))
    }

    @Test
    fun testAppNameIsDisplayed() {
        ActivityScenario.launch(AboutActivity::class.java)

        onView(withText("Gasoliprecio"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testVersionIsDisplayed() {
        ActivityScenario.launch(AboutActivity::class.java)

        onView(withText("Versión 1.0"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDescriptionIsDisplayed() {
        ActivityScenario.launch(AboutActivity::class.java)

        onView(withText(containsString("Gasoliprecio es una aplicación")))
            .check(matches(isDisplayed()))
    }
}

