package com.example.recorder

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastUpdateTime: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f
    var threshold: Float = 14.5f
    private var isFirst = true

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val currentTime = System.currentTimeMillis()
        if (isFirst) {
            lastX = event.values[0]
            lastY = event.values[1]
            lastZ = event.values[2]
            lastUpdateTime = currentTime
            isFirst = false
            return
        }

        val diffTime = currentTime - lastUpdateTime
        if (diffTime > 100) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            val speed = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()

            if (speed > threshold) {
                onShake()
                lastUpdateTime = currentTime + 800 // debouncing window
            } else {
                lastUpdateTime = currentTime
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
