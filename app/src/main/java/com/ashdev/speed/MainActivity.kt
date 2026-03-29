package com.ashdev.speed

import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ashdev.speed.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val speedTester = SpeedTester()
    private var isTesting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.refreshButton.setOnClickListener {
            if (!isTesting) {
                startSpeedTest()
            } else {
                Toast.makeText(this, "Prueba en curso, por favor espera...", Toast.LENGTH_SHORT).show()
            }
        }

        startSpeedTest()
    }

    private fun startSpeedTest() {
        if (!isNetworkAvailable()) {
            showNoInternetError()
            return
        }

        isTesting = true
        binding.errorText.visibility = TextView.GONE
        binding.speedText.text = getString(R.string.speed_testing)
        binding.speedGauge.setSpeed(0.0)
        
        // Mostrar indicador de carga en el botón
        binding.refreshButton.isEnabled = false
        
        // Mostrar mensaje de prueba en progreso
        Toast.makeText(this, "Probando velocidad...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            // Primero probamos latencia
            val latency = withContext(Dispatchers.IO) {
                speedTester.testLatency()
            }
            
            // Luego probamos velocidad con 3 intentos para mayor precisión
            val speedMbps = withContext(Dispatchers.IO) {
                speedTester.testDownloadSpeedWithRetries(3)
            }
            
            withContext(Dispatchers.Main) {
                isTesting = false
                binding.refreshButton.isEnabled = true
                
                if (speedMbps > 0) {
                    binding.speedText.text = getString(R.string.speed_mbps, speedMbps)
                    binding.speedGauge.setSpeed(speedMbps)
                    
                    // Mostrar latencia opcionalmente
                    if (latency < 999) {
                        Toast.makeText(
                            this@MainActivity,
                            "Latencia: ${latency}ms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    showNoInternetError()
                    Toast.makeText(
                        this@MainActivity,
                        "Error al medir velocidad",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showNoInternetError() {
        binding.errorText.visibility = TextView.VISIBLE
        binding.speedText.text = getString(R.string.speed_mbps, 0.0)
        binding.speedGauge.setSpeed(0.0)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_functions -> {
                showChangelogDialog()
                true
            }
            R.id.menu_about -> {
                showAboutDialog()
                true
            }
            R.id.menu_exit -> {
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showChangelogDialog() {
        val changelogText = """
            Versión 1.1 - Mejoras importantes:
            • Múltiples servidores de prueba para mayor precisión
            • Pruebas con 3 intentos y eliminación de valores atípicos
            • Medición de latencia (ping)
            • Interfaz mejorada con indicadores de prueba
            • Mayor estabilidad en conexiones rápidas
            
            Versión 1.0:
            • Prueba básica de velocidad
            • Diseño con aguja analógica
            • Menú de información
            • Splash animado
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.changelog_title)
            .setMessage(changelogText)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_about)
            .setMessage(R.string.about_text)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}