package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntry(i18nContext: I18nContext, name: String) {
    LocalizedA(i18nContext, attrs = { classes("entry") }) {
        Div {
            Text(name)
        }
    }
}