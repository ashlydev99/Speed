package com.ashdev.speed

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SpeedGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentSpeed = 0.0 // Mbps
    private var maxSpeed = 100.0   // Escala máxima 100 Mbps
    
    // Pinturas
    private val arcBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        style = Paint.Style.STROKE
        strokeWidth = 25f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val arcProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BFFF")
        style = Paint.Style.STROKE
        strokeWidth = 25f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4444")
        style = Paint.Style.FILL
    }
    
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BFFF")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    
    private val startAngle = 135f
    private val sweepAngle = 270f
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2 + 50
        
        val radius = minOf(width, height) * 0.35f
        
        // 1. Dibujar fondo del arco (gris)
        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            startAngle, sweepAngle, false, arcBgPaint
        )
        
        // 2. Dibujar arco de progreso (azul)
        val percent = (currentSpeed / maxSpeed).coerceIn(0.0, 1.0)
        val currentSweep = percent * sweepAngle
        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            startAngle, currentSweep.toFloat(), false, arcProgressPaint
        )
        
        // 3. Dibujar aguja
        val needleAngle = startAngle + (percent * sweepAngle).toFloat()
        val needleAngleRad = Math.toRadians(needleAngle.toDouble())
        val needleLength = radius * 0.85f
        val needleTipX = centerX + (needleLength * cos(needleAngleRad)).toFloat()
        val needleTipY = centerY + (needleLength * sin(needleAngleRad)).toFloat()
        
        // Línea de la aguja
        val needleLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF4444")
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawLine(centerX, centerY, needleTipX, needleTipY, needleLinePaint)
        
        // Círculo en el centro
        canvas.drawCircle(centerX, centerY, 15f, centerPaint)
        canvas.drawCircle(centerX, centerY, 12f, Paint().apply {
            color = Color.parseColor("#00BFFF")
            style = Paint.Style.FILL
        })
        
        // 4. Dibujar marcas de velocidad
        for (i in 0..4) {
            val speedValue = i * 25
            val angle = startAngle + (speedValue.toDouble() / maxSpeed) * sweepAngle
            val angleRad = Math.toRadians(angle)
            val markRadius = radius + 15f
            val markX = centerX + (markRadius * cos(angleRad)).toFloat()
            val markY = centerY + (markRadius * sin(angleRad)).toFloat()
            
            canvas.drawText(speedValue.toString(), markX, markY, textPaint)
        }
        
        // 5. Texto "Mbps" abajo
        val mbpsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Mbps", centerX, centerY + radius + 30, mbpsPaint)
    }
    
    fun setSpeed(speedMbps: Double) {
        currentSpeed = speedMbps.coerceIn(0.0, maxSpeed)
        invalidate() // Forzar redibujo
    }
    
    fun setMaxSpeed(max: Double) {
        maxSpeed = max
        invalidate()
    }
}