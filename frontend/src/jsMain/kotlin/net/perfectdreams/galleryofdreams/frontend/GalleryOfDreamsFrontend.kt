package net.perfectdreams.galleryofdreams.frontend

import io.ktor.client.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.frontend.components.FanArtOverview
import net.perfectdreams.galleryofdreams.frontend.components.FanArtsArtistOverview
import net.perfectdreams.galleryofdreams.frontend.components.FanArtsOverview
import net.perfectdreams.galleryofdreams.frontend.components.HomeOverview
import net.perfectdreams.galleryofdreams.frontend.components.LeftSidebar
import net.perfectdreams.galleryofdreams.frontend.components.RightSidebar
import net.perfectdreams.galleryofdreams.frontend.screen.Screen
import net.perfectdreams.galleryofdreams.frontend.utils.AppState
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.galleryofdreams.frontend.utils.RoutingManager
import net.perfectdreams.galleryofdreams.frontend.utils.State
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.url.URL

class GalleryOfDreamsFrontend {
    val root by lazy { document.getElementById("root") as HTMLDivElement? }
    val spaLoadingWrapper by lazy { document.getElementById("spa-loading-wrapper") as HTMLDivElement? }
    val appState = AppState(this)
    val routingManager = RoutingManager(this)
    val http = HttpClient {
        expectSuccess = false
    }

    fun start() {
        appState.loadData()

        document.addEventListener("DOMContentLoaded", {
            renderComposable(rootElementId = "root") {
                val dataWrapper = appState.galleryOfDreamsDataWrapper
                val i18nContext = appState.i18nContext

                if (dataWrapper is State.Success && i18nContext is State.Success) {
                    // Fade out the single page application loading wrapper...
                    spaLoadingWrapper?.addClass("loaded")

                    if (routingManager.screenState == null) {
                        switchToProperScreenBasedOnPath(dataWrapper.value, i18nContext.value, window.location.pathname + window.location.search)
                        window.onpopstate = {
                            switchToProperScreenBasedOnPath(dataWrapper.value, i18nContext.value, it.state as String)
                        }
                    }

                    Div(attrs = { id("wrapper") }) {
                        LeftSidebar(this@GalleryOfDreamsFrontend, dataWrapper.value, i18nContext.value)

                        RightSidebar {
                            when (val screen = routingManager.screenState) {
                                is Screen.HomeOverview -> HomeOverview(
                                    this@GalleryOfDreamsFrontend,
                                    screen,
                                    dataWrapper.value,
                                    i18nContext.value
                                )
                                is Screen.FanArtsOverview -> FanArtsOverview(
                                    this@GalleryOfDreamsFrontend,
                                    screen,
                                    dataWrapper.value,
                                    i18nContext.value
                                )
                                is Screen.FanArtsArtistOverview -> FanArtsArtistOverview(
                                    this@GalleryOfDreamsFrontend,
                                    screen,
                                    dataWrapper.value,
                                    i18nContext.value
                                )
                                is Screen.FanArtOverview -> FanArtOverview(
                                    this@GalleryOfDreamsFrontend,
                                    screen,
                                    dataWrapper.value,
                                    i18nContext.value
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
        })
    }

    internal fun switchToProperScreenBasedOnPath(
        data: GalleryOfDreamsDataWrapper,
        i18nContext: I18nContext,
        path: String
    ) {
        val url = URL("http://127.0.0.1$path")

        val pathWithoutLocale = url.pathname
            .split("/")
            .drop(2)
            .joinToString("/")
            .let { "/$it" }

        val queryParams = url.searchParams

        if (pathWithoutLocale == "/") {
            routingManager.switchToHomeOverview(i18nContext)
        } else if (pathWithoutLocale.startsWith("/fan-arts")) {
            routingManager.switchToFanArtsOverview(
                i18nContext,
                (queryParams.get("page")?.toIntOrNull()?.minus(1)) ?: 0,
                queryParams.get("sort")?.let { FanArtSortOrder.valueOf(it) } ?: FanArtSortOrder.DATE_DESCENDING,
                queryParams.getAll("tags").map { FanArtTag.valueOf(it) }
            )
        } else if (pathWithoutLocale.startsWith("/artists")) {
            val artistFanArtPage = Regex("/artists/([A-z0-9-_]+)/([A-z0-9-_]+)")
                .matchEntire(pathWithoutLocale)
            val artistPage = Regex("/artists/([A-z0-9-_]+)")
                .matchEntire(pathWithoutLocale)

            if (artistFanArtPage != null) {
                val artist = data.artists.first { it.slug == artistFanArtPage.groupValues[1] }
                routingManager.switchToFanArtOverview(
                    i18nContext,
                    artist,
                    artist.fanArts.first { it.slug == artistFanArtPage.groupValues[2] }
                )
            } else if (artistPage != null) {
                routingManager.switchToArtistFanArtsOverview(
                    i18nContext,
                    data.artists.first { it.slug == artistPage.groupValues[1] },
                    (queryParams.get("page")?.toIntOrNull()?.minus(1)) ?: 0,
                    queryParams.get("sort")?.let { FanArtSortOrder.valueOf(it) } ?: FanArtSortOrder.DATE_DESCENDING,
                    queryParams.getAll("tags").map { FanArtTag.valueOf(it) }
                )
            }
        }
    }
}