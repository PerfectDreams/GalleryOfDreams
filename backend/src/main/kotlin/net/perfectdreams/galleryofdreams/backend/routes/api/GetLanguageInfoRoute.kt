package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import org.apache.commons.codec.digest.DigestUtils

class GetLanguageInfoRoute(private val m: GalleryOfDreamsBackend) : BaseRoute("/api/v1/languages/{languageId}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val language = m.languageManager.getLanguageById(call.parameters.getOrFail("languageId"))

        val dataAsJson = Json.encodeToString(language)

        // Naive Etag implementation: Check if the data hashCode changed or not, if it hasn't, we don't need to send the entire payload again
        val eTagKey = DigestUtils.sha256Hex(dataAsJson)

        if (call.request.header("If-None-Match") == eTagKey) {
            call.respond(HttpStatusCode.NotModified)
            return
        }

        call.response.header("ETag", eTagKey)

        call.respondJson(dataAsJson)
    }
}