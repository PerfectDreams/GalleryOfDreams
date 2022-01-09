plugins {
    kotlin("multiplatform") version "1.6.10" apply false
    kotlin("plugin.serialization") version "1.6.10" apply false
    id("org.jetbrains.compose") version "1.0.1-rc2" apply false
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