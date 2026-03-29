package com.ashdev.speed

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SpeedTester {
    
    private val TAG = "SpeedTester"
    
    suspend fun testDownloadSpeed(): Double = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            // Múltiples URLs de prueba (si una falla, prueba otra)
            val testUrls = listOf(
                "https://proof.ovh.net/files/1Mb.dat",
                "http://speedtest.tele2.net/1MB.zip"
            )
            
            var speedMbps = 0.0
            
            for (url in testUrls) {
                try {
                    Log.d(TAG, "Probando: $url")
                    
                    val startTime = System.currentTimeMillis()
                    var totalBytes = 0L
                    
                    connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000
                    connection.connect()
                    
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            totalBytes += bytesRead
                        }
                    }
                    
                    val endTime = System.currentTimeMillis()
                    val durationSec = (endTime - startTime) / 1000.0
                    
                    if (durationSec > 0 && totalBytes > 0) {
                        val bitsPerSecond = (totalBytes * 8) / durationSec
                        speedMbps = bitsPerSecond / 1_000_000.0
                        Log.d(TAG, "Velocidad: ${String.format("%.2f", speedMbps)} Mbps")
                        return@withContext speedMbps
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error con URL: $url", e)
                    continue
                } finally {
                    connection?.disconnect()
                }
            }
            
            return@withContext speedMbps
            
        } catch (e: Exception) {
            Log.e(TAG, "Error general", e)
            return@withContext 0.0
        }
    }
}