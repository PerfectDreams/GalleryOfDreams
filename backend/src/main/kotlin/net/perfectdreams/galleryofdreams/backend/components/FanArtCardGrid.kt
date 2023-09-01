package net.perfectdreams.galleryofdreams.backend.components

import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistWithFanArt
import net.perfectdreams.i18nhelper.core.I18nContext

fun FlowContent.fanArtCardGrid(m: GalleryOfDreamsBackend, i18nContext: I18nContext, dssBaseUrl: String, namespace: String, fanArts: List<FanArtArtistWithFanArt>) {
    if (fanArts.isEmpty()) {
        div {
            h1 {
                text("¯\\_(ツ)_/¯")
            }
            p {
                text("Nenhuma fan art é compatível com o seu filtro!")
            }
        }
    } else {
        div {
            style =
                "display: grid; grid-template-columns: repeat(auto-fill, minmax(192px, 1fr)); grid-template-rows: repeat(auto-fill, minmax(192px, 1fr)); gap: 1em; justify-content: space-between; width: 100%;"

            for (fanArt in fanArts) {
                fanArtCard(m, i18nContext, dssBaseUrl, namespace, fanArt.fanArtArtist, fanArt.fanArt)
            }
        }
    }
}
