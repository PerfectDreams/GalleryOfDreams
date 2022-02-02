package net.perfectdreams.galleryofdreams.common.data.api

import kotlinx.serialization.Serializable
import net.perfectdreams.galleryofdreams.common.data.FanArtArtistSocialConnection

@Serializable
class CreateArtistWithFanArtRequest(
    val name: String,
    val slug: String,
    val socialConnections: List<FanArtArtistSocialConnection>,
    val fanArt: UploadFanArtRequest
)