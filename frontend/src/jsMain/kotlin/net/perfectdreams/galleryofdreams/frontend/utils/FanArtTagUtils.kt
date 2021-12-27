package net.perfectdreams.galleryofdreams.frontend.utils

import net.perfectdreams.galleryofdreams.common.FanArtTag

val FanArtTag.icon: IconManager.Icon?
    get() = when (this) {
        FanArtTag.OUTSTANDING_FAN_ART -> IconManager.star
        FanArtTag.LORITTA_CHRISTMAS_2021_EVENT, FanArtTag.LORITTA_CHRISTMAS_2020_EVENT, FanArtTag.LORITTA_CHRISTMAS_2018_EVENT -> IconManager.santaClaus
        FanArtTag.LORITTA_ANNIVERSARY_2019_EVENT -> IconManager.cake
        FanArtTag.LORITTA_SWEATER_2019_EXTRAVAGANZA -> null
        FanArtTag.LORITTA_APRIL_FOOLS_2019_EVENT -> null
        FanArtTag.COMICS -> IconManager.speechBubble
        FanArtTag.PIXEL_ART -> null
        FanArtTag.LORITTA_WEBSITE_ARTS -> null
        FanArtTag.LORITTA, FanArtTag.PANTUFA, FanArtTag.GABRIELA -> null
    }