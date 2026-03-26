plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.endlessrunner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.endlessrunner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Настройки для игры
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Конфигурация типов сборки
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Отключаем логирование в release сборке
            buildConfigField("Boolean", "DEBUG_MODE", "false")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            buildConfigField("Boolean", "DEBUG_MODE", "true")
        }
    }

    // Настройки компиляции Kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Включаем оптимизации для coroutines
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }

    // Включаем BuildConfig
    buildFeatures {
        buildConfig = true
    }

    // Упаковка ресурсов
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Kotlin Coroutines - для асинхронных операций
    implementation(libs.kotlinx.coroutines.android)

    // Kotlinx Serialization - для JSON конфигурации
    implementation(libs.kotlinx.serialization.json)

    // Koin - Dependency Injection
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // DataStore Preferences - для хранения настроек и прогресса игрока
    implementation(libs.androidx.datastore.preferences)

    // ============================================================================
    // Jetpack Compose - UI фреймворк
    // ============================================================================
    
    // Compose BOM - для управления версиями
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Compose Foundation
    implementation("androidx.compose.foundation:foundation")
    
    // Compose Material 3
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Compose Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose для интеграции с View
    implementation("androidx.compose.ui:ui-viewbinding")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
