package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.utils.pathWithoutLocale
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext

class GetHomeRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val cachedRootHTML = withContext(Dispatchers.IO) { m.hackySSR.getOrRenderRootElementPageHTMLForCrawlers(call, i18nContext) }
        call.respondHtml(
            block = galleryOfDreamsSpaHtml(
                m,
                i18nContext,
                i18nContext.get(I18nKeysData.WebsiteTitle),
                call.request.pathWithoutLocale(),
                {},
                cachedRootHTML
            )
        )
    }
}