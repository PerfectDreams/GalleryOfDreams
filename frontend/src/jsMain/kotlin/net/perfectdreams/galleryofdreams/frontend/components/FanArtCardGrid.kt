package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.frontend.GalleryOfDreamsFrontend
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.gridTemplateRows
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun FanArtCardsGrid(
    m: GalleryOfDreamsFrontend,
    data: GalleryOfDreamsDataWrapper,
    i18nContext: I18nContext,
    fanArts: List<FanArt>
) {
    if (fanArts.isEmpty()) {
        Div {
            H1 {
                Text("¯\\_(ツ)_/¯")
            }
            P {
                Text("Nenhuma fan art é compatível com o seu filtro!")
            }
        }
    } else {
        Div(attrs = {
            style {
                display(DisplayStyle.Grid)
                gridTemplateColumns("repeat(auto-fill, minmax(192px, 1fr))")
                gridTemplateRows("repeat(auto-fill, minmax(192px, 1fr))")
                gap(1.em)
                justifyContent(JustifyContent.SpaceBetween)
                width(100.percent)
            }
        }) {
            for (fanArt in fanArts) {
                FanArtCard(m, data, i18nContext, fanArt)
            }
        }
    }
}