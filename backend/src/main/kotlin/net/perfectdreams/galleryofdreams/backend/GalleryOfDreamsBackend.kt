package net.perfectdreams.galleryofdreams.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import dev.kord.common.entity.Snowflake
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.dreamstorageservice.client.DreamStorageServiceClient
import net.perfectdreams.galleryofdreams.backend.plugins.configureRouting
import net.perfectdreams.galleryofdreams.backend.routes.GetFanArtArtistRoute
import net.perfectdreams.galleryofdreams.backend.routes.GetFanArtRoute
import net.perfectdreams.galleryofdreams.backend.routes.GetFanArtsListRoute
import net.perfectdreams.galleryofdreams.backend.routes.GetHomeRoute
import net.perfectdreams.galleryofdreams.backend.routes.GetSitemapRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.GetFanArtArtistByDiscordIdRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.GetFanArtsRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.GetLanguageInfoRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.PatchFanArtRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.PostArtistWithFanArtRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.PostCheckFanArtRoute
import net.perfectdreams.galleryofdreams.backend.routes.api.PostFanArtRoute
import net.perfectdreams.galleryofdreams.backend.tables.AuthorizationTokens
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.LanguageManager
import net.perfectdreams.galleryofdreams.backend.utils.WebhookClient
import net.perfectdreams.galleryofdreams.backend.utils.WebsiteAssetsHashManager
import net.perfectdreams.galleryofdreams.backend.utils.exposed.createOrUpdatePostgreSQLEnum
import net.perfectdreams.galleryofdreams.common.FanArtTag
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.concurrent.thread

class GalleryOfDreamsBackend(val languageManager: LanguageManager) {
    companion object {
        private val logger = KotlinLogging.logger {}
        val webhookLinkRegex = Regex("https?://(?:[A-z]+\\.)?discord\\.com/api/webhooks/([0-9]+)/([A-z0-9-_]+)")
    }

    val routes = listOf(
        GetHomeRoute(this),
        GetFanArtsListRoute(this),
        GetFanArtArtistRoute(this),
        GetFanArtRoute(this),

        GetSitemapRoute(this),

        // ===[ API ]===
        GetFanArtsRoute(this),
        GetLanguageInfoRoute(this),
        GetFanArtArtistByDiscordIdRoute(this),
        PostArtistWithFanArtRoute(this),
        PostFanArtRoute(this),
        PostCheckFanArtRoute(this),
        PatchFanArtRoute(this)
    )

    private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
    private val ISOLATION_LEVEL = IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!
    val database = connectToDatabase(
        HikariDataSource(
            createPostgreSQLHikari(
                System.getenv("GALLERYOFDREAMS_DATABASE_ADDRESS"),
                System.getenv("GALLERYOFDREAMS_DATABASE_NAME"),
                System.getenv("GALLERYOFDREAMS_DATABASE_USERNAME"),
                System.getenv("GALLERYOFDREAMS_DATABASE_PASSWORD")
            )
        )
    )
    val http = HttpClient {
        expectSuccess = false
        install(HttpTimeout) {
            this.requestTimeoutMillis = 120_000
            this.connectTimeoutMillis = 120_000
            this.socketTimeoutMillis = 120_000
        }
    }
    val dreamStorageServiceClient = DreamStorageServiceClient(
        System.getenv("GALLERYOFDREAMS_DSS_URL"),
        System.getenv("GALLERYOFDREAMS_DSS_TOKEN"),
        http
    )

    private val typesToCache = listOf(
        ContentType.Text.CSS,
        ContentType.Text.JavaScript,
        ContentType.Application.JavaScript,
        ContentType.Image.Any,
        ContentType.Video.Any
    )

    val hashManager = WebsiteAssetsHashManager()
    val websiteUrl = System.getenv("GALLERYOFDREAMS_WEBSERVER_URL").removeSuffix("/")
    val webhookClient = System.getenv("GALLERYOFDREAMS_DISCORD_WEBHOOK")?.let {
        val (_, webhookId, webhookToken) = webhookLinkRegex.matchEntire(it)?.groupValues ?: error("$it is not a valid Discord Webhook!")
        WebhookClient(Snowflake(webhookId), webhookToken)
    }

    fun start() {
        runBlocking {
            transaction {
                createOrUpdatePostgreSQLEnum(FanArtTag.values())

                SchemaUtils.createMissingTablesAndColumns(
                    FanArtArtists,
                    FanArts,
                    FanArtTags,
                    FanArtArtistDiscordConnections,
                    FanArtArtistTwitterConnections,
                    FanArtArtistDeviantArtConnections,
                    AuthorizationTokens
                )
            }
        }

        val server = embeddedServer(CIO, port = System.getenv("GALLERYOFDREAMS_WEBSERVER_PORT")?.toIntOrNull() ?: 8080) {
            install(CORS) {
                anyHost()
            }

            // Enables gzip and deflate compression
            install(Compression)

            install(IgnoreTrailingSlash)

            // Enables caching for the specified types in the typesToCache list
            install(CachingHeaders) {
                options { _, outgoingContent ->
                    val contentType = outgoingContent.contentType
                    if (contentType != null) {
                        val contentTypeWithoutParameters = contentType.withoutParameters()
                        val matches = typesToCache.any { contentTypeWithoutParameters.match(it) || contentTypeWithoutParameters == it }

                        if (matches)
                            CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 3600))
                        else
                            null
                    } else null
                }
            }

            routing {
                static("/assets/") {
                    resources("static/assets/")
                }

                resource("/favicon.svg", "static/favicon.svg")
            }

            configureRouting(this@GalleryOfDreamsBackend, routes)
        }.start(wait = true)

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                server.stop(15_000L, 15_000L)
            }
        )
    }

    fun createPostgreSQLHikari(address: String, databaseName: String, username: String, password: String): HikariConfig {
        val hikariConfig = createHikariConfig()
        hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName"

        hikariConfig.username = username
        hikariConfig.password = password

        return hikariConfig
    }

    private fun createHikariConfig(): HikariConfig {
        val hikariConfig = HikariConfig()

        hikariConfig.driverClassName = DRIVER_CLASS_NAME

        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        hikariConfig.isAutoCommit = false

        // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
        hikariConfig.leakDetectionThreshold = 30L * 1000
        hikariConfig.transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

        return hikariConfig
    }

    private fun connectToDatabase(dataSource: HikariDataSource): Database =
        Database.connect(
            HikariDataSource(dataSource),
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = DEFAULT_REPETITION_ATTEMPTS
                defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
            }
        )

    // https://github.com/JetBrains/Exposed/issues/1003
    suspend fun <T> transaction(repetitions: Int = 5, statement: suspend org.jetbrains.exposed.sql.Transaction.() -> T): T {
        var lastException: Exception? = null
        for (i in 1..repetitions) {
            try {
                return newSuspendedTransaction(Dispatchers.IO, database) {
                    statement.invoke(this)
                }
            } catch (e: ExposedSQLException) {
                logger.warn(e) { "Exception while trying to execute query. Tries: $i" }
                lastException = e
            }
        }
        throw lastException ?: RuntimeException("This should never happen")
    }
}