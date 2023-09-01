package net.perfectdreams.galleryofdreams.backend.components

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.galleryofdreams.backend.utils.FanArtUtils
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX
import net.perfectdreams.i18nhelper.core.I18nContext

fun FlowContent.fanArtArtist(
    i18nContext: I18nContext,
    dssBaseUrl: String,
    namespace: String,
    artist: FanArtArtistX,
    fanArtCount: Long
) {
    div {
        a(classes = "entry", href = "/${i18nContext.websiteLocaleIdPath}/artists/${artist.slug}") {
            attributes["hx-target"] = "#content"

            val url = FanArtUtils.getArtistAvatarUrl(dssBaseUrl, namespace, artist, 32)

            img(src = url) {
                style = "object-fit: cover; border-radius: 100%;"
                attributes["loading"] = "lazy"
                width = "32"
                height = "32"
            }

            div {
                style = "display: flex; flex-direction: column;"
                attributes["power-close-sidebar"] = "true"

                div {
                    text(artist.name)
                }

                div {
                    style = "font-size: 0.8em;"
                    text("$fanArtCount fan arts")
                }
            }
        }
    }
}