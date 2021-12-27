plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }

        withJava()
    }

    js(IR) {
        // Declares that we want to compile for the browser and for nodejs
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":common"))

                implementation("io.ktor:ktor-client-core:${Versions.KTOR}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")
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