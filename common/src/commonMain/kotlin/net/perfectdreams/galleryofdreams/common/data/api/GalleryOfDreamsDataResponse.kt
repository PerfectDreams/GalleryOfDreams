package net.perfectdreams.galleryofdreams.common.data.api

import kotlinx.serialization.Serializable
import net.perfectdreams.galleryofdreams.common.data.DreamStorageServiceData
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist

@Serializable
data class GalleryOfDreamsDataResponse(
    val dreamStorageServiceData: DreamStorageServiceData,
    val artists: List<FanArtArtist>
)