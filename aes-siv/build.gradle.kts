import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
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
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    linuxArm64()
    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("dev.whyoleg.cryptography.DelicateCryptographyApi")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.dev.whyoleg.cryptography.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmMain {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.org.cryptomator.sivMode)
                implementation(libs.dev.whyoleg.cryptography.provider.jdk)
            }
        }
        jsMain {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.dev.whyoleg.cryptography.provider.webcrypto)
            }
        }
        val wasmJsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.dev.whyoleg.cryptography.provider.webcrypto)
            }
        }
        nativeMain {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.dev.whyoleg.cryptography.provider.openssl3.prebuilt)
            }
        }
    }
}
