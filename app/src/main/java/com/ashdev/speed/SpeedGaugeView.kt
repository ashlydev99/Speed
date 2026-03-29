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

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A1929")
        style = Paint.Style.FILL
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BFFF")
        style = Paint.Style.STROKE
        strokeWidth = 15f
        strokeCap = Paint.Cap.ROUND
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BFFF")
        style = Paint.Style.FILL
    }

    private val centerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BFFF")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BFFF")
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val startAngle = 140f
    private val sweepAngle = 260f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height * 0.8f  // desplazar hacia abajo para dejar espacio al texto

        val radius = minOf(width, height) * 0.4f

        // Fondo del arco
        arcPaint.color = Color.GRAY
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
            startAngle, sweepAngle, false, arcPaint)

        // Arco de velocidad
        val percent = (currentSpeed / maxSpeed).coerceIn(0.0, 1.0)
        val currentSweep = percent * sweepAngle
        arcPaint.color = Color.parseColor("#00BFFF")
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
            startAngle, currentSweep.toFloat(), false, arcPaint)

        // Aguja
        val needleAngle = startAngle + (percent * sweepAngle).toFloat()
        val needleLength = radius * 0.8f
        val needleTipX = centerX + needleLength * Math.cos(Math.toRadians(needleAngle.toDouble())).toFloat()
        val needleTipY = centerY + needleLength * Math.sin(Math.toRadians(needleAngle.toDouble())).toFloat()
        canvas.drawLine(centerX, centerY, needleTipX, needleTipY, needlePaint)

        // Círculo central
        canvas.drawCircle(centerX, centerY, radius * 0.12f, centerCirclePaint)

        // Texto de velocidad (opcional, ya lo muestra el TextView)
        // Puedes añadir un texto dentro del círculo si quieres
        // canvas.drawText(String.format("%.0f", currentSpeed), centerX, centerY + 10, textPaint)
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