package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.attributes.AutoComplete.Companion.name
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text

@Composable
fun LeftSidebar(
    m: GalleryOfDreamsFrontend,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext
) {
    Aside(attrs = {
        id("left-sidebar")
        if (m.appState.isSidebarOpen)
            classes("is-open")
        else
            classes("is-closed")
    }) {
        Div(attrs = { classes("entries") }) {
            LocalizedA(i18nContext, "/", attrs = {
                classes("entry")

                onClick {
                    // Do not redirect
                    it.preventDefault()

                    m.routingManager.switchToHomeOverview(i18nContext)
                }
            }) {
                Text(i18nContext.get(I18nKeysData.AboutTheFanArts))
            }

            SidebarDivider()

            Div(attrs = {
                style {
                    fontWeight("bold")
                }
            }) {
                Text(i18nContext.get(I18nKeysData.Sort))
            }

            val entries = listOf(
                SelectMenuEntry(
                    { Text(i18nContext.get(I18nKeysData.SortType.FanArtCountDescending)) },
                    m.appState.fanArtArtistsSortOrder == FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING,
                    {
                        m.appState.fanArtArtistsSortOrder = FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING
                    },
                    {}
                ),
                SelectMenuEntry(
                    { Text(i18nContext.get(I18nKeysData.SortType.FanArtCountAscending)) },
                    m.appState.fanArtArtistsSortOrder == FanArtArtistSortOrder.FAN_ART_COUNT_ASCENDING,
                    {
                        m.appState.fanArtArtistsSortOrder = FanArtArtistSortOrder.FAN_ART_COUNT_ASCENDING
                    },
                    {}
                ),
                SelectMenuEntry(
                    { Text(i18nContext.get(I18nKeysData.SortType.AlphabeticallyAscending)) },
                    m.appState.fanArtArtistsSortOrder == FanArtArtistSortOrder.ALPHABETICALLY_ASCENDING,
                    {
                        m.appState.fanArtArtistsSortOrder = FanArtArtistSortOrder.ALPHABETICALLY_ASCENDING
                    },
                    {}
                ),
                SelectMenuEntry(
                    { Text(i18nContext.get(I18nKeysData.SortType.AlphabeticallyDescending)) },
                    m.appState.fanArtArtistsSortOrder == FanArtArtistSortOrder.ALPHABETICALLY_DESCENDING,
                    {
                        m.appState.fanArtArtistsSortOrder = FanArtArtistSortOrder.ALPHABETICALLY_DESCENDING
                    },
                    {}
                )
            )

            SelectMenu(entries)

            Div(attrs = {
                style {
                    fontWeight("bold")
                }
            }) {
                Text(i18nContext.get(I18nKeysData.Filter))
            }

            Div {
                Input(InputType.Text) {
                    classes("text-input")

                    onInput {
                        m.appState.fanArtArtistsNameFilter = it.value
                    }

                    value(m.appState.fanArtArtistsNameFilter)
                }
            }

            SidebarDivider()

            A("/fan-arts", attrs = {
                classes("entry")

                onClick {
                    // Do not redirect
                    it.preventDefault()

                    m.routingManager.switchToFanArtsOverview(
                        i18nContext,
                        0,
                        FanArtSortOrder.DATE_DESCENDING,
                        listOf()
                    )
                }
            }) {
                Text(i18nContext.get(I18nKeysData.ViewAllFanArts(data.fanArts.size)))
            }

            val sortedFanArtArtists = when (m.appState.fanArtArtistsSortOrder) {
                FanArtArtistSortOrder.FAN_ART_COUNT_ASCENDING -> data.artists.sortedBy { it.fanArts.size }.asSequence()
                FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING -> data.artists.sortedByDescending { it.fanArts.size }.asSequence()
                FanArtArtistSortOrder.ALPHABETICALLY_ASCENDING -> data.artists.sortedBy { it.name }.asSequence()
                FanArtArtistSortOrder.ALPHABETICALLY_DESCENDING -> data.artists.sortedByDescending { it.name }.asSequence()
            }.filter { it.name.contains(m.appState.fanArtArtistsNameFilter, true) }
                .toList()

            if (sortedFanArtArtists.isNotEmpty()) {
                for (fanArtArtist in sortedFanArtArtists) {
                    Div(attrs = {
                        onClick {
                            // Do not redirect
                            it.preventDefault()

                            m.routingManager.switchToArtistFanArtsOverview(
                                i18nContext,
                                fanArtArtist,
                                0,
                                FanArtSortOrder.DATE_DESCENDING,
                                listOf()
                            )
                        }
                    }) {
                        FanArtArtistSidebarEntry(data, i18nContext, fanArtArtist)
                    }
                }
            } else {
                Div {
                    Text("¯\\_(ツ)_/¯")
                }

                Div {
                    Text("Altere o filtro de busca!")
                }
            }
        }
    }

    Aside(attrs = { id("mobile-left-sidebar" )}) {
        // We use a button so it can be tabbable and has better accessbility
        Button(
            attrs = {
                classes("hamburger-button")
                attr("aria-label", "Menu Button")

                onClick {
                    m.appState.isSidebarOpen = !m.appState.isSidebarOpen
                }
            }
        ) {
            if (m.appState.isSidebarOpen)
                UIIcon(IconManager.times)
            else
                UIIcon(IconManager.bars)
        }
    }
}