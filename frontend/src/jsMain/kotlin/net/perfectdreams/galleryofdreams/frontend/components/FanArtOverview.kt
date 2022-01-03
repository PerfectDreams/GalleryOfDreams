package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.screen.Screen
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import net.perfectdreams.galleryofdreams.frontend.utils.classesAttrs
import net.perfectdreams.galleryofdreams.frontend.utils.icon
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

@Composable
fun FanArtOverview(
    m: GalleryOfDreamsFrontend,
    screen: Screen.FanArtOverview,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext
) {
    val artist = screen.fanArtArtist
    val fanArt = screen.fanArt

    val extension = MediaTypeUtils.convertContentTypeToExtension(fanArt.preferredMediaType)
    val fanArtUrl = "${data.dreamStorageServiceData.url}/${data.dreamStorageServiceData.namespace}/${StoragePaths.FanArt(fanArt.file).join()}.$extension"

    A(href = fanArtUrl, attrs = { attr("target", "_blank") }) {
        Img(
            src = fanArtUrl,
            alt = fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(artist.name)),
            attrs = {
                attr("loading", "lazy")

                style {
                    maxWidth(100.percent)
                    maxHeight(100.percent)
                    height(75.vh)
                    display(DisplayStyle.Block)
                    property("margin-left", "auto")
                    property("margin-right", "auto")
                    property("object-fit", "contain")
                }
            }
        )
    }

    Div(attrs = classesAttrs("fan-art-name-and-artist")) {
        FanArtArtistAvatar(data, artist, 64, 64, 64) {
            borderRadius(100.percent)
        }

        Div(attrs = classesAttrs("name-and-artist-wrapper")) {
            H1 {
                Text(fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(artist.name)))
            }
            Div {
                Text(artist.name)
            }
        }
    }

    val fanArtDescription = fanArt.description
    if (fanArtDescription != null)
        P {
            Text(fanArtDescription)
        }

    if (fanArt.tags.isNotEmpty()) {
        H2 {
            Text("Tags:")
        }

        for (tag in fanArt.tags.sortedBy { it.ordinal }) {
            val tagIcon = tag.icon
            Ul(attrs = classesAttrs("fan-art-overview-tags")) {
                Li {
                    if (tagIcon != null) {
                        UIIcon(tagIcon) {
                            classes("tag-badge")
                        }

                        Text(" ${i18nContext.get(tag.title)}")
                    } else {
                        Text(i18nContext.get(tag.title))
                    }
                }
            }
        }
    }
}