package net.perfectdreams.galleryofdreams.backend.views

import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.components.fanArtCard
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
            form(method = FormMethod.get, action = "/${i18nContext.websiteLocaleIdPath}/fan-arts") {
                id = "fan-arts-grid"
                attributes["hx-target"] = "#fan-arts-grid-and-pagination"

                fanArtFilters(m, i18nContext, fanArtSortOrder, fanArtTags)

                hr {}

                apply(fanArtGrid())
            }
        }
    }

    fun fanArtGrid(): FlowContent.() -> (Unit) = {
        div {
            id = "fan-arts-grid-and-pagination"

            div {
                style =
                    "display: grid; grid-template-columns: repeat(auto-fill, minmax(192px, 1fr)); grid-template-rows: repeat(auto-fill, minmax(192px, 1fr)); gap: 1em; justify-content: space-between; width: 100%;"

                for (fanArt in fanArts) {
                    fanArtCard(m, i18nContext, dssBaseUrl, this@FanArtsView.namespace, fanArt.fanArtArtist, fanArt.fanArt)
                }
            }

            div {
                style = "text-align: center;"

                pagination(page - 1, ceil(totalFanArts / GalleryOfDreamsBackend.FAN_ARTS_PER_PAGE.toDouble()).toInt())
            }
        }
    }
}