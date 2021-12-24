package net.perfectdreams.galleryofdreams.frontend.utils

import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist

object FanArtUtils {
    fun getArtistAvatarUrl(
        data: GalleryOfDreamsDataWrapper,
        artist: FanArtArtist,
        imageSize: Int
    ): String {
        val latestArtistFanArt = artist.fanArts.sortedByDescending { it.createdAt }.firstOrNull()
            ?: return "https://cdn.discordapp.com/emojis/523176710439567392.png?size=$imageSize"

        val extension = MediaTypeUtils.convertContentTypeToExtension(latestArtistFanArt.preferredMediaType)
        // DSS does not support resizing GIF images (yet)
        if (extension == "gif")
            return "${data.dreamStorageServiceData.url}/${data.dreamStorageServiceData.namespace}/${
                StoragePaths.FanArt(
                    latestArtistFanArt.file
                ).join()
            }.$extension"
        else
            return "${data.dreamStorageServiceData.url}/${data.dreamStorageServiceData.namespace}/${
                StoragePaths.FanArt(
                    latestArtistFanArt.file
                ).join()
            }.$extension?size=$imageSize"
    }
}