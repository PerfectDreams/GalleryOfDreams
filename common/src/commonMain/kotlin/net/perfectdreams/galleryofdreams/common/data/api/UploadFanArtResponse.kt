package net.perfectdreams.galleryofdreams.common.data.api

import kotlinx.serialization.Serializable
import net.perfectdreams.galleryofdreams.common.data.FanArt

@Serializable
class UploadFanArtResponse(
    val fanArt: FanArt
)