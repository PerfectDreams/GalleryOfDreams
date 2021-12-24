package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.icon
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun FanArtSortingAndFilters(
    i18nContext: I18nContext,
    selectedFanArtSortOrder: FanArtSortOrder,
    selectedTags: List<FanArtTag>,
    onSortChange: (FanArtSortOrder) -> (Unit),
    onTagFilterSelect: (FanArtTag) -> (Unit),
    onTagFilterDeselect: (FanArtTag) -> (Unit)
) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                gap(1.em)
                justifyContent(JustifyContent.SpaceBetween)
                width(100.percent)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    width(100.percent)
                }
            }
        ) {
            SortFanArtsInput(
                i18nContext,
                selectedFanArtSortOrder
            ) {
                onSortChange.invoke(it)
            }
        }

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    width(100.percent)
                }
            }
        ) {
            Div(
                attrs = {
                    style {
                        fontWeight("bold")
                    }
                }) {
                Text("Tags")
            }

            Div {
                val entries =
                    FanArtTag.values()
                        .map { tag ->
                            SelectMenuEntry(
                                {
                                    val icon = tag.icon
                                    if (icon != null) {
                                        UIIcon(icon) {
                                            style {
                                                height(1.em)
                                                width(1.em)
                                            }
                                        }

                                        Text(" ${i18nContext.get(tag.title)}")
                                    } else {
                                        Text(i18nContext.get(tag.title))
                                    }
                                },
                                tag in selectedTags,
                                {
                                    onTagFilterSelect.invoke(tag)
                                },
                                {
                                    onTagFilterDeselect.invoke(tag)
                                }
                            )
                        }

                SelectMenu(entries, maxValues = null)
            }
        }
    }
}