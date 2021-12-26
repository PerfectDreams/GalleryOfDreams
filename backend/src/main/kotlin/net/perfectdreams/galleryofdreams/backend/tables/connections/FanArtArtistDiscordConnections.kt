package net.perfectdreams.galleryofdreams.backend.tables.connections

import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import org.jetbrains.exposed.dao.id.LongIdTable

object FanArtArtistDiscordConnections : LongIdTable() {
    val artist = reference("artist", FanArtArtists).index()
    val discordId = long("discord_id").index()
}