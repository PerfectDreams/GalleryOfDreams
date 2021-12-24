package net.perfectdreams.galleryofdreams.backend.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FanArts : LongIdTable() {
    val slug = text("slug").index()
    val artist = reference("artist", FanArtArtists).index()
    val title = text("title").nullable()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")
    val file = text("file")
    val preferredMediaType = text("preferred_media_type")
}