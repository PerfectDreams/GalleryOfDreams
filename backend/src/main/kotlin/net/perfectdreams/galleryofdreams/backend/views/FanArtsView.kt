package net.perfectdreams.galleryofdreams.backend.views

import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.components.fanArtCard
import net.perfectdreams.galleryofdreams.backend.components.fanArtCardGrid
import net.perfectdreams.galleryofdreams.backend.components.fanArtFilters
import net.perfectdreams.galleryofdreams.backend.components.pagination
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistWithFanArt
import net.perfectdreams.galleryofdreams.backend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.ResultRow
import kotlin.math.ceil

class FanArtsView(
    m: GalleryOfDreamsBackend,
    i18nContext: I18nContext,
    title: String,
    pathWithoutLocaleId: String,
    dssBaseUrl: String,
    namespace: String,
    private val fanArtSortOrder: FanArtSortOrder,
    private val fanArtTags: List<FanArtTag>?,
    private val page: Int,
    private val fanArts: List<FanArtArtistWithFanArt>,
    private val totalFanArts: Long
) : DashboardView(m, i18nContext, title, pathWithoutLocaleId, dssBaseUrl, namespace) {
    override fun rightSidebar(): FlowContent.() -> (Unit) = {
        div {
            id = "fan-arts-grid"

            form(method = FormMethod.get, action = "/${i18nContext.websiteLocaleIdPath}/fan-arts") {
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
        form(method = FormMethod.get, action = "/${i18nContext.websiteLocaleIdPath}/fan-arts") {
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

            fanArtCardGrid(m, i18nContext, dssBaseUrl, this@FanArtsView.namespace, fanArts)
            
            div {
                style = "text-align: center;"

                pagination(page - 1, ceil(totalFanArts / GalleryOfDreamsBackend.FAN_ARTS_PER_PAGE.toDouble()).toInt())
            }
        }
    }
}