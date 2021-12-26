package net.perfectdreams.galleryofdreams.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.GalleryOfDreamsDataResponse
import net.perfectdreams.galleryofdreams.common.data.UploadFanArtRequest
import net.perfectdreams.galleryofdreams.common.data.UploadFanArtResponse

class GalleryOfDreamsClient(
    baseUrl: String,
    val token: String?,
    val http: HttpClient
) {
    companion object {
        private const val apiVersion = "v1"
    }
    val baseUrl = baseUrl.removeSuffix("/") // Remove trailing slash

    suspend fun uploadImage(
        artistId: Long,
        data: ByteArray,
        mimeType: ContentType,
        request: UploadFanArtRequest
    ): UploadFanArtResponse {
        val parts = formData {
            append("attributes", Json.encodeToString(request))

            append(
                "file",
                data,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType.toString())
                    append(HttpHeaders.ContentDisposition, "filename=file") // This needs to be present for it to be recognized as a FileItem!
                }
            )
        }

        val response = http.submitFormWithBinaryData<HttpResponse>("${baseUrl}/api/$apiVersion/artists/$artistId/fan-arts", formData = parts) {
            this.method = HttpMethod.Post
            addAuthorizationTokenIfPresent(true)
        }

        return Json.decodeFromString(response.readText())
    }

    // ===[ FAN ARTS ]===
    suspend fun getAllFanArts(): GalleryOfDreamsDataResponse {
        val response = http.get<HttpResponse>("${baseUrl}/api/$apiVersion/fan-arts") {
            addAuthorizationTokenIfPresent(false)
        }

        return Json.decodeFromString(response.readText())
    }

    // ===[ ARTISTS ]===
    suspend fun getFanArtArtistByDiscordId(discordId: Long): FanArtArtist? {
        val response = http.get<HttpResponse>("${baseUrl}/api/$apiVersion/social/discord/$discordId") {
            addAuthorizationTokenIfPresent(false)
        }

        if (response.status == HttpStatusCode.NotFound)
            return null

        return Json.decodeFromString(response.readText())
    }

    private fun HttpRequestBuilder.addAuthorizationTokenIfPresent(fatalIfNotPresent: Boolean) {
        val token = token
        if (token == null && fatalIfNotPresent)
            error("This route requires a authorization token, but you didn't provide any in the constructor!")
        if (token != null)
            header("Authorization", token)
    }
}