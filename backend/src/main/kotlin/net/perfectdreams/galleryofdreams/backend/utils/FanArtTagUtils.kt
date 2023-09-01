package net.perfectdreams.galleryofdreams.backend.utils

import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.common.FanArtTag

fun FanArtTag.icon(m: GalleryOfDreamsBackend): SVGIconManager.SVGIcon? {
    return when (this) {
        FanArtTag.OUTSTANDING_FAN_ART -> m.svgIconManager.star
        FanArtTag.LORITTA_CHRISTMAS_2021_EVENT, FanArtTag.LORITTA_CHRISTMAS_2020_EVENT, FanArtTag.LORITTA_CHRISTMAS_2019_EVENT, FanArtTag.LORITTA_CHRISTMAS_2018_EVENT -> m.svgIconManager.santaClaus
        FanArtTag.LORITTA_ANNIVERSARY_2023_EVENT, FanArtTag.LORITTA_ANNIVERSARY_2022_EVENT, FanArtTag.LORITTA_ANNIVERSARY_2021_EVENT, FanArtTag.LORITTA_ANNIVERSARY_2020_EVENT, FanArtTag.LORITTA_ANNIVERSARY_2019_EVENT -> m.svgIconManager.cake
        FanArtTag.LORITTA_JUNINA_PARTY_2021_EVENT -> m.svgIconManager.womanDancing
        FanArtTag.LORITTA_HALLOWEEN_2020_EVENT -> m.svgIconManager.pumpkin
        FanArtTag.LORITTA_SWEATER_2019_EXTRAVAGANZA -> null
        FanArtTag.LORITTA_APRIL_FOOLS_2019_EVENT -> null
        FanArtTag.COMICS -> m.svgIconManager.speechBubble
        FanArtTag.PIXEL_ART -> null
        FanArtTag.LORITTA_WEBSITE_ARTS -> null
        FanArtTag.LORITTA, FanArtTag.PANTUFA, FanArtTag.GABRIELA, FanArtTag.MRPOWERGAMERBR -> null
        FanArtTag.LORITTA_JULY_HOLIDAYS_2019_EVENT -> null
    }
}