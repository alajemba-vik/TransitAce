import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
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
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
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

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
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
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.alajemba.paristransitace.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.alajemba.paristransitace"
            packageVersion = "1.0.0"
        }
    }
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
    buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
}