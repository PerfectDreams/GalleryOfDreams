package net.perfectdreams.galleryofdreams.backend.routes.api

import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookMessageBuilder
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
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class PostFanArtRoute(m: GalleryOfDreamsBackend) : RequiresAPIAuthenticationRoute(m, "/api/v1/artists/{artistId}/fan-arts") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, token: AuthorizationToken) {
        val artistId = call.parameters.getOrFail("artistId")
            .toLong()

        val (fanArtArtist, response) = withContext(Dispatchers.IO) {
            val fanArtArtist = m.transaction {
                FanArtArtists.select { FanArtArtists.id eq artistId }
                    .limit(1)
                    .firstOrNull()
            } ?: error("Artist with ID $artistId does not exist!")

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
                    uploadResult.info.imageId,
                    "fan-arts",
                    "%s"
                )
            )

            val (fanArt, tags) = m.transaction {
                val fanArt = FanArts.insert {
                    it[FanArts.slug] = attributes.slug
                    it[FanArts.title] = attributes.title
                    it[FanArts.description] = attributes.description
                    it[FanArts.artist] = fanArtArtist[FanArtArtists.id]
                    it[FanArts.createdAt] = attributes.createdAt
                    it[FanArts.dreamStorageServiceImageId] = uploadResult.info.imageId
                    it[FanArts.file] = r.link.file
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

            Pair(
                fanArtArtist,
                UploadFanArtResponse(
                    FanArt(
                        fanArt[FanArts.id].value,
                        fanArt[FanArts.slug],
                        fanArt[FanArts.title],
                        fanArt[FanArts.description],
                        fanArt[FanArts.createdAt],
                        fanArt[FanArts.dreamStorageServiceImageId],
                        fanArt[FanArts.file],
                        fanArt[FanArts.preferredMediaType],
                        tags.map {
                            it[FanArtTags.tag]
                        },
                    )
                )
            )
        }

        GlobalScope.launch {
            m.webhookClient?.send(
                WebhookMessageBuilder()
                    // No mentions are allowed!
                    .setAllowedMentions(AllowedMentions.none())
                    .setContent("<:gabriela_brush:727259143903248486> **Fan Art adicionada!** <a:lori_lick:957368372025262120> ${m.websiteUrl}/artists/${fanArtArtist[FanArtArtists.slug]}/${response.fanArt.slug}")
                    .build()
            )
        }

        call.respondJson(response)
    }
}