package net.perfectdreams.galleryofdreams.frontend.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.screen.Screen
import net.perfectdreams.i18nhelper.core.I18nContext

class RoutingManager(private val m: GalleryOfDreamsFrontend) {
    var screenState by mutableStateOf<Screen?>(null)

    fun switchToHomeOverview(i18nContext: I18nContext) = switch(Screen.HomeOverview(i18nContext))

    fun switchToFanArtsOverview(
        i18nContext: I18nContext,
        page: Int,
        fanArtSortOrder: FanArtSortOrder,
        tags: List<FanArtTag>
    ) = switch(Screen.FanArtsOverview(i18nContext, page, fanArtSortOrder, tags))

    fun switchToArtistFanArtsOverview(
        i18nContext: I18nContext,
        fanArtArtist: FanArtArtist,
        page: Int,
        fanArtSortOrder: FanArtSortOrder,
        tags: List<FanArtTag>
    ) = switch(Screen.FanArtsArtistOverview(i18nContext, fanArtArtist, page, fanArtSortOrder, tags))

    fun switchToFanArtOverview(i18nContext: I18nContext, fanArtArtist: FanArtArtist, fanArt: FanArt) = switch(Screen.FanArtOverview(i18nContext, fanArtArtist, fanArt))

    fun switch(screen: Screen) {
        val currentScreenState = screenState
        // Automatically dispose the current screen ViewModel if the screen has a ViewModel
        if (currentScreenState is Screen.ScreenWithViewModel)
            currentScreenState.model.dispose()
        screenState = screen
        m.appState.isSidebarOpen = false // Close sidebar if it is open

        val newPath = screen.path
        // popstate is fired if "data" is different
        // Title is unused
        // https://developer.mozilla.org/en-US/docs/Web/API/History/pushState
        window.history.pushState(newPath, "", newPath)
        gtagSafe("set", "page_path", newPath)
        gtagSafe("event", "page_view")
    }
}