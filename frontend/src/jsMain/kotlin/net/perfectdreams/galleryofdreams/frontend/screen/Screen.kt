package net.perfectdreams.galleryofdreams.frontend.screen

import io.ktor.http.*
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.i18nhelper.core.I18nContext

sealed class Screen {
    abstract val path: String
    abstract val title: String

    class HomeOverview(val i18nContext: I18nContext) : Screen() {
        override val path = "/${i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)}/"
        override val title = i18nContext.get(I18nKeysData.WebsiteTitle)
    }

    class FanArtsOverview(
        val i18nContext: I18nContext,
        val page: Int,
        val fanArtSortOrder: FanArtSortOrder,
        val tags: List<FanArtTag>
    ) : Screen() {
        override val path = "/${i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)}/fan-arts/?" + ParametersBuilder()
            .also {
                if (page != 0)
                    it.append("page", (page + 1).toString())

                it.append("sort", fanArtSortOrder.name)
                for (tag in tags) {
                    it.append("tags", tag.name)
                }
            }
            .build()
            .formUrlEncode()

        override val title = "Fan Arts • " + i18nContext.get(I18nKeysData.WebsiteTitle)
    }

    class FanArtsArtistOverview(
        val i18nContext: I18nContext,
        val fanArtArtist: FanArtArtist,
        val page: Int,
        val fanArtSortOrder: FanArtSortOrder,
        val tags: List<FanArtTag>,
    ) : Screen() {
        override val path = "/${i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}?" + ParametersBuilder()
            .also {
                if (page != 0)
                    it.append("page", (page + 1).toString())

                it.append("sort", fanArtSortOrder.name)
                for (tag in tags) {
                    it.append("tags", tag.name)
                }
            }
            .build()
            .formUrlEncode()

        override val title = i18nContext.get(I18nKeysData.UserFanArts(fanArtArtist.name)) + " • " + i18nContext.get(I18nKeysData.WebsiteTitle)
    }

    class FanArtOverview(val i18nContext: I18nContext, val fanArtArtist: FanArtArtist, val fanArt: FanArt) : Screen() {
        override val path = "/${i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}/${fanArt.slug}"

        override val title = (fanArt.title ?: i18nContext.get(I18nKeysData.FanArtBy(fanArtArtist.name))) + " • " +i18nContext.get(I18nKeysData.WebsiteTitle)
    }

    interface ScreenWithViewModel {
        val model: ViewModel
    }
}