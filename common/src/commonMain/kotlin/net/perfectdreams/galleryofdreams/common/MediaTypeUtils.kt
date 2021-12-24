package net.perfectdreams.galleryofdreams.common

import io.ktor.http.*

object MediaTypeUtils {
    fun convertContentTypeToExtension(type: String) = convertContentTypeToExtension(ContentType.parse(type))

    fun convertContentTypeToExtension(type: ContentType): String {
        return when (type) {
            ContentType.Image.PNG -> "png"
            ContentType.Image.JPEG -> "jpeg"
            ContentType.Image.GIF -> "gif"
            else -> error("Unsupported Content-Type $type")
        }
    }
}