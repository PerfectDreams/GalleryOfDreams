package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.screen.Screen
import net.perfectdreams.galleryofdreams.frontend.utils.Constants
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.galleryofdreams.frontend.utils.icon
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import kotlin.math.ceil

@Composable
fun FanArtsOverview(
    m: GalleryOfDreamsFrontend,
    screen: Screen.FanArtsOverview,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext
) {
    // We need to change to page 0 when sort/tags change, if not, the user may think that no fan arts match their description!
    FanArtSortingAndFilters(
        i18nContext,
        screen.fanArtSortOrder,
        screen.tags,
        {
            m.routingManager.switchToFanArtsOverview(
                i18nContext,
                0,
                it,
                screen.tags
            )
        },
        { tag ->
            m.routingManager.switchToFanArtsOverview(
                i18nContext,
                0,
                screen.fanArtSortOrder,
                screen.tags.toMutableList()
                    .also { it.add(tag) }
            )
        },
        { tag ->
            // We need to change to page 0 because, if not, the user may think that no fan arts match their description!
            m.routingManager.switchToFanArtsOverview(
                i18nContext,
                0,
                screen.fanArtSortOrder,
                screen.tags.toMutableList()
                    .also { it.remove(tag) }
            )
        }
    )

    Hr {}

    val filteredFanArts = when (screen.fanArtSortOrder) {
        FanArtSortOrder.DATE_ASCENDING -> data.fanArts.asSequence().sortedBy { it.createdAt }
        FanArtSortOrder.DATE_DESCENDING -> data.fanArts.asSequence().sortedByDescending { it.createdAt }
    }
        .filter { it.tags.containsAll(screen.tags) }
        .toList()

    val sortedFanArts = filteredFanArts
        .drop(Constants.FAN_ARTS_PER_PAGE * (screen.page))
        .take(Constants.FAN_ARTS_PER_PAGE)

    FanArtCardsGrid(m, data, i18nContext, sortedFanArts)

    Div(attrs = { style { textAlign("center") } }) {
        Pagination(screen.page, ceil(filteredFanArts.size / Constants.FAN_ARTS_PER_PAGE.toDouble()).toInt()) {
            m.routingManager.switchToFanArtsOverview(
                i18nContext,
                it,
                screen.fanArtSortOrder,
                screen.tags
            )
        }
    }
}