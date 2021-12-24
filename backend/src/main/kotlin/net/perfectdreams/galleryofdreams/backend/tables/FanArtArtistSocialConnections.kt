package net.perfectdreams.galleryofdreams.backend.tables

import net.perfectdreams.galleryofdreams.backend.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object FanArtArtistSocialConnections : LongIdTable() {
    val artist = reference("artist", FanArtArtists)
    val data = jsonb("data")
}