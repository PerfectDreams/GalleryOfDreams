package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import kotlinx.dom.clear
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import net.perfectdreams.galleryofdreams.frontend.utils.Svg
import org.jetbrains.compose.web.attributes.AttrsBuilder
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
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
                    (attrs as AttrBuilderContext<*>?)?.invoke(this as AttrsBuilder<Element>)
                }
            ) {
                DomSideEffect(icon) { ref ->
                    val viewBox = icon.element.getAttribute("viewBox")
                    if (viewBox != null)
                        ref.setAttributeNS(null, "viewBox", viewBox)

                    icon.element.children.asList().forEach {
                        ref.appendChild(it.cloneNode(true))
                    }

                    onDispose {
                        // Required to avoid the old svg data "staying" on the element
                        // Probably there is a better way to invalidate this
                        ref.clear()
                    }
                }
            }
        }
    }
}