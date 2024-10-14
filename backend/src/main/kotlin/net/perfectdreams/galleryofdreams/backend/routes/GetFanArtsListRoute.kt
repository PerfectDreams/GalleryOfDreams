package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.body
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.*
import net.perfectdreams.galleryofdreams.backend.views.FanArtsView
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.*
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery

class GetFanArtsListRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/fan-arts") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val page = call.parameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val sortOrder = call.parameters["sort"]?.let { FanArtSortOrder.valueOf(it) } ?: FanArtSortOrder.DATE_DESCENDING
        val tags = call.parameters.getAll("tags")?.map { FanArtTag.valueOf(it) }

        val zeroIndexedPage = page - 1

        val result = m.transaction {
            val query = FanArts.selectAll().where {
                (if (tags == null) Op.TRUE eq Op.TRUE else FanArts.id inSubQuery FanArtTags.select(FanArtTags.fanArt).where { FanArtTags.tag inList tags })
            }.orderBy(
                FanArts.createdAt, when (sortOrder) {
                    FanArtSortOrder.DATE_ASCENDING -> SortOrder.ASC
                    FanArtSortOrder.DATE_DESCENDING -> SortOrder.DESC
                }
            )

            // YES THE ORDER MATTERS BECAUSE THE QUERY IS MUTABLE
            val totalFanArts = query.count()
            val fanArts =
                query.limit(GalleryOfDreamsBackend.FAN_ARTS_PER_PAGE, (zeroIndexedPage * 20).toLong()).toList()

            val mappedFanArts = mutableListOf<FanArtArtistWithFanArt>()

            for (fanArt in fanArts) {
                val fanArtArtist =
                    FanArtArtists.selectAll().where { FanArtArtists.id eq fanArt[FanArts.artist] }.first()

                val discordSocialConnections = FanArtArtistDiscordConnections.selectAll()
                    .where { FanArtArtistDiscordConnections.artist eq fanArtArtist[FanArtArtists.id] }
                val twitterSocialConnections = FanArtArtistTwitterConnections.selectAll()
                    .where { FanArtArtistTwitterConnections.artist eq fanArtArtist[FanArtArtists.id] }
                val deviantArtSocialConnections = FanArtArtistDeviantArtConnections.selectAll()
                    .where { FanArtArtistDeviantArtConnections.artist eq fanArtArtist[FanArtArtists.id] }

                mappedFanArts.add(
                    FanArtArtistWithFanArt(
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
                            FanArts.selectAll().where { FanArts.artist eq fanArtArtist[FanArtArtists.id].value }
                                .orderBy(FanArts.createdAt, SortOrder.DESC).limit(1).firstOrNull()?.let {
                                m.convertToFanArt(it)
                            }
                        ),
                        m.convertToFanArt(fanArt)
                    )
                )
            }

            QueryResult(
                mappedFanArts,
                totalFanArts
            )
        }

        val view = FanArtsView(
            m,
            i18nContext,
            i18nContext.get(I18nKeysData.WebsiteTitle),
            call.request.pathWithoutLocale(),
            sortOrder,
            tags,
            page,
            result.fanArts,
            result.totalFanArts
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
        val fanArts: List<FanArtArtistWithFanArt>,
        val totalFanArts: Long
    )
}