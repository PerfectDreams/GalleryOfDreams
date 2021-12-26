package net.perfectdreams.galleryofdreams.backend.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object AuthorizationTokens : LongIdTable() {
    val token = text("token").uniqueIndex() // There can't be duplicate tokens!
    val description = text("description")
}