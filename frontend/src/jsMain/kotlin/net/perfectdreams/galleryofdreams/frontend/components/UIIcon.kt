package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import net.perfectdreams.galleryofdreams.frontend.utils.Svg
import org.jetbrains.compose.web.attributes.AttrsBuilder
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Img
import org.w3c.dom.Element
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.asList

@Composable
fun UIIcon(icon: IconManager.Icon, attrs: AttrBuilderContext<Element>? = null) {
    when (icon) {
        is IconManager.PNGIcon -> {
            Img(src = icon.base64Asset) {
                // I don't know why we need to cast this tbh but without it the compiler complains
                (attrs as AttrBuilderContext<*>?)?.invoke(this)
            }
        }
        is IconManager.SVGIcon -> {
            Svg(
                {
                    ref { element ->
                        val viewBox = icon.element.getAttribute("viewBox")
                        if (viewBox != null)
                            element.setAttributeNS(null, "viewBox", viewBox)

                        icon.element.children.asList().forEach {
                            element.appendChild(it.cloneNode(true))
                        }
                        onDispose {}
                    }

                    (attrs as AttrBuilderContext<*>?)?.invoke(this as AttrsBuilder<Element>)
                }
            )
        }
    }
}