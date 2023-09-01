package net.perfectdreams.galleryofdreams.backend.components

import kotlinx.datetime.toLocalDateTime
import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.utils.FanArtUtils
import net.perfectdreams.galleryofdreams.backend.utils.aHtmx
import net.perfectdreams.galleryofdreams.backend.utils.icon
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext

fun FlowContent.fanArtCard(m: GalleryOfDreamsBackend, i18nContext: I18nContext, dssBaseUrl: String, namespace: String, fanArtArtist: FanArtArtistX, fanArt: FanArt) {
    aHtmx(classes = "fan-art-card", href = "/${i18nContext.websiteLocaleIdPath}/artists/${fanArtArtist.slug}/${fanArt.slug}", hxTarget = "#content") {
        div(classes = "fan-art-info-card") {
            div(classes = "fan-art-tags") {
                for (tag in fanArt.tags) {
                    val icon = tag.icon(m)
                    icon?.apply(this) {
                        style = "height: 100%;"
                    }
                }
            }

            div(classes = "fan-art-info") {
                div(classes = "fan-art-info-wrapper") {
                    div(classes = "fan-art-title") {
                        text(fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(fanArtArtist.name)))
                    }

                    div(classes = "fan-art-avatar-artist-and-date") {
                        img(src = FanArtUtils.getArtistAvatarUrl(dssBaseUrl, namespace, fanArtArtist, 32)) {
                            style = "object-fit: cover; border-radius: 100%;"
                            width = "32"
                            height = "32"
                            attributes["loading"] = "lazy"
                        }

                        div(classes = "fan-art-artist-and-date") {
                            div(classes = "fan-art-artist") {
                                text(fanArtArtist.name)
                            }
                            div(classes = "fan-art-info-date") {
                                val date = fanArt.createdAt
                                    .toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                                    .date

                                val year = date.year.toString().padStart(4, '0')
                                val month = date.monthNumber.toString().padStart(2, '0')
                                val day = date.dayOfMonth.toString().padStart(2, '0')
                                text("$day/$month/$year")
                            }
                        }
                    }
                }
            }
        }

        val extension = MediaTypeUtils.convertContentTypeToExtension(fanArt.preferredMediaType)
        img(src = "$dssBaseUrl/$namespace/fan-arts/${fanArt.file}.$extension") {
            style = "width: 100%; height: 100%; object-fit: cover;"
            attributes["loading"] = "lazy"
            alt = i18nContext.get(I18nKeysData.FanArtBy(fanArtArtist.name))
        }
    }
}