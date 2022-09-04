plugins {
    kotlin("multiplatform") version Versions.KOTLIN apply false
    kotlin("plugin.serialization") version Versions.KOTLIN apply false
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev750" apply false // 1.2.0-alpha01-dev750 supports Kotlin 1.7.10
}

allprojects {
    group = "net.perfectdreams.galleryofdreams"
    version = Versions.GALLERY_OF_DREAMS

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.perfectdreams.net/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        google()
    }
}