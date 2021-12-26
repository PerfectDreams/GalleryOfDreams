package net.perfectdreams.galleryofdreams.backend.utils

import org.jetbrains.exposed.dao.id.EntityID

data class AuthorizationToken(
    val id: EntityID<Long>,
    val token: String,
    val description: String
)