package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.application.*
import io.ktor.html.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.title
import kotlinx.html.unsafe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtistSocialConnections
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.FanArts.title
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.data.DreamStorageServiceData
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.GalleryOfDreamsDataResponse
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class GetFanArtArtistRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/artists/{artistSlug}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val cachedRootHTML = withContext(Dispatchers.IO) { m.hackySSR.getOrRenderRootElementPageHTMLForCrawlers(call) }
        call.respondHtml(block = galleryOfDreamsSpaHtml(m, cachedRootHTML))
    }
}