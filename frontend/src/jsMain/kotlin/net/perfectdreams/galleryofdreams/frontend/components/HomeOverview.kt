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
    // Sabia que existem pessoas que fazem artes maravilhosas de mim só porque eu ajudei no servidor delas? Eu até me derreto por dentro com tanta arte fofa de mim!
    //
    //Então para mostrar as incríveis artes que fizeram, eu resolvi colocar nesta página todas as fan arts que fizeram! Obrigada a todos que fizeram estas fan arts, eu fico feliz que existe tanta gente assim que gosta do que eu faço!
    //
    //Aliás, se você quiser enviar uma fan art para mim, então envie no meu servidor de suporte! Se a sua arte ficar legal ela poderá aparecer aqui!
    //
    //Para ver as fan arts, comece clicando para ver todas as fan arts que já fizeram ou clique em um artista para ver os que ela fez. Divirta-se apreciando as artes!

    // Erisly:
    // Thanks to all of the artists who've made art of me! I really appreciate it and really love all of your works! Clicking on an artwork will allow you to see the artist and any links they wished to provide. Submit fan art via the #fanart channel in Erisly's Official Discord Server to be added onto here!

    // Pollux:
    // Artwork made with love by our community members, users, and friends! Send your artworks using either this form or tweeting them to @maidPollux!
    // This gallery contains artwork not only from Pollux but from her friends. Event mascots that seasonally appear from time to time~

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