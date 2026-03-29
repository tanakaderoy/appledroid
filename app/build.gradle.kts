plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tanakaderoy.appledroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tanakaderoy.appledroid"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "appledroid-${variant.buildType.name}.apk"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.kotlin)
}
