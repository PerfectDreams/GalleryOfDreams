package net.perfectdreams.galleryofdreams.backend.views

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.components.fanArtCard
import net.perfectdreams.galleryofdreams.backend.components.fanArtCardGrid
import net.perfectdreams.galleryofdreams.backend.components.fanArtFilters
import net.perfectdreams.galleryofdreams.backend.components.pagination
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistWithFanArt
import net.perfectdreams.galleryofdreams.backend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.ResultRow
import kotlin.math.ceil

class ArtistFanArtsView(
    m: GalleryOfDreamsBackend,
    i18nContext: I18nContext,
    title: String,
    pathWithoutLocaleId: String,
    private val artistSlug: String,
    private val fanArtSortOrder: FanArtSortOrder,
    private val fanArtTags: List<FanArtTag>?,
    private val page: Int,
    private val fanArtArtist: FanArtArtistX,
    private val fanArts: List<FanArt>,
    private val totalFanArts: Long
) : DashboardView(m, i18nContext, title, pathWithoutLocaleId) {
    override fun rightSidebar(): FlowContent.() -> (Unit) = {
        div {
            id = "fan-arts-wrapper"

            h1 {
                text(fanArtArtist.name)
            }

            form(method = FormMethod.get, action = "/${i18nContext.websiteLocaleIdPath}/artists/$artistSlug") {
                attributes["hx-target"] = "#fan-arts-grid-and-pagination"
                attributes["hx-get"] = action
                attributes["hx-push-url"] = "true"

                // Reset when changing the filter
                input(InputType.hidden) {
                    name = "page"
                    value = "1"
                }

                fanArtFilters(m, i18nContext, fanArtSortOrder, fanArtTags)
            }

            hr {}

            apply(fanArtGrid())
        }
    }

    fun fanArtGrid(): FlowContent.() -> (Unit) = {
        form(method = FormMethod.get, action = "/${i18nContext.websiteLocaleIdPath}/artists/$artistSlug") {
            attributes["hx-target"] = "#fan-arts-grid-and-pagination"
            attributes["hx-get"] = action
            attributes["hx-push-url"] = "true"

            id = "fan-arts-grid-and-pagination"

            // Keep current filters
            input(InputType.hidden) {
                name = "sort"
                value = fanArtSortOrder.name
            }

            if (fanArtTags != null) {
                for (tag in fanArtTags) {
                    input(InputType.hidden) {
                        name = "tags"
                        value = tag.name
                    }
                }
            }

            id = "fan-arts-grid-and-pagination"

            fanArtCardGrid(m, i18nContext, fanArts.map { FanArtArtistWithFanArt(fanArtArtist, it) })

            div {
                style = "text-align: center;"

                pagination(page - 1, ceil(totalFanArts / GalleryOfDreamsBackend.FAN_ARTS_PER_PAGE.toDouble()).toInt())
            }
        }
    }
}