package net.perfectdreams.galleryofdreams.common.data.api

import kotlinx.serialization.Serializable
import net.perfectdreams.galleryofdreams.common.FanArtTag

@Serializable
data class PatchFanArtRequest(
    val tags: List<FanArtTag>
)