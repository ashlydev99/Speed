package com.ashdev.speed

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
        supportActionBar?.title = "Speed Test"

        binding.refreshButton.setOnClickListener {
            if (!isTesting) {
                startSpeedTest()
            } else {
                Toast.makeText(this, "Prueba en curso, espera...", Toast.LENGTH_SHORT).show()
            }
        }

        startSpeedTest()
    }

    private fun startSpeedTest() {
        if (!isNetworkAvailable()) {
            showNoInternetError()
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_LONG).show()
            return
        }

        isTesting = true
        binding.errorText.visibility = TextView.GONE
        binding.speedText.text = "0.00"
        binding.mbpsText.visibility = TextView.VISIBLE
        binding.refreshButton.isEnabled = false
        
        Toast.makeText(this, "Midiendo velocidad...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val speedMbps = withContext(Dispatchers.IO) {
                speedTester.testDownloadSpeed()
            }
            
            withContext(Dispatchers.Main) {
                isTesting = false
                binding.refreshButton.isEnabled = true
                
                if (speedMbps > 0) {
                    val speedText = String.format("%.2f", speedMbps)
                    binding.speedText.text = speedText
                    
                    Toast.makeText(
                        this@MainActivity,
                        "Velocidad: ${String.format("%.2f", speedMbps)} Mbps",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    showNoInternetError()
                    Toast.makeText(
                        this@MainActivity,
                        "Error: No se pudo medir la velocidad.\nVerifica tu conexión.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showNoInternetError() {
        binding.errorText.visibility = TextView.VISIBLE
        binding.speedText.text = "0.00"
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
        AlertDialog.Builder(this)
            .setTitle("Nuevas funciones")
            .setMessage("Versión 1.0\n\n• Prueba de velocidad de descarga\n• Diseño limpio y minimalista\n• Múltiples servidores de respaldo")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Acerca de")
            .setMessage("Creado por AshDev con amor ❤️ para la comunidad\n\nContacto: ashdev@gmail.com")
            .setPositiveButton("OK", null)
            .show()
    }
}