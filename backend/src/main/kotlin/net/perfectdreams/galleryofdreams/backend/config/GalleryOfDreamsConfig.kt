package net.perfectdreams.galleryofdreams.backend.config

import kotlinx.serialization.Serializable

@Serializable
data class GalleryOfDreamsConfig(
    val discord: DiscordConfig,
    val etherealGambi: EtherealGambiConfig
) {
    @Serializable
    data class DiscordConfig(
        val token: String
    )

    @Serializable
    data class EtherealGambiConfig(
        val authorizationToken: String,
        val url: String
    )
}