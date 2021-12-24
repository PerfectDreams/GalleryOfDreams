package net.perfectdreams.galleryofdreams.backend.utils

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamstorageservice.data.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.UploadImageRequest
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtistSocialConnections
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistSocialConnection
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import org.apache.commons.codec.binary.Hex
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import pw.forst.exposed.insertOrUpdate
import java.io.File
import java.security.MessageDigest
import java.util.*
import kotlin.system.exitProcess

fun calculateChecksum(array: ByteArray) = MessageDigest.getInstance("SHA-256").digest(array)

suspend fun main() {
    val m = GalleryOfDreamsBackend(
        LanguageManager(
            GalleryOfDreamsBackend::class,
            "en",
            "/languages/"
        )
    )

    println(
        m.dreamStorageServiceClient.getCachedNamespaceOrRetrieve()
    )

    val fileHashes = mutableSetOf<String>()

    var totalFanArts = 0
    var okFanArts = 0
    var duplicateFanArts = 0
    var missingFanArts = 0
    val tagMap = mapOf(
        "christmas-2020" to FanArtTag.LORITTA_CHRISTMAS_2020_EVENT,
        "anniversary-2019" to FanArtTag.LORITTA_ANNIVERSARY_2019_EVENT,
        "april-fools-2019" to FanArtTag.LORITTA_APRIL_FOOLS_2019_EVENT,
        "website-arts" to FanArtTag.LORITTA_WEBSITE_ARTS,
        "sweater-2019" to FanArtTag.LORITTA_SWEATER_2019_EXTRAVAGANZA,
        "comics" to FanArtTag.COMICS,
        "natal-2018" to FanArtTag.LORITTA_CHRISTMAS_2018_EVENT
    )

    // ===[ TAG LIST ]===
    if (false) {
        val tags = mutableSetOf<String>()

        for (oldFanArtInfoFile in File("L:\\CinnamonAssets\\fan_arts_artists").listFiles()) {
            if (oldFanArtInfoFile.extension == "conf") {
                val oldFanArtInfo = Hocon.decodeFromConfig<OldFanArtArtist>(ConfigFactory.parseFile(oldFanArtInfoFile))

                oldFanArtInfo.fanArts.forEach {
                    if (it.tags.isNotEmpty())
                        println(it)
                }
                tags.addAll(oldFanArtInfo.fanArts.flatMap { it.tags })
            }
        }

        println(tags)
        exitProcess(0)
    }

    for (oldFanArtInfoFile in File("L:\\CinnamonAssets\\fan_arts_artists").listFiles()) {
        if (oldFanArtInfoFile.extension == "conf" && (true || oldFanArtInfoFile.nameWithoutExtension == "inksans")) {
            println(oldFanArtInfoFile)
            val oldFanArtInfo = Hocon.decodeFromConfig<OldFanArtArtist>(ConfigFactory.parseFile(oldFanArtInfoFile))

            m.transaction {
                if (FanArtArtists.select { FanArtArtists.slug eq oldFanArtInfo.id }.count() == 0L) {
                    val artistData = FanArtArtists.insertOrUpdate(FanArtArtists.id) {
                        it[FanArtArtists.slug] = oldFanArtInfo.id
                        it[FanArtArtists.name] = oldFanArtInfo.info.name
                    }

                    oldFanArtInfo.networks.forEach { social ->
                        if (social.type == "discord") {
                            FanArtArtistSocialConnections.insert {
                                it[FanArtArtistSocialConnections.artist] = artistData[FanArtArtists.id]
                                it[FanArtArtistSocialConnections.data] =
                                    Json.encodeToString<FanArtArtistSocialConnection>(
                                        DiscordSocialConnection(
                                            social.id.toLong()
                                        )
                                    )
                            }
                        }
                        if (social.type == "twitter") {
                            FanArtArtistSocialConnections.insert {
                                it[FanArtArtistSocialConnections.artist] = artistData[FanArtArtists.id]
                                it[FanArtArtistSocialConnections.data] =
                                    Json.encodeToString<FanArtArtistSocialConnection>(
                                        TwitterSocialConnection(
                                            social.id
                                        )
                                    )
                            }
                        }
                    }

                    for (fanArt in oldFanArtInfo.fanArts) {
                        totalFanArts++

                        val fanArtFile =
                            File("L:\\Pictures\\Loritta\\Fan Arts GalleryOfDreams\\fanarts\\${fanArt.fileName}")

                        if (!fanArtFile.exists()) {
                            println("Fan Art ${fanArt.fileName} does not exist!")
                            missingFanArts++
                        } else {
                            val checksum = calculateChecksum(fanArtFile.readBytes())
                            val hexChecksum = Hex.encodeHexString(checksum)
                            if (fileHashes.contains(hexChecksum)) {
                                println("Fan Art ${fanArt.fileName} is a duplicate!")
                                duplicateFanArts++
                            } else {
                                fileHashes.add(hexChecksum)
                                println("Fan Art ${fanArt.fileName} is ok!")
                                okFanArts++

                                val imageInfo = SimpleImageInfo(fanArtFile)
                                val mediaType = when (imageInfo.mimeType) {
                                    "image/png" -> ContentType.Image.PNG
                                    "image/jpeg" -> ContentType.Image.JPEG
                                    "image/gif" -> ContentType.Image.GIF
                                    else -> error("Unsupported file type! ${fanArtFile.extension} ${imageInfo.mimeType}")
                                }


                                val uploadResult = m.dreamStorageServiceClient.uploadImage(
                                    fanArtFile.readBytes(),
                                    mediaType,
                                    UploadImageRequest(
                                        false
                                    )
                                )

                                val r = m.dreamStorageServiceClient.createImageLink(
                                    CreateImageLinkRequest(
                                        uploadResult.imageId,
                                        "fan-arts",
                                        "%s"
                                    )
                                )

                                val (year, month, day) = fanArt.createdAt.split("-")

                                val createdAt = LocalDate(year.toInt(), month.toInt(), day.toInt())
                                    .atTime(0, 0, 0)
                                    .toInstant(TimeZone.of("America/Sao_Paulo"))

                                val dbFanArt = FanArts.insert {
                                    // TODO: Fix this
                                    it[FanArts.slug] = UUID.randomUUID().toString()
                                    it[FanArts.artist] = artistData[FanArtArtists.id]
                                    it[FanArts.createdAt] = createdAt
                                    it[FanArts.file] = r.file
                                    it[FanArts.preferredMediaType] = mediaType.toString()
                                }

                                for (tag in fanArt.tags) {
                                    println("Tag: $tag")
                                    FanArtTags.insert {
                                        it[FanArtTags.tag] = tagMap[tag]!!
                                        it[FanArtTags.fanArt] = dbFanArt[FanArts.id]
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    println("Result:")
    println("Total Fan Arts: $totalFanArts")
    println("OK Fan Arts: $okFanArts")
    println("Duplicate Fan Arts: $duplicateFanArts")
    println("Missing Fan Arts: $missingFanArts")

    File("L:\\Pictures\\Loritta\\Fan Arts GalleryOfDreams")
        .listFiles()
        .forEach {

        }
}