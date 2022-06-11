package net.perfectdreams.galleryofdreams.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.api.CheckFanArtResponse
import net.perfectdreams.galleryofdreams.common.data.api.CreateArtistWithFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.GalleryOfDreamsDataResponse
import net.perfectdreams.galleryofdreams.common.data.api.PatchFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.api.UploadFanArtResponse

class GalleryOfDreamsClient(
    baseUrl: String,
    val token: String?,
    val http: HttpClient
) {
    companion object {
        private const val apiVersion = "v1"

        // To avoid the client crashing due to additional fields that aren't mapped, let's ignore unknown keys
        // This is useful if we want to add new information but we don't want older clients to crash
        private val json = Json {
            ignoreUnknownKeys = true
        }
    }
    val baseUrl = baseUrl.removeSuffix("/") // Remove trailing slash

    suspend fun createArtistWithFanArt(
        data: ByteArray,
        mimeType: ContentType,
        request: CreateArtistWithFanArtRequest
    ): UploadFanArtResponse {
        val parts = formData {
            append("attributes", json.encodeToString(request))

            append(
                "file",
                data,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType.toString())
                    append(HttpHeaders.ContentDisposition, "filename=file") // This needs to be present for it to be recognized as a FileItem!
                }
            )
        }

        val response = http.submitFormWithBinaryData("${baseUrl}/api/$apiVersion/artists", formData = parts) {
            this.method = HttpMethod.Post
            addAuthorizationTokenIfPresent(true)
        }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun uploadFanArt(
        artistId: Long,
        data: ByteArray,
        mimeType: ContentType,
        request: UploadFanArtRequest
    ): UploadFanArtResponse {
        val parts = formData {
            append("attributes", json.encodeToString(request))

            append(
                "file",
                data,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType.toString())
                    append(HttpHeaders.ContentDisposition, "filename=file") // This needs to be present for it to be recognized as a FileItem!
                }
            )
        }

        val response = http.submitFormWithBinaryData("${baseUrl}/api/$apiVersion/artists/$artistId/fan-arts", formData = parts) {
            this.method = HttpMethod.Post
            addAuthorizationTokenIfPresent(true)
        }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun checkFanArt(
        data: ByteArray,
        mimeType: ContentType,
    ): CheckFanArtResponse {
        val parts = formData {
            append(
                "file",
                data,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType.toString())
                    append(HttpHeaders.ContentDisposition, "filename=file") // This needs to be present for it to be recognized as a FileItem!
                }
            )
        }

        val response = http.submitFormWithBinaryData("${baseUrl}/api/$apiVersion/fan-arts/check", formData = parts) {
            this.method = HttpMethod.Post
            addAuthorizationTokenIfPresent(true)
        }

        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun patchFanArt(
        fanArtSlug: String,
        request: PatchFanArtRequest
    ) {
        http.patch("${baseUrl}/api/$apiVersion/fan-arts/$fanArtSlug") {
            addAuthorizationTokenIfPresent(true)
            setBody(
                TextContent(
                    Json.encodeToString(request),
                    ContentType.Application.Json
                )
            )
        }
    }

    // ===[ FAN ARTS ]===
    suspend fun getAllFanArts(): GalleryOfDreamsDataResponse {
        val response = http.get("${baseUrl}/api/$apiVersion/fan-arts") {
            addAuthorizationTokenIfPresent(false)
        }

        return json.decodeFromString(response.bodyAsText())
    }

    // ===[ ARTISTS ]===
    suspend fun getFanArtArtistByDiscordId(discordId: Long): FanArtArtist? {
        val response = http.get("${baseUrl}/api/$apiVersion/social/discord/$discordId") {
            addAuthorizationTokenIfPresent(false)
        }

        if (response.status == HttpStatusCode.NotFound)
            return null

        return json.decodeFromString(response.bodyAsText())
    }

    private fun HttpRequestBuilder.addAuthorizationTokenIfPresent(fatalIfNotPresent: Boolean) {
        val token = token
        if (token == null && fatalIfNotPresent)
            error("This route requires a authorization token, but you didn't provide any in the constructor!")
        if (token != null)
            header("Authorization", token)
    }
}