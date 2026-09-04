plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kutubuddin.mivra"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.kutubuddin.mivra"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    /*
     * CameraX Pipeline Dependencies:
     * CameraX is designed as a modular suite where each artifact handles a distinct
     * responsibility in the camera capture and analysis pipeline:
     *
     * 1. camera-core:
     *    The foundational domain layer. Defines public abstractions and use-case APIs
     *    (Preview, ImageAnalysis, ImageCapture, CameraSelector, ResolutionSelector).
     *
     * 2. camera-camera2:
     *    The platform driver/engine layer. Bridges CameraX abstractions to the underlying
     *    Android Camera2 hardware subsystem and handles OEM-specific compatibility quirks.
     *    (Required at runtime; without this, CameraX cannot initialize any camera sensor).
     *
     * 3. camera-lifecycle:
     *    The lifecycle-binding layer. Provides `ProcessCameraProvider` to bind use cases
     *    directly to an Android `LifecycleOwner` (automatically starting/stopping the camera
     *    with the Activity/Compose lifecycle to prevent battery drain and leaks).
     *
     * 4. camera-compose:
     *    The UI presentation layer. Provides native Jetpack Compose viewfinder components
     *    to render the camera preview directly within Compose hierarchies without needing
     *    legacy XML `AndroidView` wrappers around `PreviewView`.
     */
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.compose)
}