plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("net.perfectdreams.i18nhelper.plugin") version Versions.I18N_HELPER
    id("maven-publish")
}

val generateI18nKeys = tasks.register<net.perfectdreams.i18nhelper.plugin.GenerateI18nKeysTask>("generateI18nKeys") {
    generatedPackage.set("net.perfectdreams.galleryofdreams.common.i18n")
    languageSourceFolder.set(file("../resources/languages/en/"))
    languageTargetFolder.set(file("$buildDir/generated/languages"))
    translationLoadTransform.set { file, map -> map }
}

kotlin {
    jvm()
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateI18nKeys)

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("net.perfectdreams.i18nhelper:core:${Versions.I18N_HELPER}")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")
            credentials(PasswordCredentials::class)
        }
    }
}