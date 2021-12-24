package net.perfectdreams.galleryofdreams.backend.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Crawler(
    val pattern: String,
    val url: String? = null,
    @SerialName("addition_date")
    val additionDate: String? = null,
    @SerialName("depends_on")
    val dependsOn: List<String>? = null,
    val description: String? = null,
    val instances: List<String>
)