package net.perfectdreams.galleryofdreams.backend.utils

import kotlinx.html.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import org.jsoup.nodes.Element
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser

class SVGIconManager(val showtime: GalleryOfDreamsBackend) {
    val registeredSvgs = mutableMapOf<String, SVGIcon>()

    val star = register("star", "twemoji/2b50.svg")
    val sparkles = register("sparkles", "twemoji/2728.svg")
    val cake = register("cake", "twemoji/1f382.svg")
    val santaClaus = register("santa-claus", "twemoji/1f385.svg")
    val speechBubble = register("speech-bubble", "twemoji/1f4ac.svg")
    val pumpkin = register("pumpkin", "twemoji/1f383.svg")
    val womanDancing = register("woman-dancing", "twemoji/1f483.svg")
    val bars = register("bars", "fontawesome5/bars.svg", SVGOptions.ADD_CURRENT_COLOR_FILLS)
    val times = register("times", "fontawesome5/times.svg", SVGOptions.ADD_CURRENT_COLOR_FILLS)

    // ===[ BRANDS ]===
    val perfectDreams = register("perfectdreams", "perfectdreams-logo-black-with-yellow-star.svg", SVGOptions.DO_NOT_ADD_ICON_CLASS)

    /**
     * Loads and registers a SVG with [name] and [path]
     *
     * The SVG also checks for name conflicts and stores all registered icons in [registeredSvgs]
     */
    fun register(name: String, path: String, vararg options: SVGOptions): SVGIcon {
        if (name in registeredSvgs)
            throw RuntimeException("There is already a SVG with name $name!")

        val parser = Parser.htmlParser()
        parser.settings(ParseSettings(true, true)) // tag, attribute preserve case, if not stuff like viewBox breaks!
        println(path)
        val document = parser.parseInput(
            SVGIconManager::class.java.getResourceAsStream("/icons/$path")
                .bufferedReader()
                .readText(),
            "/"
        )

        val svgTag = document.getElementsByTag("svg")
            .first()!!

        if (SVGOptions.DO_NOT_ADD_ICON_CLASS !in options) {
            svgTag.addClass("icon") // Add the "icon" class name to the SVG root, this helps us styling it via CSS
        }

        svgTag.addClass("icon-$name") // Also add the icon name to the SVG root, so we can individually style with CSS

        if (SVGOptions.REMOVE_FILLS in options) {
            // Remove all "fill" tags
            svgTag.getElementsByAttribute("fill")
                .removeAttr("fill")
        }

        if (SVGOptions.ADD_CURRENT_COLOR_FILLS in options) {
            // Adds "currentColor" fills
            svgTag.select("path")
                .toList()
                .filterIsInstance<Element>()
                .forEach {
                    it.attr("fill", "currentColor")
                }
        }

        val svgIcon = SVGIcon(svgTag)
        registeredSvgs[name] = svgIcon
        return svgIcon
    }

    class SVGIcon(private val html: Element) {
        fun apply(content: HtmlBlockTag, configureSvgElement: SVG.() -> (Unit) = {}) {
            content.svg {
                html.attributes().forEach {
                    if (it.key != "xmlns")
                        this.attributes[it.key] = it.value
                }

                configureSvgElement.invoke(this)

                unsafe {
                    raw(html.html())
                }
            }
        }
    }

    enum class SVGOptions {
        REMOVE_FILLS,
        ADD_CURRENT_COLOR_FILLS,
        DO_NOT_ADD_ICON_CLASS
    }
}