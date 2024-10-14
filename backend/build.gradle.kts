plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version Versions.JIB
}

group = "net.perfectdreams.galleryofdreams"
version = Versions.GALLERY_OF_DREAMS

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-html-builder:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-cors:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")

    implementation("ch.qos.logback:logback-classic:1.5.10")
    implementation("commons-codec:commons-codec:1.15")

    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.16.1")

    // Databases
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.36.2")
    implementation("org.postgresql:postgresql:42.3.3")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("net.perfectdreams.dreamstorageservice:client:2.0.2")
    implementation("pw.forst", "exposed-upsert", "1.1.0")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.5")

    // Internationalization + LanguageManager
    api("net.perfectdreams.i18nhelper.formatters:icu-messageformat-jvm:${Versions.I18N_HELPER}")
    implementation("com.charleskorn.kaml:kaml:0.35.0")
    implementation("com.ibm.icu:icu4j:71.1")
    implementation("org.yaml:snakeyaml:1.30")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")

    // https://mvnrepository.com/artifact/club.minnced/discord-webhooks
    implementation("club.minnced:discord-webhooks:0.8.2")

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

        image = "openjdk:17-slim-bullseye"
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