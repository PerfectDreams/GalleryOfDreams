package net.perfectdreams.galleryofdreams.frontend.utils

import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.w3c.dom.HTMLElement

fun <T : HTMLElement> classesAttrs(vararg classes: String): AttrBuilderContext<T> = {
    classes(*classes)
}