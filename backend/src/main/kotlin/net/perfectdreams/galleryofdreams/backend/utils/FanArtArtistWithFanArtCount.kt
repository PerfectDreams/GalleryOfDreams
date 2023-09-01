package net.perfectdreams.galleryofdreams.backend.utils

import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX

data class FanArtArtistWithFanArtCount(
    val fanArtArtist: FanArtArtistX,
    val fanArtCount: Long
)