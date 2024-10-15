package net.perfectdreams.galleryofdreams.backend.utils.emotes

class UnicodeEmote(override val name: String) : Emote() {
    override val asMention: String
        get() = name

    override fun toString() = asMention
}