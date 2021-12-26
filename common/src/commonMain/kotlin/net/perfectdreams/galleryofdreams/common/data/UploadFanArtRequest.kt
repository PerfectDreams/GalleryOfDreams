package net.perfectdreams.galleryofdreams.common.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.galleryofdreams.common.FanArtTag

@Serializable
class UploadFanArtRequest(
    val slug: String,
    val title: String? = null,
    val description: String? = null,
    val createdAt: Instant,
    val tags: List<FanArtTag>
)