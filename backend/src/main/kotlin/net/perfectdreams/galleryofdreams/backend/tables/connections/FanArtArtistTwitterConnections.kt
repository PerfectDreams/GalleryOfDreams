package net.perfectdreams.galleryofdreams.backend.tables.connections

import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import org.jetbrains.exposed.dao.id.LongIdTable

object FanArtArtistTwitterConnections : LongIdTable() {
    val artist = reference("artist", FanArtArtists).index()
    val handle = text("handle").index()
}