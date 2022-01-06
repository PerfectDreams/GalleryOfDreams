package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.ResultSet

class GetFanArtsRoute(private val m: GalleryOfDreamsBackend) : BaseRoute("/api/v1/fan-arts") {
    override suspend fun onRequest(call: ApplicationCall) {
        val namespace = m.dreamStorageServiceClient.getCachedNamespaceOrRetrieve()

        val result = m.transaction {
            // Calculate md5 hash of the elements, used for the ETag (Cache Busting)
            // This also has the advantage of being waaaay faster than the query below, so if the hash matches it will be way faster :3
            // This query seems weird but what it does is:
            // 1. Calculate the sha256 hash of every row in each table that should be cache busted then concat them (that's what the string_agg does)
            // 2. Concat all the individual results into a big string and then calculate the sha256 of that (the COALESCE empty string is used if tbe table is empty)
            var eTagKey: ByteArray? = null
            "select sha256(COALESCE(a.string_agg, '') || COALESCE(b.string_agg, '') || COALESCE(c.string_agg, '') || COALESCE(d.string_agg, '') || COALESCE(e.string_agg, '') || COALESCE(f.string_agg, '')) from (select string_agg(sha256(fanartartists::text::bytea), '') from fanartartists) as a, (select string_agg(sha256(fanarts::text::bytea), '') from fanarts) as b, (select string_agg(sha256(fanarttags::text::bytea), '') from fanarttags) as c, (select string_agg(sha256(fanartartistdiscordconnections::text::bytea), '') from fanartartistdiscordconnections) as d, (select string_agg(sha256(fanartartisttwitterconnections::text::bytea), '') from fanartartisttwitterconnections) as e, (select string_agg(sha256(fanartartistdeviantartconnections::text::bytea), '') from fanartartistdeviantartconnections) as f;".execAndMap { rs ->
                eTagKey = rs.getBytes(1)
            }

            if (eTagKey != null) {
                // Naive Etag implementation: Check if the data hashCode changed or not, if it hasn't, we don't need to send the entire payload again
                // Because all elements within GalleryOfDreamsDataResponse are "data", the hashCode should be consistent if the elements didn't change :)
                if (call.request.header("If-None-Match") == Hex.encodeHexString(eTagKey))
                    return@transaction null
            }

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

            Pair(
                GalleryOfDreamsDataResponse(
                    DreamStorageServiceData(
                        m.dreamStorageServiceClient.baseUrl,
                        namespace
                    ),
                    fanArtArtists
                ),
                eTagKey
            )
        }

        if (result == null) {
            call.respond(HttpStatusCode.NotModified)
            return
        }

        val (data, eTagKey) = result

        val dataAsJson = Json.encodeToString(data)

        call.response.header("ETag", Hex.encodeHexString(eTagKey))

        call.respondJson(dataAsJson)
    }

    private fun <T:Any> String.execAndMap(transform : (ResultSet) -> T) : List<T> {
        val result = arrayListOf<T>()
        TransactionManager.current().exec(this) { rs ->
            while (rs.next()) {
                result += transform(rs)
            }
        }
        return result
    }
}