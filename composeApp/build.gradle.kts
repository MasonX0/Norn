import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.apache.poi:poi:5.2.4")
            implementation("org.apache.poi:poi-ooxml:5.2.4")

            // Apache POI для Excel
            implementation("org.apache.poi:poi:5.2.4")
            implementation("org.apache.poi:poi-ooxml:5.2.4")
            implementation("org.apache.poi:poi-scratchpad:5.2.4")

            // Для старых форматов Excel
            implementation("org.apache.poi:poi-ooxml-full:5.2.4")

            // Зависимости для XML обработки
            implementation("org.apache.xmlbeans:xmlbeans:5.1.1")
            implementation("org.apache.commons:commons-compress:1.23.0")
            implementation("commons-io:commons-io:2.11.0")
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)  // Kotlin test framework
            implementation("org.junit.jupiter:junit-jupiter:5.9.2")  // JUnit 5
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")  // Для тестов с корутинами
        }
    }
}

android {
    namespace = "ru.bpo.norn"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.bpo.norn"
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
        mainClass = "ru.bpo.norn.jwmMain.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ru.bpo.norn"
            packageVersion = "1.0.0"
        }
    }
}
