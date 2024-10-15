package net.perfectdreams.galleryofdreams.backend

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.galleryofdreams.backend.config.GalleryOfDreamsConfig
import net.perfectdreams.galleryofdreams.backend.utils.LanguageManager
import java.io.File

object GalleryOfDreamsBackendLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Hocon.decodeFromConfig<GalleryOfDreamsConfig>(ConfigFactory.parseString(File("gallery-of-dreams.conf").readText(Charsets.UTF_8)).resolve())

        val languageManager = LanguageManager(
            GalleryOfDreamsBackend::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

        val m = GalleryOfDreamsBackend(config, languageManager)
        m.start()
    }
}