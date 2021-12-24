package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.screen.Screen
import net.perfectdreams.galleryofdreams.frontend.utils.Constants
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date
import kotlin.math.ceil

@Composable
fun FanArtsArtistOverview(
    m: GalleryOfDreamsFrontend,
    screen: Screen.FanArtsArtistOverview,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext,
) {
    H1 {
        Text(screen.fanArtArtist.name)
    }

    // We need to change to page 0 when sort/tags change, if not, the user may think that no fan arts match their description!
    FanArtSortingAndFilters(
        i18nContext,
        screen.fanArtSortOrder,
        screen.tags,
        {
            m.routingManager.switchToArtistFanArtsOverview(
                i18nContext,
                screen.fanArtArtist,
                0,
                it,
                screen.tags
            )
        },
        { tag ->
            m.routingManager.switchToArtistFanArtsOverview(
                i18nContext,
                screen.fanArtArtist,
                0,
                screen.fanArtSortOrder,
                screen.tags.toMutableList()
                    .also { it.add(tag) }
            )
        },
        { tag ->
            // We need to change to page 0 because, if not, the user may think that no fan arts match their description!
            m.routingManager.switchToArtistFanArtsOverview(
                i18nContext,
                screen.fanArtArtist,
                0,
                screen.fanArtSortOrder,
                screen.tags.toMutableList()
                    .also { it.remove(tag) }
            )
        }
    )

    Hr {}

    val filteredFanArts = when (screen.fanArtSortOrder) {
        FanArtSortOrder.DATE_ASCENDING -> screen.fanArtArtist.fanArts.asSequence().sortedBy { it.createdAt }
        FanArtSortOrder.DATE_DESCENDING -> screen.fanArtArtist.fanArts.asSequence().sortedByDescending { it.createdAt }
    }
        .filter { it.tags.containsAll(screen.tags) }
        .toList()

    val sortedFanArts = filteredFanArts
        .drop(Constants.FAN_ARTS_PER_PAGE * (screen.page))
        .take(Constants.FAN_ARTS_PER_PAGE)

    FanArtCardsGrid(m, data, i18nContext, sortedFanArts)

    Div(attrs = { style { textAlign("center") }}) {
        Pagination(screen.page, ceil(filteredFanArts.size / Constants.FAN_ARTS_PER_PAGE.toDouble()).toInt()) {
            m.routingManager.switchToArtistFanArtsOverview(
                i18nContext,
                screen.fanArtArtist,
                it,
                screen.fanArtSortOrder,
                screen.tags
            )
        }
    }
}