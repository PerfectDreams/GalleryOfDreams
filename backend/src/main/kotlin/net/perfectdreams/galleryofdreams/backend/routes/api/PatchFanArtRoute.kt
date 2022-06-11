package net.perfectdreams.galleryofdreams.backend.routes.api

import dev.kord.rest.builder.message.create.allowedMentions
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamstorageservice.data.api.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.UploadImageRequest
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.routes.RequiresAPIAuthenticationRoute
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.AuthorizationToken
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.api.PatchFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtResponse
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.*

class PatchFanArtRoute(m: GalleryOfDreamsBackend) : RequiresAPIAuthenticationRoute(m, "/api/v1/fan-arts/{slug}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, token: AuthorizationToken) {
        val fanArtId = call.parameters.getOrFail("fanArtId")

        val requestBody = call.receiveText()
        val request = Json.decodeFromString<PatchFanArtRequest>(requestBody)

        val r = withContext(Dispatchers.IO) {
            // Patch new tags
            // To do this, we will remove all current tags and reinsert them!
            m.transaction {
                // Get the fan art
                val fanArt = FanArts.select { FanArts.slug eq fanArtId }
                    .limit(1)
                    .firstOrNull() ?: return@transaction false

                // Delete all tags
                FanArtTags.deleteWhere { FanArtTags.id eq fanArt[FanArts.id] }

                // Reinsert them!
                for (tag in request.tags) {
                    FanArtTags.insert {
                        it[FanArtTags.fanArt] = fanArt[FanArts.id]
                        it[FanArtTags.tag] = tag
                    }
                }

                return@transaction true
            }
        }

        if (r) {
            call.respondJson("", HttpStatusCode.OK)
        } else {
            call.respondJson("", HttpStatusCode.NotFound)
        }
    }
}