package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import net.perfectdreams.galleryofdreams.frontend.utils.classesAttrs
import net.perfectdreams.galleryofdreams.frontend.utils.icon
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun FanArtCard(
    m: GalleryOfDreamsFrontend,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext,
    fanArt: FanArt
) {
    val artist = data.artists.first { fanArt in it.fanArts }

    A(
        "/artists/${artist.slug}/${fanArt.slug}",
        attrs = {
            classes("fan-art-card")

            onClick {
                // Do not redirect
                it.preventDefault()

                m.routingManager.switchToFanArtOverview(i18nContext, artist, fanArt)
            }
        }
    ) {
        Div(attrs = { classes("fan-art-info-card") }) {
            Div(attrs = { classes("fan-art-tags") }) {
                for (tag in fanArt.tags.sortedByDescending { it.ordinal }) {
                    val tagIcon = tag.icon
                    if (tagIcon != null) {
                        UIIcon(tagIcon) {
                            style {
                                height(100.percent)
                            }
                        }
                    }
                }
            }

            Div(attrs = classesAttrs("fan-art-info")) {
                Div(attrs = classesAttrs("fan-art-info-wrapper")) {
                    Div(attrs = classesAttrs("fan-art-title")) {
                        Text(fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(artist.name)))
                    }

                    Div(attrs = classesAttrs("fan-art-avatar-artist-and-date")) {
                        FanArtArtistAvatar(data, artist, 32) {
                            borderRadius(100.percent)
                            height(32.px)
                            width(32.px)
                        }

                        Div(classesAttrs("fan-art-artist-and-date")) {
                            Div(attrs = classesAttrs("fan-art-artist")) {
                                Text(artist.name)
                            }
                            Div(attrs = classesAttrs("fan-art-info-date")) {
                                // TODO: Better formatting
                                // println("Zone IDs:")
                                // TimeZone.availableZoneIds.forEach {
                                //     println(it)
                                // }
                                val date = fanArt.createdAt
                                    .toLocalDateTime(TimeZone.UTC)
                                    .date

                                val year = date.year.toString().padStart(4, '0')
                                val month = date.monthNumber.toString().padStart(2, '0')
                                val day = date.dayOfMonth.toString().padStart(2, '0')
                                Text("$day/$month/$year")
                            }
                        }
                    }
                }
            }
        }

        val extension = MediaTypeUtils.convertContentTypeToExtension(fanArt.preferredMediaType)
        val fanArtUrl = "${data.dreamStorageServiceData.url}/${data.dreamStorageServiceData.namespace}/${StoragePaths.FanArt(fanArt.file).join()}.$extension"

        Img(
            fanArtUrl,
            alt = fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(artist.name)),
            attrs = {
                attr("loading", "lazy")

                style {
                    width(100.percent)
                    height(100.percent)
                    property("object-fit", "cover")
                }
            }
        )
    }
}