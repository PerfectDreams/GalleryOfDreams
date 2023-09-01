package net.perfectdreams.galleryofdreams.backend.utils

import io.ktor.server.application.*
import io.ktor.server.request.*

val ApplicationCall.htmxElementTarget: String?
    get() = this.request.header("HX-Target")