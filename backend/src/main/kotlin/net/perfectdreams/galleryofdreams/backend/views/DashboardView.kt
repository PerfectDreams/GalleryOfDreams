package net.perfectdreams.galleryofdreams.backend.views

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.components.fanArtArtist
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.FanArtArtistWithFanArtCount
import net.perfectdreams.galleryofdreams.backend.utils.aHtmx
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.exposed.sql.ResultRow

abstract class DashboardView(
    val m: GalleryOfDreamsBackend,
    val i18nContext: I18nContext,
    val title: String,
    val pathWithoutLocaleId: String,
    val dssBaseUrl: String,
    val namespace: String,
) {
    fun generateHtml(totalFanArtCount: Long, fanArtistsSidebar: List<FanArtArtistWithFanArtCount>): HTML.() -> (Unit) = {
        attributes["lang"] = i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)

        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")
            // We are sure that we will access the DreamStorageService URL, so let's preconnect!
            link(href = m.dreamStorageServiceClient.baseUrl, rel = "preconnect")

            title(this@DashboardView.title)

            // https://www.reddit.com/r/discordapp/comments/82p8i6/a_basic_tutorial_on_how_to_get_the_most_out_of/
            meta(name = "theme-color", "#29a6fe")
            metaBlock()

            styleLink("/assets/css/style.css?hash=${m.hashManager.getAssetHash("/assets/css/style.css")}")
            script(src = "/assets/js/frontend.js?hash=${m.hashManager.getAssetHash("/assets/js/frontend.js")}") {
                defer = true // Only execute after the page has been parsed
            }
            meta(
                name = "htmx-config",
                content = buildJsonObject {
                    put("defaultSettleDelay", 0)
                }.toString()
            )

            link(href = "/favicon.svg", rel = "icon", type = "image/svg+xml")

            // https://stackoverflow.com/a/67476915/7271796
            for ((_, language) in m.languageManager.languageContexts.filter { it.value != i18nContext }) {
                // The href must be absolute!
                link(rel = "alternate", href = m.websiteUrl + "/" + language.get(I18nKeysData.WebsiteLocaleIdPath) + pathWithoutLocaleId) {
                    attributes["hreflang"] = language.get(I18nKeysData.WebsiteLocaleIdPath)
                }
            }

            script(
                src = "https://web-analytics.perfectdreams.net/js/plausible.js",
            ) {
                attributes["data-domain"] = "fanarts.perfectdreams.net"
                defer = true
            }
        }

        body {
            attributes["hx-boost"] = "true"
            attributes["hx-swap"] = "focus-scroll:false"

            div {
                id = "root"

                div {
                    id = "wrapper"

                    nav(classes = "is-closed") {
                        id = "mobile-left-sidebar"
                        attributes["hx-preserve"] = "true"

                        button(classes = "hamburger-button") {
                            id = "hamburger-button"

                            attributes["aria-label"] = "Menu Button"

                            div(classes = "open-ui") {
                                m.svgIconManager.bars.apply(this) {
                                    style = "height: 1em;"
                                }
                            }

                            div(classes = "close-ui") {
                                m.svgIconManager.times.apply(this) {
                                    style = "height: 1em;"
                                }
                            }
                        }
                    }

                    nav(classes = "is-closed") {
                        id = "left-sidebar"
                        attributes["hx-preserve"] = "true"

                        div(classes = "entries") {
                            aHtmx(classes = "entry", href = "/${i18nContext.websiteLocaleIdPath}/", hxTarget = "#content") {
                                attributes["power-close-sidebar"] = "true"

                                text(i18nContext.get(I18nKeysData.AboutTheFanArts))
                            }

                            hr(classes = "divider")

                            form(method = FormMethod.post) {
                                id = "fan-art-artists-filters"
                                attributes["hx-post"] = "/${i18nContext.websiteLocaleIdPath}/fan-art-artists"
                                attributes["hx-trigger"] = "submit, keyup changed delay:500ms from:#fan-art-artist-search, change from:#fan-art-artist-sort"
                                attributes["hx-target"] = "#fan-art-artists"
                                attributes["hx-vals"] = buildJsonObject {
                                    put("offset", 0)
                                }.toString()

                                div {
                                    style = "font-weight: bold;"

                                    text(i18nContext.get(I18nKeysData.Sort))
                                }

                                select(classes = "text-input") {
                                    attributes["power-select"] = "true"
                                    name = "sort"
                                    id = "fan-art-artist-sort"

                                    option {
                                        value = FanArtArtistSortOrder.FAN_ART_COUNT_ASCENDING.name
                                        text(i18nContext.get(I18nKeysData.SortType.FanArtCountAscending))
                                    }

                                    option {
                                        value = FanArtArtistSortOrder.FAN_ART_COUNT_DESCENDING.name
                                        selected = true
                                        text(i18nContext.get(I18nKeysData.SortType.FanArtCountDescending))
                                    }

                                    option {
                                        value = FanArtArtistSortOrder.ALPHABETICALLY_ASCENDING.name
                                        text(i18nContext.get(I18nKeysData.SortType.AlphabeticallyAscending))
                                    }

                                    option {
                                        value = FanArtArtistSortOrder.ALPHABETICALLY_DESCENDING.name
                                        text(i18nContext.get(I18nKeysData.SortType.AlphabeticallyDescending))
                                    }
                                }

                                div {
                                    style = "font-weight: bold;"

                                    text(i18nContext.get(I18nKeysData.Filter))
                                }

                                input(InputType.search, classes = "text-input") {
                                    id = "fan-art-artist-search"
                                    name = "query"
                                }
                            }

                            hr(classes = "divider")

                            aHtmx(classes = "entry", href = "/${i18nContext.websiteLocaleIdPath}/fan-arts", hxTarget = "#content") {
                                attributes["power-close-sidebar"] = "true"

                                text(i18nContext.get(I18nKeysData.ViewAllFanArts(totalFanArtCount)))
                            }

                            div {
                                id = "fan-art-artists"

                                for (fanArtist in fanArtistsSidebar) {
                                    fanArtArtist(i18nContext, dssBaseUrl, this@DashboardView.namespace, fanArtist.fanArtArtist, fanArtist.fanArtCount)
                                }

                                div {
                                    attributes["hx-post"] = "/${i18nContext.websiteLocaleIdPath}/fan-art-artists"
                                    attributes["hx-trigger"] = "intersect once"
                                    attributes["hx-swap"] = "afterend"
                                    attributes["hx-include"] = "#fan-art-artists-filters"
                                    attributes["hx-vals"] = buildJsonObject {
                                        put("offset", GalleryOfDreamsBackend.ARTIST_LIST_COUNT_PER_QUERY)
                                    }.toString()
                                }
                            }
                        }
                    }

                    section {
                        id = "right-sidebar"

                        article {
                            id = "content"
                            apply(rightSidebar())
                        }
                    }
                }
            }
        }
    }

    abstract fun rightSidebar(): FlowContent.() -> (Unit)

    open fun HEAD.metaBlock() {}
}