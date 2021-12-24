package net.perfectdreams.galleryofdreams.common.data

import kotlinx.serialization.Serializable

@Serializable
sealed class FanArtArtistSocialConnection

@Serializable
class DiscordSocialConnection(val id: Long) : FanArtArtistSocialConnection()

@Serializable
class TwitterSocialConnection(val handle: String) : FanArtArtistSocialConnection()

@Serializable
class DeviantArtSocialConnection(val handle: String) : FanArtArtistSocialConnection()