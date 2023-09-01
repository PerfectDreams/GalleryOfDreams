package net.perfectdreams.galleryofdreams.frontend.utils

import net.perfectdreams.galleryofdreams.frontend.utils.IconManager.registeredIcons
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.svg.SVGElement

// Inspired by Loritta's Showtime "SVGIconManager" class
object IconManager {
    val registeredIcons = mutableMapOf<String, Icon>()

    val chevronDown = registerSVGIcon("chevron-down", svgChevronDown, SVGOptions.ADD_CURRENT_COLOR_FILLS)

    /**
     * Loads and registers a SVG with [name] and [path]
     *
     * The SVG also checks for name conflicts and stores all registered icons in [registeredIcons]
     */
    fun registerSVGIcon(name: String, html: String, vararg options: SVGOptions): SVGIcon {
        if (name in registeredIcons)
            throw RuntimeException("There is already a SVG with name $name!")

        val parser = DOMParser()
        val document = parser.parseFromString(html, "image/svg+xml")

        val svgTag = document.getElementsByTagName("svg")
            .asList()
            .first()

        // TODO: Check if we really need this
        // svgTag.addClass("icon") // Add the "icon" class name to the SVG root, this helps us styling it via CSS
        //    .addClass("icon-$name") // Also add the icon name to the SVG root, so we can individually style with CSS

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.querySelectorAll("[fill]")
                .asList()
                .filterIsInstance<Element>()
                .forEach {
                    it.removeAttribute("fill")
                }


            // And remove all "fill" tags from the style too
            svgTag.querySelectorAll("[style]")
                .asList()
                .filterIsInstance<SVGElement>()
                .forEach {
                    it.style.removeProperty("fill")
                }
        }

        if (SVGOptions.ADD_CURRENT_COLOR_FILLS in options) {
            // Adds "currentColor" fills
            svgTag.querySelectorAll("path")
                .asList()
                .filterIsInstance<Element>()
                .forEach {
                    it.setAttribute("fill", "currentColor")
                }
        }

        val svgIcon = SVGIcon(svgTag)
        registeredIcons[name] = svgIcon
        return svgIcon
    }

    sealed class Icon
    class PNGIcon(val base64Asset: String) : Icon()
    class SVGIcon(val element: Element) : Icon()

    enum class SVGOptions {
        REMOVE_FILLS,
        ADD_CURRENT_COLOR_FILLS,
    }
}

@JsModule("./icons/fontawesome5/chevron-down.svg")
@JsNonModule
external val svgChevronDown: dynamic