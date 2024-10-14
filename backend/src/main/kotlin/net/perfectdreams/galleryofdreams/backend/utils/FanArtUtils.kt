package net.perfectdreams.galleryofdreams.backend.utils

import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX

object FanArtUtils {
    fun getArtistAvatarUrl(
        artist: FanArtArtistX,
        imageSize: Int
    ): String {
        val fanArtAvatar = artist.fanArtAvatar ?: return "https://cdn.discordapp.com/emojis/523176710439567392.png?size=$imageSize"

        val fileName = fanArtAvatar.file.substringBeforeLast(".")
        val extension = fanArtAvatar.file.substringAfterLast(".")

        // EtherealGambi does not support resizing GIF images (yet)
        if (extension == "gif")
            return "https://assets.perfectdreams.media/galleryofdreams/fan-arts/${fanArtAvatar.file}"
        else {
            return "https://assets.perfectdreams.media/galleryofdreams/fan-arts/$fileName@${imageSize}w.$extension"
        }
    }
}