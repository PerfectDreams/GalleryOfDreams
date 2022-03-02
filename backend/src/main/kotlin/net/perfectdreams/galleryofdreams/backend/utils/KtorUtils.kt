package net.perfectdreams.galleryofdreams.backend.utils

import io.ktor.request.*

fun ApplicationRequest.pathWithoutLocale() = "/${call.request.path().split("/").drop(2).joinToString("/")}"