plugins {
    kotlin("multiplatform") version Versions.KOTLIN apply false
    kotlin("plugin.serialization") version Versions.KOTLIN apply false
    id("org.jetbrains.compose") version "1.7.0-alpha03" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

allprojects {
    group = "net.perfectdreams.galleryofdreams"
    version = Versions.GALLERY_OF_DREAMS

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.perfectdreams.net/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        google()
    }
}