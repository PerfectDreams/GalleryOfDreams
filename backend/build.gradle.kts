plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version "3.1.4"
}

group = "net.perfectdreams.galleryofdreams"
version = Versions.GALLERY_OF_DREAMS

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.2")
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-html-builder:${Versions.KTOR}")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha11")
    implementation("commons-codec:commons-codec:1.15")

    // Databases
    implementation("org.jetbrains.exposed:exposed-core:0.36.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.36.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.36.2")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.36.2")
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("io.github.microutils:kotlin-logging:2.1.16")
    implementation("net.perfectdreams.dreamstorageservice:client:0.0.1-SNAPSHOT")
    implementation("pw.forst", "exposed-upsert", "1.1.0")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.5")

    // Internationalization + LanguageManager
    api("net.perfectdreams.i18nhelper.formatters:icu-messageformat-jvm:${Versions.I18N_HELPER}")
    implementation("com.charleskorn.kaml:kaml:0.35.0")
    implementation("com.ibm.icu:icu4j:69.1")
    implementation("org.yaml:snakeyaml:1.28")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")

    // Used for SEO purposes, by caching the Fan Art page result
    implementation("com.microsoft.playwright:playwright:1.17.0")

    // TODO: Remove later!
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:${Versions.KOTLINX_SERIALIZATION}")

    testImplementation("io.ktor:ktor-server-tests:${Versions.KTOR}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.31")
}

jib {
    container {
        mainClass = "net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackendLauncher"
    }

    to {
        image = "ghcr.io/perfectdreams/galleryofdreams"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "openjdk:17-slim-buster"
    }
}

val jsBrowserProductionWebpack = tasks.getByPath(":frontend:jsBrowserProductionWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

tasks {
    val sass = sassTask("style.scss")

    processResources {
        // We need to wait until the JS build finishes and the SASS files are generated
        dependsOn(jsBrowserProductionWebpack)
        dependsOn(sass)

        from("../resources/") // Include folders from the resources root folder

        // Copy the output from the frontend task to the backend resources
        from(jsBrowserProductionWebpack.destinationDirectory) {
            into("static/assets/js/")
        }

        // Same thing with the SASS output
        from(File(buildDir, "sass")) {
            into("static/assets/css/")
        }
    }
}