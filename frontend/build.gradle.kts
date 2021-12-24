plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("io.ktor:ktor-client-js:1.6.7")
                implementation("net.perfectdreams.i18nhelper.formatters:intl-messageformat-js:${Versions.I18N_HELPER}")
            }
        }
    }
}