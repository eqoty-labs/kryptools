rootProject.name = "kryptools"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        // See https://jmfayard.github.io/refreshVersions
        id("de.fayard.refreshVersions") version "0.60.5"
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenCentral()
        google()
    }
}


plugins {
    id("de.fayard.refreshVersions")
}


refreshVersions {
//    rejectVersionIf {
//        @Suppress("UnstableApiUsage")
//        candidate.stabilityLevel != StabilityLevel.Stable
//    }
}

include(":aes-siv")
include(":aes-gcm")
include(":axlsign")
include(":bech32")
include(":secp256k1")
