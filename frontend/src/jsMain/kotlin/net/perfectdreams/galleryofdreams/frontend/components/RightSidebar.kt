package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AutoComplete.Companion.name
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Article
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Text

@Composable
fun RightSidebar(block: @Composable () -> (Unit)) {
    Section(attrs = { id("right-sidebar") }) {
        Article(attrs = { classes("content") }) {
            block.invoke()
        }
    }
}