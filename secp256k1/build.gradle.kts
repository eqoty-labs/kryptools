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

    iosArm64(); iosX64() ;iosSimulatorArm64()
    tvosArm64(); tvosX64(); tvosSimulatorArm64()
    watchosArm32(); watchosArm64(); watchosSimulatorArm64()
    macosX64(); macosArm64()
    linuxX64()
    mingwX64()

    // Todo: add targets as https://github.com/ionspin/kotlin-multiplatform-bignum adds support for them
    //watchosDeviceArm64()
    //watchosX64()
    //linuxArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.com.squareup.okio)
                implementation(libs.bignum)
                implementation(libs.bignum.serialization.kotlinx)
                implementation("dev.whyoleg.cryptography:cryptography-random:0.3.0-SNAPSHOT")
                implementation(libs.io.github.luca992.cash.z.ecc.android.kotlinBip39)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
