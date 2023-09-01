package net.perfectdreams.galleryofdreams.backend.views

import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.utils.websiteLocaleIdPath
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeys
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext

class HomeView(
    m: GalleryOfDreamsBackend,
    i18nContext: I18nContext,
    title: String,
    pathWithoutLocaleId: String,
    dssBaseUrl: String,
    namespace: String,
) : DashboardView(m, i18nContext, title, pathWithoutLocaleId, dssBaseUrl, namespace) {
    override fun rightSidebar(): FlowContent.() -> (Unit) = {
        div {
            style = "text-align: center;"

            h1 {
                // TODO: Better handling of this
                val (before, after) = i18nContext.language.textBundle.strings[I18nKeys.Home.Title.key]!!
                    .split("{perfectDreamsLogo}")

                span {
                    text(before)
                }

                span {
                    m.svgIconManager.perfectDreams.apply(this) {
                        style = "height: 1em;"

                        attributes["aria-label"] = "PerfectDreams"
                    }
                }

                span {
                    text(after)
                }
            }

            for (str in i18nContext.get(I18nKeysData.Home.Description)) {
                p {
                    text(str)
                }
            }
        }
    }
}