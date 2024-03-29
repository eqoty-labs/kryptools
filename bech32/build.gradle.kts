import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String


kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JvmTarget.JVM_1_8.target
        }
    }
    js(IR) {
        browser()
        nodejs()
    }
    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()
        nodejs()
    }
    iosArm64(); iosX64(); iosSimulatorArm64()
    tvosArm64(); tvosX64(); tvosSimulatorArm64()
    watchosArm32(); watchosArm64(); watchosX64(); watchosSimulatorArm64(); watchosDeviceArm64()
    macosX64(); macosArm64()
    linuxX64(); linuxArm64()
    mingwX64()
    androidNativeArm32(); androidNativeArm64(); androidNativeX86();androidNativeX64()

    applyDefaultHierarchyTemplate()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

    }
}
