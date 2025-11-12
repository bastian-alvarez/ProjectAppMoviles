import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    id("de.mannodermaus.android-junit5") version "1.11.3.0"
    id("jacoco")
}

android {
    namespace = "com.example.uinavegacion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uinavegacion"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "de.mannodermaus.junit5.AndroidJUnit5Runner"

        buildConfigField("String", "CATALOGO_BASE_URL", "\"http://10.0.2.2:8086\"")
        buildConfigField("String", "USUARIO_BASE_URL", "\"http://10.0.2.2:8081\"")
        buildConfigField("String", "JUEGOS_BASE_URL", "\"http://10.0.2.2:8082\"")
        buildConfigField("String", "LICENCIA_BASE_URL", "\"http://10.0.2.2:8083\"")
        buildConfigField("String", "ORDEN_BASE_URL", "\"http://10.0.2.2:8084\"")
        buildConfigField("String", "RESENA_BASE_URL", "\"http://10.0.2.2:8085\"")
        buildConfigField("String", "ADMIN_BASE_URL", "\"http://10.0.2.2:8087\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.useJUnitPlatform()
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class.java) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val jacocoTestReport by tasks.registering(JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/*\$inlined\$*",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*"
        )
    }

    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    
    // Librerías para consumo de APIs REST sencillas
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.mockk:mockk:1.13.12")

    androidTestImplementation(platform("org.junit:junit-bom:5.10.2"))
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api")
    androidTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.12")

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //librerias nuevas
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Carga de imágenes en Compose
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // Window Size Classes para diseño adaptativo
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    
    // Activity Compose para window info
    implementation("androidx.activity:activity-compose:1.9.3")
}