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

    val tailSpin = registerSVGIcon("tail-spin", svgTailSpin)
    val star = registerSVGIcon("star", svgStar)
    val sparkles = registerSVGIcon("sparkles", svgSparkles)
    val cake = registerSVGIcon("cake", svgCake)
    val santaClaus = registerSVGIcon("santa-claus", svgSantaClaus)
    val speechBubble = registerSVGIcon("speech-bubble", svgSpeechBubble)
    val pumpkin = registerSVGIcon("pumpkin", svgPumpkin)
    val womanDancing = registerSVGIcon("woman-dancing", svgWomanDancing)
    val bars = registerSVGIcon("bars", svgBars, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val times = registerSVGIcon("times", svgTimes, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val chevronLeft = registerSVGIcon("chevron-left", svgChevronLeft, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val chevronRight = registerSVGIcon("chevron-right", svgChevronRight, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val chevronDown = registerSVGIcon("chevron-down", svgChevronDown, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val perfectDreamsLogoPureBlack = registerSVGIcon("perfectdreams-logo-pure-black", svgPerfectDreamsLogoPureBlack, SVGOptions.REMOVE_FILLS, SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val perfectDreamsLogoBlackWithYellowStar = registerSVGIcon("perfectdreams-logo-black-with-yellow-star", svgPerfectDreamsLogoBlackWithYellowStar)
    val loritta = registerPNGIcon("loritta", pngLoritta)

    fun registerPNGIcon(name: String, base64Asset: String): PNGIcon {
        val pngIcon = PNGIcon(base64Asset)
        registeredIcons[name] = pngIcon
        return pngIcon
    }

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

// Needs to be top level!
@JsModule("./icons/tail-spin.svg")
@JsNonModule
external val svgTailSpin: dynamic

@JsModule("./icons/twemoji/2b50.svg")
@JsNonModule
external val svgStar: dynamic

@JsModule("./icons/twemoji/2728.svg")
@JsNonModule
external val svgSparkles: dynamic

@JsModule("./icons/fontawesome5/bars.svg")
@JsNonModule
external val svgBars: dynamic

@JsModule("./icons/fontawesome5/times.svg")
@JsNonModule
external val svgTimes: dynamic

@JsModule("./icons/fontawesome5/chevron-left.svg")
@JsNonModule
external val svgChevronLeft: dynamic

@JsModule("./icons/fontawesome5/chevron-right.svg")
@JsNonModule
external val svgChevronRight: dynamic

@JsModule("./icons/fontawesome5/chevron-down.svg")
@JsNonModule
external val svgChevronDown: dynamic

@JsModule("./icons/perfectdreams-logo-pure-black.svg")
@JsNonModule
external val svgPerfectDreamsLogoPureBlack: dynamic

@JsModule("./icons/perfectdreams-logo-black-with-yellow-star.svg")
@JsNonModule
external val svgPerfectDreamsLogoBlackWithYellowStar: dynamic

@JsModule("./icons/perfectdreams-logo-colored-with-outline.svg")
@JsNonModule
external val svgPerfectDreamsLogoColoredWithOutline: dynamic

@JsModule("./icons/twemoji/1f382.svg")
@JsNonModule
external val svgCake: dynamic

@JsModule("./icons/twemoji/1f385.svg")
@JsNonModule
external val svgSantaClaus: dynamic

@JsModule("./icons/twemoji/1f383.svg")
@JsNonModule
external val svgPumpkin: dynamic

@JsModule("./icons/twemoji/1f483.svg")
@JsNonModule
external val svgWomanDancing: dynamic

@JsModule("./icons/twemoji/1f4ac.svg")
@JsNonModule
external val svgSpeechBubble: dynamic

@JsModule("./icons/loritta.png")
@JsNonModule
external val pngLoritta: dynamic