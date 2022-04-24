package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.InputType
import kotlinx.html.meta
import kotlinx.serialization.json.JsonNull.content
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists.name
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags.fanArt
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.FanArts.preferredMediaType
import net.perfectdreams.galleryofdreams.backend.utils.pathWithoutLocale
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class GetFanArtRoute(m: GalleryOfDreamsBackend) : LocalizedRoute(m, "/artists/{artistSlug}/{fanArtSlug}") {
    override suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext) {
        val cachedRootHTML = withContext(Dispatchers.IO) { m.hackySSR.getOrRenderRootElementPageHTMLForCrawlers(call, i18nContext) }
        val fanArtData = m.transaction {
            FanArts.innerJoin(FanArtArtists)
                .select { FanArtArtists.slug eq call.parameters.getOrFail("artistSlug") and (FanArts.slug eq call.parameters.getOrFail("fanArtSlug")) }
                .firstOrNull()
        }
        val namespace = m.dreamStorageServiceClient.getCachedNamespaceOrRetrieve()

        if (fanArtData != null) {
            call.respondHtml(
                block = galleryOfDreamsSpaHtml(
                    m,
                    i18nContext,
                    i18nContext.get(I18nKeysData.WebsiteTitle),
                    call.request.pathWithoutLocale(),
                    {
                        meta(content = (fanArtData[FanArts.title] ?: i18nContext.get(I18nKeysData.FanArtBy(fanArtData[FanArtArtists.name])))) {
                            attributes["property"] = "og:title"
                        }
                        meta(content = i18nContext.get(I18nKeysData.WebsiteTitle)) {
                            attributes["property"] = "og:site_name"
                        }
                        meta(content = m.dreamStorageServiceClient.baseUrl + "/${namespace}/${StoragePaths.FanArt("${fanArtData[FanArts.file]}.${MediaTypeUtils.convertContentTypeToExtension(fanArtData[FanArts.preferredMediaType])}").join()}") {
                            attributes["property"] = "og:image"
                        }
                        meta(name = "twitter:card", content = "summary_large_image")
                    },
                    cachedRootHTML
                )
            )
        }
    }
}