package net.perfectdreams.galleryofdreams.common.data

import kotlinx.serialization.Serializable

@Serializable
data class FanArtArtist(
    val id: Long,
    val slug: String,
    val name: String,
    val fanArts: List<FanArt>,
    val tags: List<String>,
    val socialConnections: List<FanArtArtistSocialConnection>
)

@Serializable
data class FanArtArtistX(
    val id: Long,
    val slug: String,
    val name: String,
    val tags: List<String>,
    val socialConnections: List<FanArtArtistSocialConnection>,
    val fanArtAvatar: FanArt?
)