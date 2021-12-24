package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.frontend.utils.FanArtUtils
import net.perfectdreams.galleryofdreams.frontend.utils.GalleryOfDreamsDataWrapper
import org.jetbrains.compose.web.css.StyleBuilder
import org.jetbrains.compose.web.dom.Img

@Composable
fun FanArtArtistAvatar(
    data: GalleryOfDreamsDataWrapper,
    fanArtArtist: FanArtArtist,
    imageSize: Int,
    builder: StyleBuilder.() -> (Unit)
) {
    Img(src = FanArtUtils.getArtistAvatarUrl(data, fanArtArtist, imageSize)) {
        attr("loading", "lazy")

        style {
            property("object-fit", "cover")
            builder.invoke(this)
        }
    }
}