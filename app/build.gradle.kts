plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    alias(libs.plugins.devtoolsKsp)
    alias(libs.plugins.daggerHilt)
}

android {
    namespace = "com.edgetech.bbscout"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edgetech.bbscout"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        val properties = project.properties
        manifestPlaceholders += mapOf(
            "MAPS_API_KEY" to (properties.getOrDefault("MAPS_API_KEY", "") as String)
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
//    composeOptions {
//       // kotlinCompilerExtensionVersion = "1.5.2"
//    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.compose)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.google.maps.compose)
    implementation(libs.google.play.services.maps)

    implementation(libs.kotlinx.serialization.json)

    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit and Location Services
    implementation(libs.google.mlkit.`object`.detection)
    implementation(libs.mlkit.image.labeling)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.mlkit.entity.extraction)
    implementation("com.google.mlkit:object-detection:17.0.0")
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    //For the History DataStore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compile)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}