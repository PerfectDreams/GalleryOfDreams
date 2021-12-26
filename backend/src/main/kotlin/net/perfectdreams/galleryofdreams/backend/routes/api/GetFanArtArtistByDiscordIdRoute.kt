package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.*
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
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.select

class GetFanArtArtistByDiscordIdRoute(private val m: GalleryOfDreamsBackend) : BaseRoute("/api/v1/social/discord/{discordId}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val discordId = call.parameters.getOrFail("discordId").toLong()

        val artistData = m.transaction {
            val discordConnectionArtistId = FanArtArtistDiscordConnections.select { FanArtArtistDiscordConnections.artist eq discordId }
                .limit(1)
                .firstOrNull() ?: return@transaction null

            val fanArtArtist = FanArtArtists
                .select { FanArtArtists.id eq discordConnectionArtistId[FanArtArtistDiscordConnections.artist] }
                .limit(1)
                .first()

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

        if (artistData == null) {
            call.respondJson("{}", HttpStatusCode.NotFound)
            return
        }

        call.respondJson(artistData)
    }
}