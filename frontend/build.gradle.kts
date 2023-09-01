plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile> {
    kotlinOptions {
        // Jetpack Compose doesn't support Kotlin 1.7.10 yet, but the latest version seems to compile just fine under Kotlin 1.7.10
        freeCompilerArgs += listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true")
    }
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
                implementation(compose.html.core)
                implementation(compose.runtime)
                api(npm("htmx.org", "1.9.5"))

                api("io.ktor:ktor-client-js:${Versions.KTOR}")
                api("net.perfectdreams.i18nhelper.formatters:intl-messageformat-js:${Versions.I18N_HELPER}")
            }
        }
    }
}