package net.perfectdreams.galleryofdreams.backend.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FanArtArtists : LongIdTable() {
    val slug = text("slug").index()
    val name = text("name")
}