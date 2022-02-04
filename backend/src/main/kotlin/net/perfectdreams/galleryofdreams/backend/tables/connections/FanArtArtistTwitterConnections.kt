package net.perfectdreams.galleryofdreams.backend.tables.connections

import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FanArtArtistTwitterConnections : LongIdTable() {
    val artist = reference("artist", FanArtArtists, ReferenceOption.CASCADE).index()
    val handle = text("handle").index()
}