package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.application.*
import io.ktor.request.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeys
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class LocalizedRoute(val m: GalleryOfDreamsBackend, val originalPath: String) : BaseRoute("/{localeId}$originalPath") {
    override suspend fun onRequest(call: ApplicationCall) {
        val localeIdFromPath = call.parameters["localeId"]

        // Pegar a locale da URL e, caso não existir, faça fallback para o padrão BR
        val locale = m.languageManager.languageContexts.values.firstOrNull { it.language.textBundle.strings.get(I18nKeys.WebsiteLocaleIdPath.key) == localeIdFromPath }

        if (locale != null) {
            return onLocalizedRequest(
                call,
                locale
            )
        }
    }

    abstract suspend fun onLocalizedRequest(call: ApplicationCall, i18nContext: I18nContext)

    fun getPathWithoutLocale(call: ApplicationCall) = call.request.path().split("/").drop(2).joinToString("/")
}