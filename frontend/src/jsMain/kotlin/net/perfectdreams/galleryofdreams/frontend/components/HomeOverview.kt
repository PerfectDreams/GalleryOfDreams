package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeys
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.screen.Screen
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomeOverview(
    m: GalleryOfDreamsFrontend,
    screen: Screen.HomeOverview,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext
) {
    Div(
        attrs = {
            style {
                textAlign("center")
            }
        }
    ) {
        H1 {
            // TODO: Better handling of this
            val (before, after) = i18nContext.language.textBundle.strings[I18nKeys.Home.Title.key]!!
                .split("{perfectDreamsLogo}")

            Span {
                Text(before)
            }

            UIIcon(IconManager.perfectDreamsLogoBlackWithYellowStar) {
                style {
                    height(1.em)
                }

                attr("aria-label", "PerfectDreams")
            }

            Span {
                Text(after)
            }
        }

        for (str in i18nContext.get(I18nKeysData.Home.Description)) {
            Text(str)
        }
    }
}