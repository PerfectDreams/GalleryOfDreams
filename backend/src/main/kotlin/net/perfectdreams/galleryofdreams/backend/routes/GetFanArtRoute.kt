package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.application.*
import io.ktor.html.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.i18nhelper.core.I18nContext

class GetFanArtRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/artists/{artistSlug}/{fanArtSlug}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val cachedRootHTML = withContext(Dispatchers.IO) { m.hackySSR.getOrRenderRootElementPageHTMLForCrawlers(call) }
        call.respondHtml(block = galleryOfDreamsSpaHtml(m, cachedRootHTML))
    }
}