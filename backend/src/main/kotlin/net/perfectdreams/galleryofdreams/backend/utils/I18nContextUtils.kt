package net.perfectdreams.galleryofdreams.backend.utils

import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext

val I18nContext.websiteLocaleIdPath: String
        get() = this.get(I18nKeysData.WebsiteLocaleIdPath)
