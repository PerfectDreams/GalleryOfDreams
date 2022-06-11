package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.routes.RequiresAPIAuthenticationRoute
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.AuthorizationToken
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.data.api.PatchFanArtRequest
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class PatchFanArtRoute(m: GalleryOfDreamsBackend) : RequiresAPIAuthenticationRoute(m, "/api/v1/fan-arts/{fanArtSlug}") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, token: AuthorizationToken) {
        val fanArtSlug = call.parameters.getOrFail("fanArtSlug")

        val requestBody = call.receiveText()
        val request = Json.decodeFromString<PatchFanArtRequest>(requestBody)

        val r = withContext(Dispatchers.IO) {
            // Patch new tags
            // To do this, we will remove all current tags and reinsert them!
            m.transaction {
                // Get the fan art
                val fanArt = FanArts.select { FanArts.slug eq fanArtSlug }
                    .limit(1)
                    .firstOrNull() ?: return@transaction false

                // Delete all tags
                FanArtTags.deleteWhere { FanArtTags.fanArt eq fanArt[FanArts.id] }

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