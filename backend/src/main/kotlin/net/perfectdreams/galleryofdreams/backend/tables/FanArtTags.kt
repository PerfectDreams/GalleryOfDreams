package net.perfectdreams.galleryofdreams.backend.tables

import net.perfectdreams.galleryofdreams.backend.utils.exposed.postgresEnumeration
import net.perfectdreams.galleryofdreams.common.FanArtTag
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FanArtTags : LongIdTable() {
    val fanArt = reference("fan_art", FanArts, ReferenceOption.CASCADE).index()
    val tag = postgresEnumeration<FanArtTag>("tag")
}