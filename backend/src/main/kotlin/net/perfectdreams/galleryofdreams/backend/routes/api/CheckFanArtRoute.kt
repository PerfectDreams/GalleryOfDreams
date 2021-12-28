package net.perfectdreams.galleryofdreams.backend.routes.api

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.dreamstorageservice.data.api.CheckImageRequest
import net.perfectdreams.dreamstorageservice.data.api.ImageDoesNotExistResponse
import net.perfectdreams.dreamstorageservice.data.api.ImageExistsResponse
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.routes.RequiresAPIAuthenticationRoute
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.utils.AuthorizationToken
import net.perfectdreams.galleryofdreams.backend.utils.exposed.respondJson
import net.perfectdreams.galleryofdreams.common.data.api.CheckFanArtResponse
import net.perfectdreams.galleryofdreams.common.data.api.FanArtDoesNotExistResponse
import net.perfectdreams.galleryofdreams.common.data.api.FanArtExistsResponse
import org.jetbrains.exposed.sql.select

class CheckFanArtRoute(m: GalleryOfDreamsBackend) : RequiresAPIAuthenticationRoute(m, "/api/v1/fan-arts/check") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, token: AuthorizationToken) {
        val response = withContext(Dispatchers.IO) {
            // Receive the uploaded file
            val multipart = call.receiveMultipart()
            val parts = multipart.readAllParts()
            val filePart = parts.first { it.name == "file" } as PartData.FileItem

            val fileToBeStored = filePart.streamProvider.invoke().readAllBytes()
            val contentType = filePart.contentType ?: error("Missing Content-Type!")

            val checkResult = m.dreamStorageServiceClient.checkImage(
                fileToBeStored,
                contentType,
                CheckImageRequest(false)
            )

            when (checkResult) {
                is ImageExistsResponse -> {
                    // If the image exists, check if it is in our database
                    val fanArtCount = m.transaction {
                        FanArts.select {
                            FanArts.dreamStorageServiceImageId eq checkResult.imageId
                        }.count()
                    }

                    if (fanArtCount != 0L)
                        FanArtExistsResponse()
                    else
                        FanArtDoesNotExistResponse()
                }
                is ImageDoesNotExistResponse -> {
                    FanArtDoesNotExistResponse()
                }
            }
        }

        call.respondJson<CheckFanArtResponse>(response)
    }
}