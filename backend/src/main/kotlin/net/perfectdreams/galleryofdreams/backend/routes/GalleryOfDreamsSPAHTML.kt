package net.perfectdreams.galleryofdreams.backend.routes

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.title
import kotlinx.html.unsafe
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend

fun galleryOfDreamsSpaHtml(m: GalleryOfDreamsBackend, rootHtmlContent: String = ""): HTML.() -> (Unit) = {
    head {
        title("Gallery of Dreams")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        styleLink("/assets/css/style.css?hash=${m.hashManager.getAssetHash("/assets/css/style.css")}")
        script(src = "/assets/js/frontend.js?hash=${m.hashManager.getAssetHash("/assets/js/frontend.js")}") {
            defer = true // Only execute after the page has been parsed
        }

        link(href = "/favicon.svg", rel = "icon", type = "image/svg+xml")

        unsafe {
            raw("""
                <!-- Global site tag (gtag.js) - Google Analytics -->
                <script async src="https://www.googletagmanager.com/gtag/js?id=G-30QBEL5NBS"></script>
                <script>
                  window.dataLayer = window.dataLayer || [];
                  function gtag(){dataLayer.push(arguments);}
                  gtag('js', new Date());

                  gtag('config', 'G-30QBEL5NBS');
                </script>
            """.trimIndent())
        }
    }

    body {
        unsafe {
            raw("""<div id="spa-loading-wrapper">
    <!-- By Sam Herbert (@sherb), for everyone. More @ http://goo.gl/7AJzbL -->
    <svg class="loading-spinner" width="38" height="38" viewBox="0 0 38 38" xmlns="http://www.w3.org/2000/svg">
        <defs>
            <linearGradient x1="8.042%" y1="0%" x2="65.682%" y2="23.865%" id="a">
                <stop stop-color="currentColor" stop-opacity="0" offset="0%"/>
                <stop stop-color="currentColor" stop-opacity=".631" offset="63.146%"/>
                <stop stop-color="currentColor" offset="100%"/>
            </linearGradient>
        </defs>
        <g fill="none" fill-rule="evenodd">
            <g transform="translate(1 1)">
                <path d="M36 18c0-9.94-8.06-18-18-18" id="Oval-2" stroke="url(#a)" stroke-width="2">
                    <animateTransform attributeName="transform" type="rotate" from="0 18 18" to="360 18 18" dur="0.9s" repeatCount="indefinite"/>
                </path>
                <circle fill="currentColor" cx="36" cy="18" r="1">
                    <animateTransform attributeName="transform" type="rotate" from="0 18 18" to="360 18 18" dur="0.9s" repeatCount="indefinite"/>
                </circle>
            </g>
        </g>
    </svg>
</div>
<div id="root">
$rootHtmlContent
</div>""")
        }
    }
}