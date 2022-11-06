import com.vanniktech.maven.publish.SonatypeHost

plugins {
    @Suppress("DSL_SCOPE_VIOLATION")
    val libs = libs
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
    val watchosTargets = arrayOf(
        "watchosArm64", "watchosX86", "watchosX64", "watchosSimulatorArm64"
    )
    val tvosTargets = arrayOf(
        "tvosArm64", "tvosX64", "tvosSimulatorArm64"
    )
    val macosTargets = arrayOf(
        "macosX64", "macosArm64",
    )
    val darwinTargets = iosTargets + watchosTargets + tvosTargets + macosTargets
    val linuxTargets = arrayOf<String>()//("linuxArm64", "linuxX64")
    val mingwTargets = arrayOf<String>()//("mingwX64", "mingwX86")
    val nativeTargets = linuxTargets + darwinTargets + mingwTargets

}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    js(IR) {
        browser {
            testTask {
                useMocha {
                    timeout = "20s"
                }
            }
        }
        nodejs()
    }
    for (target in Targets.nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(npm("@peculiar/webcrypto", "^1.4.1"))
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
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

plugins.withId("com.vanniktech.maven.publish.base") {
    mavenPublishing {
        publishToMavenCentral(SonatypeHost.S01)
        signAllPublications()
        pomFromGradleProperties()
    }
}
