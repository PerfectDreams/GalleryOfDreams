package net.perfectdreams.galleryofdreams.common.data

import kotlinx.serialization.Serializable

@Serializable
data class GalleryOfDreamsDataResponse(
    val dreamStorageServiceData: DreamStorageServiceData,
    val artists: List<FanArtArtist>
)