package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.body
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.htmxElementTarget
import net.perfectdreams.galleryofdreams.backend.utils.pathWithoutLocale
import net.perfectdreams.galleryofdreams.backend.views.HomeView
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.selectAll

class GetHomeRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val view = HomeView(
            m,
            i18nContext,
            i18nContext.get(I18nKeysData.WebsiteTitle),
            call.request.pathWithoutLocale()
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
                val fanArtArtists = m.searchFanArtArtists(
                    FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING,
                    null,
                    GalleryOfDreamsBackend.ARTIST_LIST_COUNT_PER_QUERY,
                    0
                )

                val totalFanArts = m.transaction { FanArts.selectAll().count() }

                call.respondHtml {
                    apply(view.generateHtml(totalFanArts, fanArtArtists))
                }
            }
        }
    }
}