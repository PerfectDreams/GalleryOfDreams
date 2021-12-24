package net.perfectdreams.galleryofdreams.frontend.screen

import io.ktor.http.*
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtSortOrder
import net.perfectdreams.i18nhelper.core.I18nContext

sealed class Screen {
    abstract val path: String

    class HomeOverview(val i18nContext: I18nContext) : Screen() {
        override val path = "/${i18nContext.get(net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData.WebsiteLocaleIdPath)}/"
    }

    class FanArtsOverview(
        val i18nContext: I18nContext,
        val page: Int,
        val fanArtSortOrder: FanArtSortOrder,
        val tags: List<FanArtTag>
    ) : Screen() {
        override val path = "/${i18nContext.get(net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData.WebsiteLocaleIdPath)}/fan-arts/?" + ParametersBuilder()
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
    }

    class FanArtsArtistOverview(
        val i18nContext: I18nContext,
        val fanArtArtist: FanArtArtist,
        val page: Int,
        val fanArtSortOrder: FanArtSortOrder,
        val tags: List<FanArtTag>,
    ) : Screen() {
        override val path = "/${i18nContext.get(net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}?" + ParametersBuilder()
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
    }

    class FanArtOverview(val i18nContext: I18nContext, val fanArtArtist: FanArtArtist, val fanArt: FanArt) : Screen() {
        override val path = "/${i18nContext.get(net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}/${fanArt.slug}"
    }

    interface ScreenWithViewModel {
        val model: ViewModel
    }
}