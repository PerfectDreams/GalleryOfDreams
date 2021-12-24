package net.perfectdreams.galleryofdreams.backend.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OldFanArtArtist(
    val id: String,
    val info: Info,
    @SerialName("fan-arts")
    val fanArts: List<FanArt>,
    val networks: List<SocialNetwork> = listOf()
) {
    @Serializable
    data class Info(
        val name: String
    )

    @Serializable
    data class FanArt(
        @SerialName("file-name")
        val fileName: String,
        @SerialName("created-at")
        val createdAt: String,
        val tags: List<String>
    )

    @Serializable
    data class SocialNetwork(
        val type: String,
        val id: String
    )
}