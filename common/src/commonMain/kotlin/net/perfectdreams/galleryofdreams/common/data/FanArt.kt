package net.perfectdreams.galleryofdreams.common.data

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import net.perfectdreams.galleryofdreams.common.FanArtTag

@Serializable
data class FanArt(
    val id: Long,
    val slug: String,
    val title: String?,
    val description: String?,
    val createdAt: Instant,
    val dreamStorageServiceImageId: Long,
    val file: String,
    val preferredMediaType: String,
    val tags: List<FanArtTag>
)