import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

object Targets {

    val iosTargets = arrayOf(
        "iosArm64", "iosX64", "iosSimulatorArm64",
    )

    // Todo: add targets as https://github.com/ionspin/kotlin-multiplatform-bignum adds support for them
    val watchosTargets = arrayOf(
        "watchosArm32", "watchosArm64", "watchosSimulatorArm64",
    )
    val tvosTargets = arrayOf(
        "tvosArm64", "tvosX64", "tvosSimulatorArm64"
    )
    val macosTargets = arrayOf(
        "macosX64", "macosArm64",
    )
    val darwinTargets = iosTargets + watchosTargets + tvosTargets + macosTargets

    // Todo: add targets as https://github.com/ionspin/kotlin-multiplatform-bignum adds support for them
    val linuxTargets = arrayOf("linuxX64")
    val mingwTargets = arrayOf("mingwX64")
    val nativeTargets = linuxTargets + darwinTargets + mingwTargets

}

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
    for (target in Targets.nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }

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

// https://youtrack.jetbrains.com/issue/KT-46466
val dependsOnTasks = mutableListOf<String>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOnTasks.add(this.name.replace("publish", "sign").replaceAfter("Publication", ""))
    dependsOn(dependsOnTasks)
}

plugins.withId("com.vanniktech.maven.publish") {
    mavenPublishing {
        publishToMavenCentral(SonatypeHost.S01)
        signAllPublications()
    }
}
