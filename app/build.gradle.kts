plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.tokenroomdatabase"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tokenroomdatabase"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // ✅ YAHAN HONA CHAHIYE
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)


    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )



    implementation(
        "androidx.core:core-splashscreen:1.2.0"
    )

    implementation("androidx.lifecycle:lifecycle-process:2.9.2")


    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    ksp(libs.room.compiler)
}