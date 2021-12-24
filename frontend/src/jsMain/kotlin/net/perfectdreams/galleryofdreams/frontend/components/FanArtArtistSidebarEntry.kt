package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtUtils
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun FanArtArtistSidebarEntry(data: GalleryOfDreamsDataWrapper, fanArtArtist: FanArtArtist) {
    A("/artists/${fanArtArtist.slug}", attrs = { classes("entry") }) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    gap(0.5.em)
                }
            }
        ) {
            FanArtArtistAvatar(data, fanArtArtist, 32) {
                borderRadius(100.percent)
                height(32.px)
                width(32.px)
            }

            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                    }
                }
            ) {
                Div {
                    Text(fanArtArtist.name)
                }
                Div(attrs = {
                    style {
                        fontSize(0.8.em)
                    }
                }) {
                    Text(fanArtArtist.fanArts.size.toString() + " fan arts")
                }
            }
        }
    }
}