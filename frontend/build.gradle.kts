plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.web.core)
                implementation(compose.runtime)

                api("io.ktor:ktor-client-js:${Versions.KTOR}")
                implementation("net.perfectdreams.i18nhelper.formatters:intl-messageformat-js:${Versions.I18N_HELPER}")
            }
        }
    }
}