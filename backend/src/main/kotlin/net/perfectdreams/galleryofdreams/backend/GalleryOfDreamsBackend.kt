package net.perfectdreams.galleryofdreams.backend

import club.minnced.discord.webhook.WebhookClientBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
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
import net.perfectdreams.galleryofdreams.backend.routes.*
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
import net.perfectdreams.galleryofdreams.backend.utils.*
import net.perfectdreams.galleryofdreams.backend.utils.exposed.createOrUpdatePostgreSQLEnum
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.ResultSet
import java.time.Duration
import kotlin.concurrent.thread

class GalleryOfDreamsBackend(val languageManager: LanguageManager) {
    companion object {
        private val logger = KotlinLogging.logger {}
        val webhookLinkRegex = Regex("https?://(?:[A-z]+\\.)?discord\\.com/api/webhooks/([0-9]+)/([A-z0-9-_]+)")
        const val FAN_ARTS_PER_PAGE = 20
        const val ARTIST_LIST_COUNT_PER_QUERY = 25
    }

    val routes = listOf(
        GetHomeRoute(this),
        GetFanArtsListRoute(this),
        GetFanArtArtistRoute(this),
        GetFanArtRoute(this),
        PostFanArtArtistsSearchRoute(this),

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
        WebhookClientBuilder(webhookId.toLong(), webhookToken)
            .build()
    }
    val svgIconManager = SVGIconManager(this)

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

    suspend fun searchFanArtArtists(
        sortOrder: FanArtArtistSortOrder,
        query: String?,
        limit: Int,
        offset: Int
    ): List<FanArtArtistWithFanArtCount> {
        return transaction {
            // We do this manually because we can optimize it better (we don't want to pull all artists if they don't have enough fan arts, as an example)
            val results = mutableListOf<ResultRow>()

            (this.connection as JdbcConnectionImpl)
                .connection
                .let {
                    when (sortOrder) {
                        FanArtArtistSortOrder.FAN_ART_COUNT_ASCENDING -> {
                            it.prepareStatement("select * from fanartartists where fanartartists.name ilike ? order by (select count(*) from fanarts where fanarts.artist = fanartartists.id group by fanarts.artist) asc limit $limit offset $offset;")
                                .apply {
                                    this.setString(1, if (query == null) "%" else "%$query%")
                                }
                        }
                        FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING -> {
                            it.prepareStatement("select * from fanartartists where fanartartists.name ilike ? order by (select count(*) from fanarts where fanarts.artist = fanartartists.id group by fanarts.artist) desc limit $limit offset $offset;")
                                .apply {
                                    this.setString(1, if (query == null) "%" else "%$query%")
                                }
                        }
                        FanArtArtistSortOrder.ALPHABETICALLY_ASCENDING -> {
                            it.prepareStatement("select * from fanartartists where fanartartists.name ilike ? order by fanartartists.name asc limit $limit offset $offset;")
                                .apply {
                                    this.setString(1, if (query == null) "%" else "%$query%")
                                }
                        }
                        FanArtArtistSortOrder.ALPHABETICALLY_DESCENDING -> {
                            it.prepareStatement("select * from fanartartists where fanartartists.name ilike ? order by fanartartists.name desc limit $limit offset $offset;")
                                .apply {
                                    this.setString(1, if (query == null) "%" else "%$query%")
                                }
                        }
                    }
                }
                .executeQuery()
                .also {
                    while (it.next()) {
                        results.add(ResultRow.create(it, FanArtArtists.realFields.toSet().mapIndexed { index, expression -> expression to index }.toMap()))
                    }
                }

            results.map {
                val discordSocialConnections = FanArtArtistDiscordConnections.select {
                    FanArtArtistDiscordConnections.artist eq it[FanArtArtists.id]
                }
                val twitterSocialConnections = FanArtArtistTwitterConnections.select {
                    FanArtArtistTwitterConnections.artist eq it[FanArtArtists.id]
                }
                val deviantArtSocialConnections = FanArtArtistDeviantArtConnections.select {
                    FanArtArtistDeviantArtConnections.artist eq it[FanArtArtists.id]
                }

                val count = FanArts.select {
                    FanArts.artist eq it[FanArtArtists.id]
                }.count()

                FanArtArtistWithFanArtCount(
                    FanArtArtistX(
                        it[FanArtArtists.id].value,
                        it[FanArtArtists.slug],
                        it[FanArtArtists.name],
                        listOf(),
                        discordSocialConnections.map {
                            DiscordSocialConnection(it[FanArtArtistDiscordConnections.discordId])
                        } + twitterSocialConnections.map {
                            TwitterSocialConnection(it[FanArtArtistTwitterConnections.handle])
                        } + deviantArtSocialConnections.map {
                            DeviantArtSocialConnection(it[FanArtArtistDeviantArtConnections.handle])
                        },
                        FanArts.select {
                            FanArts.artist eq it[FanArtArtists.id].value
                        }.orderBy(FanArts.createdAt, SortOrder.DESC).limit(1).firstOrNull()?.let {
                            convertToFanArt(it)
                        }
                    ),
                    count
                )
            }
        }
    }

    fun convertToFanArt(fanArt: ResultRow) = FanArt(
        fanArt[FanArts.id].value,
        fanArt[FanArts.slug],
        fanArt[FanArts.title],
        fanArt[FanArts.description],
        fanArt[FanArts.createdAt],
        fanArt[FanArts.dreamStorageServiceImageId],
        fanArt[FanArts.file],
        fanArt[FanArts.preferredMediaType],
        FanArtTags.slice(FanArtTags.tag).select {
            FanArtTags.fanArt eq fanArt[FanArts.id]
        }.map { it[FanArtTags.tag] }
    )

    fun <T:Any> execAndMap(string: String, transform : (ResultSet) -> T) : List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(string) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }
}