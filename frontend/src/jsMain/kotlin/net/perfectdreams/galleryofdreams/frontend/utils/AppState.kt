package net.perfectdreams.galleryofdreams.frontend.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.request.*
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.galleryofdreams.common.data.GalleryOfDreamsDataResponse
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.formatters.IntlMFFormatter

class AppState(private val m: GalleryOfDreamsFrontend)  {
    var isSidebarOpen by mutableStateOf(false)
    var fanArtArtistsSortOrder by mutableStateOf(FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING)
    var fanArtArtistsNameFilter by mutableStateOf("")
    var galleryOfDreamsDataWrapper by mutableStateOf<State<GalleryOfDreamsDataWrapper>>(State.Loading())
    var i18nContext by mutableStateOf<State<I18nContext>>(State.Loading())

    fun loadData() {
        val pathLocale = window.location.pathname
            .split("/")
            .drop(1)
            .take(1)
            .first()

        val host = window.location.protocol + "//" + window.location.host

        val jobs = listOf(
            GlobalScope.async {
                // For some reason using the GalleryOfDreamsClient throws a weird exception in Ktor, probably related to DCE
                // So let's just... not use that I guess
                val response = Json.decodeFromString<GalleryOfDreamsDataResponse>(m.http.get<String>("$host/api/v1/fan-arts"))
                this@AppState.galleryOfDreamsDataWrapper = State.Success(GalleryOfDreamsDataWrapper(response))
            },
            GlobalScope.async {
                val result = m.http.get<String>("$host/api/v1/languages/$pathLocale") {}

                val i18nContext = I18nContext(
                    IntlMFFormatter(),
                    Json.decodeFromString(result)
                )

                this@AppState.i18nContext = State.Success(i18nContext)
            }
        )

        GlobalScope.launch {
            jobs.awaitAll()
        }
    }
}