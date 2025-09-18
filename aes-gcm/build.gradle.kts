@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

kotlin {
    jvm {}
    js(IR) {
        browser {
            testTask {
                useMocha {
                    timeout = "120s"
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "120s"
                }
            }
        }
    }
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
    androidNativeArm32(); androidNativeArm64(); androidNativeX86(); androidNativeX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        commonMain {
            dependencies {
                implementation(libs.dev.whyoleg.cryptography.core)
                implementation(libs.dev.whyoleg.cryptography.provider.optimal)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

