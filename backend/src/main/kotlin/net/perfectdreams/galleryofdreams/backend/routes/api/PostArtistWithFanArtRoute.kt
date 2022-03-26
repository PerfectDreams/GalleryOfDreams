package net.perfectdreams.galleryofdreams.backend.routes.api

import dev.kord.rest.builder.message.create.allowedMentions
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
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
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.AuthorizationToken
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.data.DeviantArtSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.galleryofdreams.common.data.api.CreateArtistWithFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

class PostArtistWithFanArtRoute(m: GalleryOfDreamsBackend) : RequiresAPIAuthenticationRoute(m, "/api/v1/artists") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, token: AuthorizationToken) {
        val (fanArtArtist, response) = withContext(Dispatchers.IO) {
            // Receive the uploaded file
            val multipart = call.receiveMultipart()
            val parts = multipart.readAllParts()
            val filePart = parts.first { it.name == "file" } as PartData.FileItem
            val attributesPart = parts.first { it.name == "attributes" } as PartData.FormItem

            val attributes = Json.decodeFromString<CreateArtistWithFanArtRequest>(attributesPart.value)

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

            val (fanArtArtist, fanArt, tags) = m.transaction {
                val fanArtArtist = FanArtArtists.insert {
                    it[FanArtArtists.name] = attributes.name
                    it[FanArtArtists.slug] = attributes.slug
                }

                attributes.socialConnections.forEach { socialConnection ->
                    when (socialConnection) {
                        is DeviantArtSocialConnection -> {
                            FanArtArtistDeviantArtConnections.insert {
                                it[FanArtArtistDeviantArtConnections.artist] = fanArtArtist[FanArtArtists.id]
                                it[FanArtArtistDeviantArtConnections.handle] = socialConnection.handle
                            }
                        }
                        is DiscordSocialConnection -> {
                            FanArtArtistDiscordConnections.insert {
                                it[FanArtArtistDiscordConnections.artist] = fanArtArtist[FanArtArtists.id]
                                it[FanArtArtistDiscordConnections.discordId] = socialConnection.id
                            }
                        }
                        is TwitterSocialConnection -> {
                            FanArtArtistTwitterConnections.insert {
                                it[FanArtArtistTwitterConnections.artist] = fanArtArtist[FanArtArtists.id]
                                it[FanArtArtistTwitterConnections.handle] = socialConnection.handle
                            }
                        }
                    }
                }

                val fanArtData = attributes.fanArt

                val fanArt = FanArts.insert {
                    it[FanArts.slug] = fanArtData.slug
                    it[FanArts.title] = fanArtData.title
                    it[FanArts.description] = fanArtData.description
                    it[FanArts.artist] = fanArtArtist[FanArtArtists.id]
                    it[FanArts.createdAt] = fanArtData.createdAt
                    it[FanArts.dreamStorageServiceImageId] = uploadResult.info.imageId
                    it[FanArts.file] = r.link.file
                    it[FanArts.preferredMediaType] = contentType.toString()
                }

                val tags = fanArtData.tags.map { tag ->
                    FanArtTags.insert {
                        it[FanArtTags.fanArt] = fanArt[FanArts.id]
                        it[FanArtTags.tag] = tag
                    }
                }

                Triple(fanArtArtist, fanArt, tags)
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

        // If a new fan art was added, we will purge all the prerendered HTML from our cache!
        m.hackySSR.pageCache.clear()
        m.hackySSR.languageBrowsers.forEach { (_, value) ->
            value.invalidateBrowser()
        }
        m.hackySSR.languageBrowsers.clear()

        GlobalScope.launch {
            m.webhookClient?.executeWebhook {
                // No mentions are allowed!
                allowedMentions {}
                content =
                    "<:gabriela_brush:727259143903248486> **Artista e Fan Art adicionados!** <a:lori_lick:957368372025262120> ${m.websiteUrl}/artists/${fanArtArtist[FanArtArtists.slug]}/${response.fanArt.slug}"
            }
        }

        call.respondJson(response)
    }
}