plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("net.perfectdreams.i18nhelper.plugin") version Versions.I18N_HELPER
}

i18nHelper {
    generatedPackage.set("net.perfectdreams.galleryofdreams.common.i18n")
    languageSourceFolder.set("../resources/languages/en/")
}

kotlin {
    jvm()
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/languages")

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                implementation("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("net.perfectdreams.i18nhelper:core:${Versions.I18N_HELPER}")
            }
        }
    }
}