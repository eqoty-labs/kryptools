import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

object Targets {

    val iosTargets = arrayOf("iosArm64", "iosX64", "iosSimulatorArm64")
    val tvosTargets = arrayOf("tvosArm64", "tvosX64", "tvosSimulatorArm64")
    val watchosTargets = arrayOf(
        "watchosArm32", "watchosArm64", "watchosX64", "watchosSimulatorArm64", "watchosDeviceArm64"
    )
    val macosTargets = arrayOf("macosX64", "macosArm64")
    val darwinTargets = iosTargets + tvosTargets + watchosTargets + macosTargets
    val linuxTargets = arrayOf("linuxX64", "linuxArm64")
    val mingwTargets = arrayOf("mingwX64")
    val androidTargets = arrayOf(
        "androidNativeArm32", "androidNativeArm64", "androidNativeX86", "androidNativeX64",
    )
    val nativeTargets = linuxTargets + darwinTargets + mingwTargets

}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    js(IR) {
        browser {
            testTask {
                useMocha {
                    timeout = "4s"
                }
            }
        }
        nodejs()
    }
    @Suppress("OPT_IN_USAGE")
    wasm{
        browser()
        nodejs()
        d8()
    }
    for (target in Targets.nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
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
