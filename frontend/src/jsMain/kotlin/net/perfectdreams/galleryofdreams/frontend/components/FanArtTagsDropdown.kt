package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

// TODO: Fix this
@Composable
fun FanArtTagsDropdown(i18nContext: I18nContext, tags: List<FanArtTag>) {
    Ul {
        for (tag in tags) {
            Li {
                Input(InputType.Checkbox)
                Text(" ${i18nContext.get(tag.title)}")
            }
        }
    }
}