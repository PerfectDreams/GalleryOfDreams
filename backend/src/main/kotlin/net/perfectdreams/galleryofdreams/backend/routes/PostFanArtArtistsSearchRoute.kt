package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.components.fanArtArtist
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistWithFanArtCount
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.data.DeviantArtSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.*

class PostFanArtArtistsSearchRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/fan-art-artists") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val parameters = call.receiveParameters()
        val offset = parameters.getOrFail("offset").toInt()
        val query = parameters.getOrFail("query").lowercase()
        val sort = FanArtArtistSortOrder.valueOf(parameters.getOrFail("sort"))

        val results = m.searchFanArtArtists(sort, query, GalleryOfDreamsBackend.ARTIST_LIST_COUNT_PER_QUERY, offset)

        call.respondHtml {
            body {
                for (fanArtArtist in results) {
                    fanArtArtist(i18nContext, fanArtArtist.fanArtArtist, fanArtArtist.fanArtCount)
                }

                if (results.size == GalleryOfDreamsBackend.ARTIST_LIST_COUNT_PER_QUERY) {
                    div {
                        attributes["hx-post"] = "/${i18nContext.websiteLocaleIdPath}/fan-art-artists"
                        attributes["hx-trigger"] = "intersect once"
                        attributes["hx-swap"] = "afterend"
                        attributes["hx-include"] = "#fan-art-artists-filters"
                        attributes["hx-vals"] = buildJsonObject {
                            put("offset", offset + GalleryOfDreamsBackend.ARTIST_LIST_COUNT_PER_QUERY)
                        }.toString()
                    }
                }
            }
        }
    }
}