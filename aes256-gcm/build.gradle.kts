import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
        browser {
            testTask(Action {
                useMocha {
                    timeout = "120s"
                }
            })
        }
        nodejs()
    }
    @Suppress("OPT_IN_USAGE") wasmJs {
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
        commonMain {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-core:0.3.0-SNAPSHOT")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmMain {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:0.3.0")
            }
        }
        jsMain {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.0")
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.0")
            }
        }
        nativeMain {
            dependencies {
                // maybe better to not use prebuilt for every target:
                // https://whyoleg.github.io/cryptography-kotlin/modules/cryptography-provider-openssl3/
                implementation("dev.whyoleg.cryptography:cryptography-provider-openssl3-prebuilt:0.3.0")
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
