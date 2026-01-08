plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "data"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":uisystem"))

            // Room e SQLite
            api(libs.room.runtime)
            api(libs.sqlite.bundled)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "com.bitlabbr.minhadespensa.data"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspCommonMainMetadata", libs.room.compiler)

    // Android
    add("kspAndroid", libs.room.compiler)

    // IOS
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}