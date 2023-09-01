package net.perfectdreams.galleryofdreams.backend.utils

import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX

object FanArtUtils {
    fun getArtistAvatarUrl(
        dssBaseUrl: String,
        namespace: String,
        artist: FanArtArtistX,
        imageSize: Int
    ): String {
        val fanArtAvatar = artist.fanArtAvatar ?: return "https://cdn.discordapp.com/emojis/523176710439567392.png?size=$imageSize"

        val extension = MediaTypeUtils.convertContentTypeToExtension(fanArtAvatar.preferredMediaType)
        // DSS does not support resizing GIF images (yet)
        if (extension == "gif")
            return "$dssBaseUrl/$namespace/${
                StoragePaths.FanArt(
                    fanArtAvatar.file
                ).join()
            }.$extension"
        else
            return "$dssBaseUrl/$namespace/${
                StoragePaths.FanArt(
                    fanArtAvatar.file
                ).join()
            }.$extension?size=$imageSize"
    }
}