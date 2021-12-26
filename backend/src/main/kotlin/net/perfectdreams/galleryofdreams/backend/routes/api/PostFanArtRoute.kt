package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.http.contentType
import io.ktor.request.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamstorageservice.data.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.UploadImageRequest
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.routes.RequiresAPIAuthenticationRoute
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.FanArts.createdAt
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.AuthorizationToken
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.DeviantArtSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.galleryofdreams.common.data.UploadFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.UploadFanArtResponse
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.*

class PostFanArtRoute(m: GalleryOfDreamsBackend) : RequiresAPIAuthenticationRoute(m, "/api/v1/artists/{artistId}/fan-arts") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, token: AuthorizationToken) {
        val artistId = call.parameters.getOrFail("artistId")
            .toLong()

        val response = withContext(Dispatchers.IO) {
            // Receive the uploaded file
            val multipart = call.receiveMultipart()
            val parts = multipart.readAllParts()
            val filePart = parts.first { it.name == "file" } as PartData.FileItem
            val attributesPart = parts.first { it.name == "attributes" } as PartData.FormItem

            val attributes = Json.decodeFromString<UploadFanArtRequest>(attributesPart.value)

            val fileToBeStored = filePart.streamProvider.invoke().readAllBytes()
            val contentType = filePart.contentType ?: error("Missing Content-Type!")

            val uploadResult = m.dreamStorageServiceClient.uploadImage(
                fileToBeStored,
                contentType,
                UploadImageRequest(false)
            )

            val r = m.dreamStorageServiceClient.createImageLink(
                CreateImageLinkRequest(
                    uploadResult.imageId,
                    "fan-arts",
                    "%s"
                )
            )

            val (fanArt, tags) = m.transaction {
                val fanArt = FanArts.insert {
                    it[FanArts.slug] = attributes.slug
                    it[FanArts.title] = attributes.title
                    it[FanArts.description] = attributes.description
                    it[FanArts.artist] = artistId
                    it[FanArts.createdAt] = attributes.createdAt
                    it[FanArts.file] = r.file
                    it[FanArts.preferredMediaType] = contentType.toString()
                }

                val tags = attributes.tags.map { tag ->
                    FanArtTags.insert {
                        it[FanArtTags.fanArt] = fanArt[FanArts.id]
                        it[FanArtTags.tag] = tag
                    }
                }

                Pair(fanArt, tags)
            }

            UploadFanArtResponse(
                FanArt(
                    fanArt[FanArts.id].value,
                    fanArt[FanArts.slug],
                    fanArt[FanArts.title],
                    fanArt[FanArts.description],
                    fanArt[FanArts.createdAt],
                    fanArt[FanArts.file],
                    fanArt[FanArts.preferredMediaType],
                    tags.map {
                        it[FanArtTags.tag]
                    },
                )
            )
        }

        call.respondJson(response)
    }
}