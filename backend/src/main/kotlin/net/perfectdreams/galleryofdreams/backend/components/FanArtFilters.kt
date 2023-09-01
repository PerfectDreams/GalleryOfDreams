package net.perfectdreams.galleryofdreams.backend.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.utils.FanArtSortOrder
import net.perfectdreams.galleryofdreams.backend.utils.icon
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext

fun FlowContent.fanArtFilters(m: GalleryOfDreamsBackend, i18nContext: I18nContext, fanArtSortOrder: FanArtSortOrder, fanArtTags: List<FanArtTag>?) {
    div {
        id = "fan-art-filters"
        style = "display: flex; flex-direction: row; gap: 1em; justify-content: space-between; width: 100%;"

        div {
            style = "display: flex; flex-direction: column; width: 100%;"

            div {
                style = "font-weight: bold;"
                text(i18nContext.get(I18nKeysData.Sort))
            }

            div {
                select(classes = "text-input") {
                    attributes["power-select"] = "true"
                    name = "sort"
                    id = "fan-art-filter-sort"

                    option {
                        value = FanArtSortOrder.DATE_ASCENDING.name
                        selected = fanArtSortOrder == FanArtSortOrder.DATE_ASCENDING
                        text(i18nContext.get(I18nKeysData.SortType.DateAscending))
                    }

                    option {
                        value = FanArtSortOrder.DATE_DESCENDING.name
                        selected = fanArtSortOrder == FanArtSortOrder.DATE_DESCENDING
                        text(i18nContext.get(I18nKeysData.SortType.DateDescending))
                    }
                }
            }
        }

        div {
            style = "display: flex; flex-direction: column; width: 100%;"

            div {
                style = "font-weight: bold;"
                text("Tags")
            }

            div {
                select(classes = "text-input") {
                    attributes["power-select"] = "true"
                    attributes["max-values"] = "null"
                    multiple = true
                    name = "tags"
                    id = "fan-art-filter-tags"

                    for (tag in FanArtTag.values()) {
                        option {
                            attributes["option-html"] = createHTML().span {
                                val icon = tag.icon(m)
                                if (icon != null) {
                                    icon.apply(this) {
                                        style = "height: 1em;"
                                    }
                                    text(" ")
                                }
                                text(i18nContext.get(tag.title))
                            }

                            value = tag.name
                            selected = fanArtTags?.contains(tag) ?: false
                            text(i18nContext.get(tag.title))
                        }
                    }
                }
            }
        }

        noScript {
            div {
                style = "display: flex; flex-direction: column; width: 100%;"

                button(type = ButtonType.submit) {
                    text(i18nContext.get(I18nKeysData.UpdateFilters))
                }
            }
        }
    }
}