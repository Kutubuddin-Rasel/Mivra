package com.kutubuddin.mivra.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class DiagnosticFrameAnalyzer : ImageAnalysis.Analyzer {
    private companion object{
        const val  TAG = "MivraFrameAnalyzer"
    }
    private var frameCount = 0
    private var windowStartNanos = System.nanoTime()

    override fun analyze(image: ImageProxy) {
        try {
            frameCount++

            val now = System.nanoTime()
            val elapsedSeconds = (now - windowStartNanos) / 1_000_000_000.0

            if(elapsedSeconds >= 1.0){
                val analysisFps = frameCount/elapsedSeconds

                Log.d(TAG, """
                    analysisFps = $analysisFps
                    size = ${image.width} * ${image.height}
                    rotation = ${image.imageInfo.rotationDegrees}
                    format = ${image.format}
                    timeStamp = ${image.imageInfo.timestamp}
                """.trimIndent())

                frameCount = 0
                windowStartNanos = now
            }
        } finally {
            image.close()
        }
    }
}