package net.perfectdreams.galleryofdreams.backend.views

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.components.fanArtCard
import net.perfectdreams.galleryofdreams.backend.components.fanArtFilters
import net.perfectdreams.galleryofdreams.backend.components.pagination
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.FanArtUtils
import net.perfectdreams.galleryofdreams.backend.utils.icon
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.ResultRow
import kotlin.math.ceil

class FanArtView(
    m: GalleryOfDreamsBackend,
    i18nContext: I18nContext,
    title: String,
    pathWithoutLocaleId: String,
    dssBaseUrl: String,
    namespace: String,
    private val fanArtArtist: FanArtArtistX,
    private val fanArt: FanArt
) : DashboardView(m, i18nContext, title, pathWithoutLocaleId, dssBaseUrl, namespace) {
    override fun rightSidebar(): FlowContent.() -> (Unit) = {
        val extension = MediaTypeUtils.convertContentTypeToExtension(fanArt.preferredMediaType)
        val url = "https://assets.perfectdreams.media/galleryofdreams/fan-arts/${fanArt.file}"
        a(href = url) {
            img(src = url) {
                style = "max-width: 100%; max-height: 100%; height: 75vh; display: block; margin-left: auto; margin-right: auto; object-fit: contain;"
                alt = i18nContext.get(I18nKeysData.FanArtBy(fanArtArtist.name))
                attributes["loading"] = "lazy"
            }
        }

        div(classes = "fan-art-name-and-artist") {
            img(src = FanArtUtils.getArtistAvatarUrl(fanArtArtist, 64)) {
                style = "object-fit: cover; border-radius: 100%;"
                width = "64"
                height = "64"
            }

            div(classes = "name-and-artist-wrapper") {
                h1 {
                    text(fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(fanArtArtist.name)))
                }

                div {
                    text(fanArtArtist.name)
                }
            }
        }

        if (fanArt.tags.isNotEmpty()) {
            h2 {
                text("Tags:")
            }

            ul(classes = "fan-art-overview-tags") {
                for (tag in fanArt.tags) {
                    val tagIcon = tag.icon(m)

                    li {
                        if (tagIcon != null) {
                            tagIcon.apply(this) {
                                classes += "tag-badge"
                            }
                            text(" ")
                        }

                        text(i18nContext.get(tag.title))
                    }
                }
            }
        }
    }

    override fun HEAD.metaBlock() {
        meta(content = (fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(fanArtArtist.name)))) {
            attributes["property"] = "og:title"
        }
        meta(content = i18nContext.get(I18nKeysData.WebsiteTitle)) {
            attributes["property"] = "og:site_name"
        }
        meta(content = m.dreamStorageServiceClient.baseUrl + "/${this@FanArtView.namespace}/${StoragePaths.FanArt("${fanArt.file}.${MediaTypeUtils.convertContentTypeToExtension(fanArt.preferredMediaType)}").join()}") {
            attributes["property"] = "og:image"
        }
        meta(name = "twitter:card", content = "summary_large_image")
    }
}