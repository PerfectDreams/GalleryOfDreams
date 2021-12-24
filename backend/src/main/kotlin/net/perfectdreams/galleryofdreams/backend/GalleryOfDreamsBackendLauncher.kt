package net.perfectdreams.galleryofdreams.backend

import net.perfectdreams.galleryofdreams.backend.utils.LanguageManager

object GalleryOfDreamsBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val languageManager = LanguageManager(
            GalleryOfDreamsBackend::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val m = GalleryOfDreamsBackend(languageManager)
        m.start()
    }
}