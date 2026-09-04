plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.kutubuddin.mivra"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // CameraX core and camera2 implementation
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)

    // CameraX Lifecycle library
    implementation(libs.androidx.camera.lifecycle)

    // CameraX View class
    implementation(libs.androidx.camera.view)

    // Optional: CameraX Extensions (uncomment if added to .toml)
     implementation(libs.androidx.camera.extensions)
}