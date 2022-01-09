package net.perfectdreams.galleryofdreams.common.data

import kotlinx.serialization.Serializable

@Serializable
sealed class FanArtArtistSocialConnection

@Serializable
data class DiscordSocialConnection(val id: Long) : FanArtArtistSocialConnection()

@Serializable
data class TwitterSocialConnection(val handle: String) : FanArtArtistSocialConnection()

@Serializable
data class DeviantArtSocialConnection(val handle: String) : FanArtArtistSocialConnection()