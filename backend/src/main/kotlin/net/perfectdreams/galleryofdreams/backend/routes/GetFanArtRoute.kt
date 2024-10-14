package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.meta
import kotlinx.serialization.json.JsonNull.content
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists.name
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags.fanArt
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.FanArts.preferredMediaType
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistWithFanArt
import net.perfectdreams.galleryofdreams.backend.utils.htmxElementTarget
import net.perfectdreams.galleryofdreams.backend.utils.pathWithoutLocale
import net.perfectdreams.galleryofdreams.backend.views.FanArtView
import net.perfectdreams.galleryofdreams.backend.views.FanArtsView
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.data.DeviantArtSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class GetFanArtRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/artists/{artistSlug}/{fanArtSlug}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val fanArtAndArtist = m.transaction {
            val data = FanArts.innerJoin(FanArtArtists)
                .selectAll().where {
                    FanArtArtists.slug eq call.parameters.getOrFail("artistSlug") and (FanArts.slug eq call.parameters.getOrFail(
                        "fanArtSlug"
                    ))
                }
                .first()

            val discordSocialConnections = FanArtArtistDiscordConnections.selectAll()
                .where { FanArtArtistDiscordConnections.artist eq data[FanArtArtists.id] }
            val twitterSocialConnections = FanArtArtistTwitterConnections.selectAll()
                .where { FanArtArtistTwitterConnections.artist eq data[FanArtArtists.id] }
            val deviantArtSocialConnections = FanArtArtistDeviantArtConnections.selectAll()
                .where { FanArtArtistDeviantArtConnections.artist eq data[FanArtArtists.id] }

            FanArtArtistWithFanArt(
                FanArtArtistX(
                    data[FanArtArtists.id].value,
                    data[FanArtArtists.slug],
                    data[name],
                    listOf(),
                    discordSocialConnections.map {
                        DiscordSocialConnection(it[FanArtArtistDiscordConnections.discordId])
                    } + twitterSocialConnections.map {
                        TwitterSocialConnection(it[FanArtArtistTwitterConnections.handle])
                    } + deviantArtSocialConnections.map {
                        DeviantArtSocialConnection(it[FanArtArtistDeviantArtConnections.handle])
                    },
                    FanArts.selectAll().where { FanArts.artist eq data[FanArtArtists.id].value }
                        .orderBy(FanArts.createdAt, SortOrder.DESC).limit(1).firstOrNull()?.let {
                        m.convertToFanArt(it)
                    }
                ),
                m.convertToFanArt(data)
            )
        }

        val view = FanArtView(
            m,
            i18nContext,
            i18nContext.get(I18nKeysData.WebsiteTitle),
            call.request.pathWithoutLocale(),
            fanArtAndArtist.fanArtArtist,
            fanArtAndArtist.fanArt
        )

        when (call.htmxElementTarget) {
            "content" -> {
                call.respondHtml {
                    body {
                        apply(view.rightSidebar())
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
}