import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.junit5)
    alias(libs.plugins.jacoco)
}

room {
    schemaDirectory("$projectDir/schemas")
}

val dynamicVersionCode: Int? = System.getenv("VERSION_CODE")?.toIntOrNull()
val dynamicVersionName: String? = System.getenv("VERSION_NAME")

android {
    namespace = "dev.alimansour.mytasks"
    compileSdk = 36

    signingConfigs {
        if (!project.getSecret("KEYSTORE_PASSWORD").isNullOrEmpty()) {
            create("release") {
                storeFile =
                    project.rootProject.layout.projectDirectory
                        .file("release-key.jks")
                        .asFile
                storePassword = project.getSecret("KEYSTORE_PASSWORD")
                keyAlias = project.getSecret("KEY_ALIAS")
                keyPassword = project.getSecret("KEY_PASSWORD")
                enableV1Signing = true
                enableV2Signing = true
            }
        }

        getByName("debug") {
            storeFile = File(System.getProperty("user.home"), ".android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    defaultConfig {
        applicationId = "dev.alimansour.mytasks"
        minSdk = 24
        targetSdk = 36
        versionCode = dynamicVersionCode ?: 1
        versionName = dynamicVersionName ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            isDebuggable = false
            if ("release" in signingConfigs.names) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    sourceSets {
        getByName("test") {
            resources.srcDir("src/test/resources")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val debugTreeKotlin =
        fileTree(
            mapOf(
                "dir" to "${layout.buildDirectory}/tmp/kotlin-classes/debug",
                "excludes" to
                    listOf(
                        "**/R.class",
                        "**/R$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "**/*_MembersInjector.class",
                        "**/di/**",
                        "**/*_Factory*.class",
                        "**/*_Provide*Factory*.class",
                        "**/*_Impl*.class",
                        "**/*Companion*.class",
                    ),
            ),
        )

    val debugTreeJava =
        fileTree(
            mapOf(
                "dir" to "${layout.buildDirectory}/intermediates/javac/debug/classes",
                "excludes" to
                    listOf(
                        "**/R.class",
                        "**/R$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                    ),
            ),
        )

    classDirectories.setFrom(files(debugTreeKotlin, debugTreeJava))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "**/jacoco/testDebugUnitTest.exec",
                "**/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "**/jacoco/test.exec",
            )
        },
    )
}

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_CONFIG_CHECK", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation)
    implementation(libs.splashScreen)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.timber)
    implementation(libs.coroutines.core)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.core)
    implementation(libs.bundles.koin.android)
    ksp(libs.koin.ksp.compiler)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

private fun Project.getSecret(key: String): String? {
    val localProperties =
        Properties().apply {
            val propertiesFile = rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                propertiesFile.inputStream().use { load(it) }
            }
        }
    return localProperties.getProperty(key) ?: System.getenv(key)
}
