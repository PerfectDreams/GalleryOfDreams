package net.perfectdreams.galleryofdreams.backend.utils

import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistX

data class FanArtArtistWithFanArt(
    val fanArtArtist: FanArtArtistX,
    val fanArt: FanArt
)