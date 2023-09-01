package net.perfectdreams.galleryofdreams.backend.utils

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.*

val ApplicationCall.htmxElementTarget: String?
    get() = this.request.header("HX-Target")

/**
 * Anchor
 */
@HtmlTagMarker
inline fun FlowOrInteractiveOrPhrasingContent.aHtmx(href : String, hxTarget : String, classes : String? = null, crossinline block : A.() -> Unit = {}) = a(href = href, classes = classes) {
    attributes["hx-get"] = href
    attributes["hx-push-url"] = "true"
    attributes["hx-target"] = hxTarget
    block()
}