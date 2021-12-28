package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.application.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.data.DeviantArtSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DreamStorageServiceData
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.api.GalleryOfDreamsDataResponse
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class GetFanArtsRoute(private val m: GalleryOfDreamsBackend) : BaseRoute("/api/v1/fan-arts") {
    override suspend fun onRequest(call: ApplicationCall) {
        val namespace = m.dreamStorageServiceClient.getCachedNamespaceOrRetrieve()

        call.respondJson(
            m.transaction {
                val fanArtArtists = FanArtArtists.selectAll().map { fanArtArtist ->
                    val discordSocialConnections = FanArtArtistDiscordConnections.select {
                        FanArtArtistDiscordConnections.artist eq fanArtArtist[FanArtArtists.id]
                    }
                    val twitterSocialConnections = FanArtArtistTwitterConnections.select {
                        FanArtArtistTwitterConnections.artist eq fanArtArtist[FanArtArtists.id]
                    }
                    val deviantArtSocialConnections = FanArtArtistDeviantArtConnections.select {
                        FanArtArtistDeviantArtConnections.artist eq fanArtArtist[FanArtArtists.id]
                    }

                    val fanArts = FanArts.select {
                        FanArts.artist eq fanArtArtist[FanArtArtists.id]
                    }.map {
                        FanArt(
                            it[FanArts.id].value,
                            it[FanArts.slug],
                            it[FanArts.title],
                            it[FanArts.description],
                            it[FanArts.createdAt],
                            it[FanArts.dreamStorageServiceImageId],
                            it[FanArts.file],
                            it[FanArts.preferredMediaType],
                            FanArtTags.select {
                                FanArtTags.fanArt eq it[FanArts.id]
                            }.map {
                                it[FanArtTags.tag]
                            },
                        )
                    }

                    FanArtArtist(
                        fanArtArtist[FanArtArtists.id].value,
                        fanArtArtist[FanArtArtists.slug],
                        fanArtArtist[FanArtArtists.name],
                        fanArts,
                        listOf(),
                        discordSocialConnections.map {
                            DiscordSocialConnection(it[FanArtArtistDiscordConnections.discordId])
                        } + twitterSocialConnections.map {
                            TwitterSocialConnection(it[FanArtArtistTwitterConnections.handle])
                        } + deviantArtSocialConnections.map {
                            DeviantArtSocialConnection(it[FanArtArtistDeviantArtConnections.handle])
                        }
                    )
                }

                GalleryOfDreamsDataResponse(
                    DreamStorageServiceData(
                        m.dreamStorageServiceClient.baseUrl,
                        namespace
                    ),
                    fanArtArtists
                )
            }
        )
    }
}