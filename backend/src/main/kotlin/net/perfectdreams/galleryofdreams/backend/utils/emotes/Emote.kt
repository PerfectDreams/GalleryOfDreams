package net.perfectdreams.galleryofdreams.backend.utils.emotes

sealed class Emote {
    /**
     * The emote name
     */
    abstract val name: String

    abstract val asMention: String

    override fun toString() = asMention
}