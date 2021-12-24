package net.perfectdreams.galleryofdreams.common.data

import kotlinx.serialization.Serializable

@Serializable
data class DreamStorageServiceData(
    val url: String,
    val namespace: String
)