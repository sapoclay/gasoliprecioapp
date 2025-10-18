package com.example.gasolina

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class GasolinaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Aplicar el tema guardado al iniciar la aplicación
        val sharedPreferences = getSharedPreferences("${packageName}_preferences", MODE_PRIVATE)
        val themePref = sharedPreferences.getString("pref_theme", "system") ?: "system"

        val mode = when (themePref) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
