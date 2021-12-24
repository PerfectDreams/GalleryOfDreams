package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

@Composable
fun SortFanArtsInput(
    i18nContext: I18nContext,
    currentSortOrder: FanArtSortOrder,
    callback: (FanArtSortOrder) -> (Unit)
) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    fontWeight("bold")
                }
            }) {
            Text(i18nContext.get(I18nKeysData.Sort))
        }

        val entries = listOf(
            SelectMenuEntry(
                { Text(i18nContext.get(I18nKeysData.SortType.DateAscending)) },
                currentSortOrder == FanArtSortOrder.DATE_ASCENDING,
                {
                    callback.invoke(FanArtSortOrder.DATE_ASCENDING)
                },
                {}
            ),
            SelectMenuEntry(
                { Text(i18nContext.get(I18nKeysData.SortType.DateDescending)) },
                currentSortOrder == FanArtSortOrder.DATE_DESCENDING,
                {
                    callback.invoke(FanArtSortOrder.DATE_DESCENDING)
                },
                {}
            )
        )

        SelectMenu(entries)
    }
}