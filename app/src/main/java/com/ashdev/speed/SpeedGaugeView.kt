package com.ashdev.speed

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class SpeedGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentSpeed = 0.0 // Mbps
    private var maxSpeed = 100.0   // Escala máxima 100 Mbps
    
    // Colores estilo fast.com
    private val backgroundColor = Color.parseColor("#0A1929")
    private val arcColor = Color.parseColor("#00BFFF")
    private val needleColor = Color.parseColor("#00BFFF")
    private val centerColor = Color.parseColor("#00BFFF")
    private val textColor = Color.parseColor("#00BFFF")
    
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }
    
    private val arcBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C3E50")
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val arcProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = arcColor
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = needleColor
        style = Paint.Style.FILL
    }
    
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = centerColor
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    
    private val startAngle = 140f
    private val sweepAngle = 260f
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height * 0.75f
        
        val radius = minOf(width, height) * 0.35f
        
        // Fondo del arco (gris)
        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            startAngle, sweepAngle, false, arcBackgroundPaint
        )
        
        // Arco de progreso (azul eléctrico)
        val percent = (currentSpeed / maxSpeed).coerceIn(0.0, 1.0)
        val currentSweep = percent * sweepAngle
        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            startAngle, currentSweep.toFloat(), false, arcProgressPaint
        )
        
        // Aguja
        val needleAngle = startAngle + (percent * sweepAngle).toFloat()
        val needleLength = radius * 0.85f
        val needleTipX = centerX + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat()
        val needleTipY = centerY + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
        
        // Dibujar aguja con punta triangular
        val needleBaseRadius = radius * 0.12f
        val arrowSize = radius * 0.08f
        
        val path = Path()
        path.moveTo(needleTipX, needleTipY)
        
        val angleRad = Math.toRadians(needleAngle.toDouble())
        val perpAngle1 = angleRad + Math.PI / 2
        val perpAngle2 = angleRad - Math.PI / 2
        
        val arrowX1 = needleTipX - arrowSize * cos(perpAngle1).toFloat()
        val arrowY1 = needleTipY - arrowSize * sin(perpAngle1).toFloat()
        val arrowX2 = needleTipX - arrowSize * cos(perpAngle2).toFloat()
        val arrowY2 = needleTipY - arrowSize * sin(perpAngle2).toFloat()
        
        path.lineTo(arrowX1, arrowY1)
        path.lineTo(arrowX2, arrowY2)
        path.close()
        
        canvas.drawPath(path, needlePaint)
        
        // Línea de la aguja
        canvas.drawLine(centerX, centerY, needleTipX, needleTipY, needlePaint)
        
        // Círculo central
        canvas.drawCircle(centerX, centerY, needleBaseRadius, centerPaint)
        
        // Marcar escala (0, 25, 50, 75, 100)
        for (i in 0..4) {
            val value = i * 25
            val angle = startAngle + (value / maxSpeed) * sweepAngle
            val angleRadScale = Math.toRadians(angle.toDouble())
            val markRadius = radius + 15f
            val markX = centerX + markRadius * cos(angleRadScale).toFloat()
            val markY = centerY + markRadius * sin(angleRadScale).toFloat()
            
            canvas.drawText(value.toString(), markX, markY, smallTextPaint)
        }
        
        // Texto "Mbps" debajo de la aguja
        canvas.drawText("Mbps", centerX, centerY + radius + 40, smallTextPaint)
    }
    
    fun setSpeed(speedMbps: Double) {
        currentSpeed = speedMbps.coerceIn(0.0, maxSpeed)
        invalidate()
    }
    
    fun setMaxSpeed(max: Double) {
        maxSpeed = max
        invalidate()
    }
}