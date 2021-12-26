package net.perfectdreams.galleryofdreams.common

import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

enum class FanArtTag(
    val title: StringI18nData
) {
    OUTSTANDING_FAN_ART(I18nKeysData.FanArtTags.OutstandingFanArt),
    LORITTA_CHRISTMAS_2021_EVENT(I18nKeysData.FanArtTags.LorittaChristmas2021Event),
    LORITTA_CHRISTMAS_2020_EVENT(I18nKeysData.FanArtTags.LorittaChristmas2020Event),
    LORITTA_ANNIVERSARY_2019_EVENT(I18nKeysData.FanArtTags.LorittaAnniversary2019Event),
    LORITTA_SWEATER_2019_EXTRAVAGANZA(I18nKeysData.FanArtTags.LorittaSweaterExtravaganza2019),
    LORITTA_APRIL_FOOLS_2019_EVENT(I18nKeysData.FanArtTags.LorittaAprilFools2019Event),
    LORITTA_CHRISTMAS_2018_EVENT(I18nKeysData.FanArtTags.LorittaChristmas2018Event),
    COMICS(I18nKeysData.FanArtTags.Comics),
    LORITTA_WEBSITE_ARTS(I18nKeysData.FanArtTags.LorittaWebsiteArts),
    LORITTA(I18nKeysData.FanArtTags.Loritta),
    PANTUFA(I18nKeysData.FanArtTags.Pantufa),
    GABRIELA(I18nKeysData.FanArtTags.Gabriela)
}