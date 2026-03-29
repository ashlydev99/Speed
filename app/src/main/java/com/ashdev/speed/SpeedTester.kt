package com.ashdev.speed

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class SpeedTester {
    
    private val TAG = "SpeedTester"
    // Usamos el servidor de Cloudflare con archivo de 10MB
    private val testUrl = "https://speed.cloudflare.com/__down?bytes=10000000"
    private val testSizeBytes = 10 * 1024 * 1024L // 10 MB
    
    suspend fun testDownloadSpeed(): Double = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            Log.d(TAG, "Iniciando prueba de velocidad con Cloudflare")
            
            val startTime = System.nanoTime()
            var totalBytes = 0L
            
            connection = URL(testUrl).openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 10000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Speed-Android/1.0")
                setRequestProperty("Accept-Encoding", "identity") // Deshabilitar compresión
            }
            
            connection.connect()
            Log.d(TAG, "Conectado a Cloudflare")
            
            connection.inputStream.use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    totalBytes += bytesRead
                }
            }
            
            val endTime = System.nanoTime()
            val durationSec = (endTime - startTime) / 1_000_000_000.0
            
            Log.d(TAG, "Descargado: ${totalBytes / 1024 / 1024} MB en ${String.format("%.2f", durationSec)} segundos")
            
            if (durationSec > 0 && totalBytes > 0) {
                val bitsPerSecond = (totalBytes * 8) / durationSec
                val speedMbps = bitsPerSecond / 1_000_000.0
                Log.d(TAG, "Velocidad: ${String.format("%.2f", speedMbps)} Mbps")
                return@withContext speedMbps
            }
            
            return@withContext 0.0
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en prueba de velocidad", e)
            return@withContext 0.0
        } finally {
            connection?.disconnect()
        }
    }
    
    // Prueba con 3 intentos y devuelve la mediana
    suspend fun testDownloadSpeedWithRetries(retries: Int = 3): Double = withContext(Dispatchers.IO) {
        val speeds = mutableListOf<Double>()
        
        for (i in 0 until retries) {
            try {
                Log.d(TAG, "Intento ${i + 1}/$retries")
                val speed = testDownloadSpeed()
                if (speed > 0) {
                    speeds.add(speed)
                    Log.d(TAG, "Intento ${i + 1}: ${String.format("%.2f", speed)} Mbps")
                }
                TimeUnit.MILLISECONDS.sleep(500)
            } catch (e: Exception) {
                Log.e(TAG, "Error en intento ${i + 1}", e)
            }
        }
        
        if (speeds.isEmpty()) {
            Log.e(TAG, "Todos los intentos fallaron")
            return@withContext 0.0
        }
        
        // Devolver la mediana para mayor precisión
        val sortedSpeeds = speeds.sorted()
        val medianSpeed = sortedSpeeds[sortedSpeeds.size / 2]
        Log.d(TAG, "Velocidad final (mediana): ${String.format("%.2f", medianSpeed)} Mbps")
        return@withContext medianSpeed
    }
}