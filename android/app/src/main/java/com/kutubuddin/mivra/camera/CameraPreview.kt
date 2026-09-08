package com.kutubuddin.mivra.camera

import android.view.OrientationEventListener
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import java.util.concurrent.Executors

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var surfaceRequest by remember {
        mutableStateOf<SurfaceRequest?>(null)
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    val preview = remember {
        Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider { request ->
                    surfaceRequest = request
                }
            }
    }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    val analysisExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val diagnosticAnalyzer = remember {
        DiagnosticFrameAnalyzer()
    }

    DisposableEffect(
        imageAnalysis,
        analysisExecutor,
        diagnosticAnalyzer
    ) {
        imageAnalysis.setAnalyzer(analysisExecutor,diagnosticAnalyzer)
        onDispose {
            imageAnalysis.clearAnalyzer()
            analysisExecutor.shutdown()
        }
    }

    DisposableEffect(
        context,
        imageAnalysis
    ) {
        val orientationListener = object : OrientationEventListener(context){
            override fun onOrientationChanged(orientation: Int) {
                if(orientation == ORIENTATION_UNKNOWN){
                    return
                }

                imageAnalysis.targetRotation= UseCase.snapToSurfaceRotation(orientation)
            }
        }

        if(orientationListener.canDetectOrientation()){
            orientationListener.enable()
        }

        onDispose {
            orientationListener.disable()
        }
    }

    LaunchedEffect(
        context,
        lifecycleOwner,
        preview,
        imageAnalysis
    ) {
        cameraError = null

        val cameraProvider = try {
            ProcessCameraProvider.awaitInstance(context)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            cameraError = exception.message ?: "Unable to start camera"

            return@LaunchedEffect
        }

        try {
            // While this lifecycle allows camera operation, use the default rear-facing camera to satisfy this Preview use case.
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
            awaitCancellation()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            cameraError = exception.message ?: "Unable to start camera"
        } finally {
            cameraProvider.unbind(preview,imageAnalysis)
        }
    }

    val currentSurfaceRequest = surfaceRequest

    when {
        cameraError != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Camera error: $cameraError")
            }
        }

        currentSurfaceRequest != null -> {
            CameraXViewfinder(
                surfaceRequest = currentSurfaceRequest,
                modifier = modifier.fillMaxSize()
            )
        }

        else -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Starting camera...")
            }
        }
    }
}