package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.util.*
import kotlinx.html.body
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.htmxElementTarget
import net.perfectdreams.galleryofdreams.backend.utils.pathWithoutLocale
import net.perfectdreams.galleryofdreams.backend.views.ArtistFanArtsView
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.*
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class GetFanArtArtistRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/artists/{artistSlug}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val artistSlug = call.parameters.getOrFail("artistSlug")
        val page = call.parameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val sortOrder = call.parameters["sort"]?.let { FanArtSortOrder.valueOf(it) } ?: FanArtSortOrder.DATE_DESCENDING
        val tags = call.parameters.getAll("tags")?.map { FanArtTag.valueOf(it) }

        val zeroIndexedPage = page - 1

        val fanArts = m.transaction {
            val fanArtArtist = FanArtArtists.select {
                FanArtArtists.slug eq artistSlug
            }.first()

            val discordSocialConnections = FanArtArtistDiscordConnections.select {
                FanArtArtistDiscordConnections.artist eq fanArtArtist[FanArtArtists.id]
            }
            val twitterSocialConnections = FanArtArtistTwitterConnections.select {
                FanArtArtistTwitterConnections.artist eq fanArtArtist[FanArtArtists.id]
            }
            val deviantArtSocialConnections = FanArtArtistDeviantArtConnections.select {
                FanArtArtistDeviantArtConnections.artist eq fanArtArtist[FanArtArtists.id]
            }

            val query = FanArts.select {
                FanArts.artist eq fanArtArtist[FanArtArtists.id] and (if (tags == null) Op.TRUE eq Op.TRUE else FanArts.id inSubQuery FanArtTags.slice(FanArtTags.fanArt).select { FanArtTags.tag inList tags })
            }.orderBy(
                FanArts.createdAt, when (sortOrder) {
                    FanArtSortOrder.DATE_ASCENDING -> SortOrder.ASC
                    FanArtSortOrder.DATE_DESCENDING -> SortOrder.DESC
                }
            )

            // YES THE ORDER MATTERS BECAUSE THE QUERY IS MUTABLE
            val totalFanArts = query.count()
            val fanArts = query.limit(GalleryOfDreamsBackend.FAN_ARTS_PER_PAGE, (zeroIndexedPage * 20).toLong()).toList()

            QueryResult(
                FanArtArtistX(
                    fanArtArtist[FanArtArtists.id].value,
                    fanArtArtist[FanArtArtists.slug],
                    fanArtArtist[FanArtArtists.name],
                    listOf(),
                    discordSocialConnections.map {
                        DiscordSocialConnection(it[FanArtArtistDiscordConnections.discordId])
                    } + twitterSocialConnections.map {
                        TwitterSocialConnection(it[FanArtArtistTwitterConnections.handle])
                    } + deviantArtSocialConnections.map {
                        DeviantArtSocialConnection(it[FanArtArtistDeviantArtConnections.handle])
                    },
                    FanArts.select {
                        FanArts.artist eq fanArtArtist[FanArtArtists.id].value
                    }.orderBy(FanArts.createdAt, SortOrder.DESC).limit(1).firstOrNull()?.let {
                        m.convertToFanArt(it)
                    }
                ),
                fanArts.map {
                    FanArt(
                        it[FanArts.id].value,
                        it[FanArts.slug],
                        it[FanArts.title],
                        it[FanArts.description],
                        it[FanArts.createdAt],
                        it[FanArts.dreamStorageServiceImageId],
                        it[FanArts.file],
                        it[FanArts.preferredMediaType],
                        FanArtTags.slice(FanArtTags.tag).select {
                            FanArtTags.fanArt eq it[FanArts.id]
                        }.map { it[FanArtTags.tag] }
                    )
                },
                totalFanArts
            )
        }

        val view = ArtistFanArtsView(
            m,
            i18nContext,
            i18nContext.get(I18nKeysData.WebsiteTitle),
            call.request.pathWithoutLocale(),
            m.dreamStorageServiceClient.baseUrl,
            m.dreamStorageServiceClient.getCachedNamespaceOrRetrieve(),
            artistSlug,
            sortOrder,
            tags,
            page,
            fanArts.fanArtArtist,
            fanArts.fanArts,
            fanArts.totalFanArts
        )

        when (call.htmxElementTarget) {
            "content" -> {
                call.respondHtml {
                    body {
                        apply(view.rightSidebar())
                    }
                }
            }
            "fan-arts-grid-and-pagination" -> {
                call.respondHtml {
                    body {
                        apply(view.fanArtGrid())
                    }
                }
            }
            else -> {
                val fanArtArtists = m.searchFanArtArtists(FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING, null, GalleryOfDreamsBackend.ARTIST_LIST_COUNT_PER_QUERY, 0)
                val totalFanArts = m.transaction { FanArts.selectAll().count() }

                call.respondHtml {
                    apply(view.generateHtml(totalFanArts, fanArtArtists))
                }
            }
        }
    }

    data class QueryResult(
        val fanArtArtist: FanArtArtistX,
        val fanArts: List<FanArt>,
        val totalFanArts: Long
    )
}