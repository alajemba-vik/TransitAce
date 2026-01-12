import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    // For reading information from local.properties
    id("com.github.gmazzo.buildconfig") version "6.0.6"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        all {

            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Have Ktor use the OkHttp engine for Android
            implementation(libs.ktor.client.okhttp)

            // Have SQLDelight support Android
            implementation(libs.android.driver)

            // Splash screen
            implementation(libs.androidx.splashscreen)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // Access material icons
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Enable Ktor client functionality in shared code
            implementation(libs.ktor.client.core)
            // For negotiating media types between the client and server
            implementation(libs.ktor.client.content.negotiation)
            // Serialize and deserialize JSON
            implementation(libs.ktor.serialization.kotlinx.json)
            // Use coroutines in Android code
            implementation(libs.kotlinx.coroutines.core)
            // Koin for DI
            implementation(libs.koin.core)
            // SQLDelight
            implementation(libs.runtime)
            implementation(libs.coroutines.extensions)
            // Koin for Compose ViewModels (provides androidContext extensions)
            implementation(libs.koin.compose.viewmodel)
            // Koog for AI integration
            implementation(libs.koog.agents)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        iosMain.dependencies {
            // Have Ktor use the Darwin engine for iOS
            implementation(libs.ktor.client.darwin)
            // Have SQLDelight support IOS

            implementation(libs.native.driver)
        }
    }
}

android {
    namespace = "com.alajemba.paristransitace"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.alajemba.paristransitace"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,io.netty.versions.properties,DEPENDENCIES}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}


sqldelight {
    databases {
        create("ParisTransitDatabase") {
            packageName = "com.alajemba.paristransitace.db"
        }
    }
}

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
localProperties.load(localFile.inputStream())

buildConfig {
    packageName("com.alajemba.paristransitace")
    val geminiKey = localProperties["GEMINI_API_KEY"]?.toString() ?: ""
    val mistralKey = localProperties["MISTRALAI_API_KEY"]?.toString() ?: ""
    val openAIKey = localProperties["OPENAI_API_KEY"]?.toString() ?: ""
    buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
    buildConfigField("String", "MISTRALAI_API_KEY", "\"$mistralKey\"")
    buildConfigField("String", "OPENAI_API_KEY", "\"$openAIKey\"")
}