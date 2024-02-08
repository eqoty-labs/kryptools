import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.vanniktech.maven.publish)
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

object Targets {

    val iosTargets = arrayOf(
        "iosArm64", "iosX64", "iosSimulatorArm64",
    )
    val watchosTargets = arrayOf(
        "watchosArm64", "watchosDeviceArm64", "watchosX64", "watchosSimulatorArm64",
    )
    val tvosTargets = arrayOf(
        "tvosArm64", "tvosX64", "tvosSimulatorArm64"
    )
    val macosTargets = arrayOf(
        "macosX64", "macosArm64",
    )
    val darwinTargets = iosTargets + watchosTargets + tvosTargets + macosTargets
    val linuxTargets = arrayOf("linuxArm64", "linuxX64")
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
        browser {
            testTask(Action {
                useMocha {
                    timeout = "120s"
                }
            })
        }
        nodejs()
    }
    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()
        nodejs()
    }
    for (target in Targets.nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        val commonMain by getting {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-core:0.3.0-SNAPSHOT")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:0.3.0-SNAPSHOT")
            }
        }
        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.0-SNAPSHOT")
            }
        }
        val wasmJsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.0-SNAPSHOT")
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                // maybe better to not use prebuilt for every target:
                // https://whyoleg.github.io/cryptography-kotlin/modules/cryptography-provider-openssl3/
                implementation("dev.whyoleg.cryptography:cryptography-provider-openssl3-prebuilt:0.3.0-SNAPSHOT")
            }
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val darwinMain by creating {
            dependsOn(nativeMain)
        }
        val darwinTest by creating {
            dependsOn(nativeTest)
        }
        Targets.darwinTargets.forEach { target ->
            getByName("${target}Main") {
                dependsOn(darwinMain)
            }
            getByName("${target}Test") {
                dependsOn(darwinTest)
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
