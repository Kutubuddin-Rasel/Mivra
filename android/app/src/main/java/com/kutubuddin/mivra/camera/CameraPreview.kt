package com.kutubuddin.mivra.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(
        context,
        lifeCycleOwner,
        preview
    ) {
        try {
            val cameraProvider = ProcessCameraProvider.awaitInstance(context)

            // While this lifecycle allows camera operation, use the default rear-facing camera to satisfy this Preview use case.
            cameraProvider.bindToLifecycle(
                lifeCycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview
            )
        } catch (exception: Exception) {
            cameraError = exception.message ?: "Unable to start camera"
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