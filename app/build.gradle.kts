import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val releaseKeystorePropertiesFile = rootProject.file("signing/keystore.properties")
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystorePropertiesFile.exists()) {
        releaseKeystorePropertiesFile.inputStream().use(::load)
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.moonsolstudios.kavvoro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moonsolstudios.kavvoro"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        manifestPlaceholders["admobApplicationId"] = "ca-app-pub-5095011886038660~7427660921"
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-5095011886038660/8267923702\"")
        buildConfigField("String", "ADMOB_REWARDED_CONTINUE_ID", "\"ca-app-pub-5095011886038660/1976689979\"")
    }

    signingConfigs {
        if (releaseKeystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(releaseKeystoreProperties.getProperty("storeFile"))
                storePassword = releaseKeystoreProperties.getProperty("storePassword")
                keyAlias = releaseKeystoreProperties.getProperty("keyAlias")
                keyPassword = releaseKeystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            manifestPlaceholders["admobApplicationId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_REWARDED_CONTINUE_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("boolean", "FORCE_UNLOCK_ALL_BRAINBALLS", "true")
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            manifestPlaceholders["admobApplicationId"] = "ca-app-pub-5095011886038660~7427660921"
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-5095011886038660/8267923702\"")
            buildConfigField("String", "ADMOB_REWARDED_CONTINUE_ID", "\"ca-app-pub-5095011886038660/1976689979\"")
            buildConfigField("boolean", "FORCE_UNLOCK_ALL_BRAINBALLS", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.google.android.gms:play-services-games-v2:21.0.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation("com.android.billingclient:billing:9.1.0")
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")
    testImplementation("junit:junit:4.13.2")
}
