import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

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
//            useEsModules()
        }
        nodejs()
    }
    wasmJs {
        browser {
        }
        nodejs()
    }
    val darwinTargets = mutableListOf<KotlinNativeTarget>()
    macosX64 {
        darwinTargets.add(this)
        setupCinterop(Target.MacosX64)
    }
    macosArm64 {
        darwinTargets.add(this)
        setupCinterop(Target.MacosArm64)
    }
    iosX64 {
        darwinTargets.add(this)
        setupCinterop(Target.IosSimulatorX64)
    }
    iosArm64 {
        darwinTargets.add(this)
        setupCinterop(Target.IosArm64)
    }
    iosSimulatorArm64 {
        darwinTargets.add(this)
        setupCinterop(Target.IosSimulatorArm64)
    }

    darwinTargets.forEach {
        it.apply {
            binaries.framework()
        }
    }
//    linuxX64()
    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        val commonMain by getting
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
            }
        }
        jsMain {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(npm("assert", "^2.1.0"))
                implementation(npm("@solar-republic/cosmos-grpc", "^0.12.0"))
                implementation(npm("@solar-republic/neutrino", "^1.0.3"))
            }
        }
        val wasmJsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(npm("@solar-republic/neutrino", "^1.0.3"))
            }
        }
    }
}

fun KotlinNativeTarget.setupCinterop(target: Target) =
    apply {
        compilations.getByName("main") {
            cinterops {
                val libAesSiv by creating {
                    defFile(project.file("src/nativeInterop/cinterop/libaes_siv.def"))
                    includeDirs.allHeaders(project.file("$projectDir/../nativelibs/libaes_siv/"))
                }
            }
            val buildFolderName = target.buildName
            val releaseFolderName = target.releaseFolderName
            val opensslTargetName = target.opensslTargetName
            kotlinOptions.freeCompilerArgs = listOf(
                "-include-binary",
                "$projectDir/../nativelibs/libaes_siv_build/$buildFolderName/$releaseFolderName/libaes_siv.a",
                "-include-binary",
                "$projectDir/../nativelibs/darwinopenssl/$opensslTargetName/lib/libcrypto.a"
            )
        }
    }

enum class Target(
    val taskSuffix: String,
    val buildName: String,
    val releaseFolderName: String,
    val opensslTargetName: String
) {
    MacosArm64("MacosArm64", "MAC_ARM64", "Release", "macosx"),
    MacosX64("MacosX64", "MAC", "Release", "macosx"),
    IosArm64("IosArm64", "OS64", "Release-iphoneos", "iphoneos"),
    IosSimulatorX64("IosX64", "SIMULATOR64", "Release-iphonesimulator", "iphonesimulator"),
    IosSimulatorArm64("IosSimulatorArm64", "SIMULATORARM64", "Release-iphonesimulator", "iphonesimulator")
}

fun makeLibAesSivTask(target: Target): Task =
    target.run {
        task<Exec>("makeLibAesSiv$taskSuffix") {
            workingDir = File("../nativelibs")
            commandLine("./make-libaes_siv.sh", buildName)
        }.apply {
            onlyIf {
                !file("../nativelibs/libaes_siv_build/$buildName/$releaseFolderName/libaes_siv.a").exists()
            }
        }
    }


tasks.findByName("cinteropLibAesSivMacosArm64")!!.dependsOn(makeLibAesSivTask(Target.MacosArm64))
tasks.findByName("cinteropLibAesSivMacosX64")!!.dependsOn(makeLibAesSivTask(Target.MacosX64))
tasks.findByName("cinteropLibAesSivIosArm64")!!.dependsOn(makeLibAesSivTask(Target.IosArm64))
tasks.findByName("cinteropLibAesSivIosX64")!!.dependsOn(makeLibAesSivTask(Target.IosSimulatorX64))
tasks.findByName("cinteropLibAesSivIosSimulatorArm64")!!.dependsOn(makeLibAesSivTask(Target.IosSimulatorArm64))

tasks.clean {
    doFirst {
        val libAesSivBuild = File("$projectDir/../nativelibs/libaes_siv_build")
        libAesSivBuild.deleteRecursively()
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

//rootProject.extensions.configure<NodeJsRootExtension> {
//    nodeVersion = "22.0.0-v8-canary202401102ecfc94f85"
//    nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
//}
//
//
//tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
//    args.add("--ignore-engines")
//}
//
