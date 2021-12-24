package net.perfectdreams.galleryofdreams.frontend.utils

import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.DreamStorageServiceData
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.GalleryOfDreamsDataResponse

class GalleryOfDreamsDataWrapper(private val response: GalleryOfDreamsDataResponse) {
    val dreamStorageServiceData: DreamStorageServiceData
        get() = response.dreamStorageServiceData
    val artists: List<FanArtArtist>
        get() = response.artists
    val fanArts: List<FanArt>
        get() = response.artists.flatMap { it.fanArts }
}