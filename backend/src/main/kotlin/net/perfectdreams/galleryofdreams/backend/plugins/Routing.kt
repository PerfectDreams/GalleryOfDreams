package net.perfectdreams.galleryofdreams.backend.plugins

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.routes.LocalizedRoute
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.sequins.ktor.BaseRoute
import java.util.*

fun Application.configureRouting(m: GalleryOfDreamsBackend, routes: List<BaseRoute>) {
    routing {
        for (route in routes) {
            if (route is LocalizedRoute) {
                val originalPath = route.originalPath

                get(originalPath) {
                    val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
                    val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
                    var localeId = "en-us"
                    for (range in ranges) {
                        localeId = range.range.toLowerCase()
                        if (localeId == "pt-br" || localeId == "pt") {
                            localeId = "pt"
                        }
                        if (localeId == "en") {
                            localeId = "en"
                        }
                    }

                    val locale = m.languageManager.getI18nContextById(localeId)

                    call.respondRedirect("/${locale.get(I18nKeysData.WebsiteLocaleIdPath)}${call.request.uri}")
                    return@get
                }
            }

            route.register(this)
        }
    }
}