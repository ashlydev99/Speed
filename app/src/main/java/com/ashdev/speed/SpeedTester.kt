package com.ashdev.speed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class SpeedTester {
    
    // Lista de servidores de prueba (CDN públicos y confiables)
    // Todos usan archivos de 10 MB para consistencia
    private data class TestServer(
        val name: String,
        val url: String,
        val sizeMB: Int = 10
    )
    
    private val testServers = listOf(
        TestServer("Cachefly", "https://cachefly.cachefly.net/10mb.test", 10),
        TestServer("Cloudflare", "https://speed.cloudflare.com/__down?bytes=10000000", 10),
        TestServer("OVH", "https://proof.ovh.net/files/10Mb.dat", 10),
        TestServer("ThinkBroadband", "https://speedtest.thinkbroadband.com/10MB.zip", 10)
    )
    
    private val testSizeBytes = 10 * 1024 * 1024L // 10 MB
    
    /**
     * Prueba la velocidad de descarga usando múltiples servidores
     * Retorna la velocidad promedio en Mbps
     */
    suspend fun testDownloadSpeed(): Double = withContext(Dispatchers.IO) {
        try {
            // Realiza pruebas con todos los servidores en paralelo
            val speeds = testServers.map { server ->
                async {
                    try {
                        testSingleServer(server.url)
                    } catch (e: Exception) {
                        // Si falla un servidor, lo ignoramos
                        null
                    }
                }
            }.awaitAll().filterNotNull()
            
            if (speeds.isEmpty()) {
                return@withContext 0.0
            }
            
            // Calculamos el promedio de todos los servidores exitosos
            val averageSpeed = speeds.average()
            
            // Eliminamos outliers (valores atípicos) para mayor precisión
            val filteredSpeeds = removeOutliers(speeds)
            val finalSpeed = if (filteredSpeeds.isNotEmpty()) {
                filteredSpeeds.average()
            } else {
                averageSpeed
            }
            
            return@withContext finalSpeed.coerceAtLeast(0.0)
            
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0.0
        }
    }
    
    /**
     * Prueba la velocidad de un servidor específico
     */
    private fun testSingleServer(urlString: String): Double {
        var connection: HttpURLConnection? = null
        try {
            val startTime = System.nanoTime()
            var totalBytes = 0L
            
            connection = URL(urlString).openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 5000
                readTimeout = 10000
                setRequestProperty("User-Agent", "Speed-Android/1.0")
                setRequestProperty("Accept-Encoding", "identity") // Deshabilitar compresión
            }
            
            connection.inputStream.use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    totalBytes += bytesRead
                }
            }
            
            val endTime = System.nanoTime()
            val durationSec = (endTime - startTime) / 1_000_000_000.0
            
            if (durationSec > 0) {
                // Convertir bytes/segundo a Mbps
                val bitsPerSecond = (totalBytes * 8) / durationSec
                return bitsPerSecond / 1_000_000.0
            }
            
            return 0.0
            
        } catch (e: Exception) {
            throw e
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Elimina outliers (valores atípicos) del conjunto de datos
     * Usa el método IQR (Rango Intercuartil)
     */
    private fun removeOutliers(speeds: List<Double>): List<Double> {
        if (speeds.size < 3) return speeds
        
        val sorted = speeds.sorted()
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val iqr = q3 - q1
        
        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr
        
        return sorted.filter { it in lowerBound..upperBound }
    }
    
    /**
     * Prueba de velocidad con múltiples intentos para mayor precisión
     * Realiza 3 pruebas y retorna la mediana
     */
    suspend fun testDownloadSpeedWithRetries(retries: Int = 3): Double = withContext(Dispatchers.IO) {
        val speeds = mutableListOf<Double>()
        
        for (i in 0 until retries) {
            try {
                val speed = testDownloadSpeed()
                if (speed > 0) {
                    speeds.add(speed)
                }
                // Pequeña pausa entre pruebas
                TimeUnit.MILLISECONDS.sleep(500)
            } catch (e: Exception) {
                // Ignoramos fallos individuales
            }
        }
        
        if (speeds.isEmpty()) {
            return@withContext 0.0
        }
        
        // Retornamos la mediana para evitar valores extremos
        speeds.sorted()[speeds.size / 2]
    }
    
    /**
     * Prueba de latencia (ping) antes de la prueba de velocidad
     */
    suspend fun testLatency(): Long = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()
        val pingServers = listOf(
            "https://www.google.com",
            "https://www.cloudflare.com",
            "https://www.github.com"
        )
        
        pingServers.forEach { server ->
            try {
                val startTime = System.nanoTime()
                val connection = URL(server).openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.connect()
                connection.disconnect()
                val endTime = System.nanoTime()
                val latencyMs = (endTime - startTime) / 1_000_000
                latencies.add(latencyMs)
            } catch (e: Exception) {
                // Ignoramos servidores que no responden
            }
        }
        
        if (latencies.isEmpty()) {
            return@withContext 999
        }
        
        return@withContext latencies.average().toLong()
    }
}