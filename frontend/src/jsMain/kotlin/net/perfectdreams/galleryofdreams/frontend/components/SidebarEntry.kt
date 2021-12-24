package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun SidebarEntry(name: String) {
    A(attrs = { classes("entry") }) {
        Div {
            Text(name)
        }
    }
}