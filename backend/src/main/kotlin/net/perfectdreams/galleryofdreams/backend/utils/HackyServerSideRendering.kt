package net.perfectdreams.galleryofdreams.backend.utils

import com.github.benmanes.caffeine.cache.Caffeine
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

// A very hacky server side rendering support, renders the page with Playwright and then serves the page for bots
class HackyServerSideRendering {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val SKIP_SSR_HEADER = "X-Skip-Hacky-SSR"
    }

    private val crawlers = Json.decodeFromStream<List<Crawler>>(HackyServerSideRendering::class.java.getResourceAsStream("/crawler-user-agents.json"))

    private val pageCache = Caffeine.newBuilder()
        .expireAfterWrite(24L, TimeUnit.HOURS)
        .maximumSize(1_000)
        .build<String, String>()
        .asMap()

    private val renderingSemaphore = Semaphore(4)

    fun isACrawlerRequest(call: ApplicationCall): Boolean = call.request.userAgent()?.let {
        isACrawler(it)
    } ?: false

    fun isACrawler(userAgent: String) = crawlers.any { Regex(it.pattern).containsMatchIn(userAgent) }

    suspend fun getOrRenderRootElementPageHTMLForCrawlers(call: ApplicationCall): String {
        return if (isACrawlerRequest(call))
            getOrRenderRootElementPageHTML(call)
        else
            return ""
    }

    val playwright = Playwright.create()
    val playwrightBrowser = playwright.chromium().launch()
    val playwrightContext = playwrightBrowser.newContext()
        .apply {
            setExtraHTTPHeaders(mapOf(SKIP_SSR_HEADER to "true"))
        }

    suspend fun getOrRenderRootElementPageHTML(call: ApplicationCall): String {
        if (call.request.header(SKIP_SSR_HEADER) != null)
            return ""

        val path = call.request.path()
        val queryString = call.request.queryString()
        val pathWithQueryParameters = if (queryString.isEmpty())
            path
        else
            "$path?$queryString"

        // Load from cache if it is present
        val cached = pageCache[pathWithQueryParameters]
        if (cached != null) {
            logger.info { "(User-Agent: ${call.request.userAgent()}) Returning cached page for $pathWithQueryParameters..." }
            return cached
        }

        try {
            logger.info { "(User-Agent: ${call.request.userAgent()}) Taking permit to render page $pathWithQueryParameters... Currently available permits: ${renderingSemaphore.availablePermits}" }
            renderingSemaphore.withPermit {
                val start = System.currentTimeMillis()
                val result = renderRootElementPageHTML(pathWithQueryParameters)
                pageCache[pathWithQueryParameters] = result
                logger.error { "(User-Agent: ${call.request.userAgent()}) Successfully rendered $pathWithQueryParameters page in ${System.currentTimeMillis() - start}ms! Let's party!!" }
                return result
            }
        } catch (e: BrowserRenderTookTooLong) {
            logger.error { "(User-Agent: ${call.request.userAgent()}) Page render for $pathWithQueryParameters took more than 5000ms! We are going to provide an empty string for the request..." }
            return ""
        }
    }

    private fun renderRootElementPageHTML(pathWithQueryParameters: String): String {
        logger.info { "Preparing to load page $pathWithQueryParameters for Hacky SSR..." }

        playwrightContext.setExtraHTTPHeaders(mapOf(SKIP_SSR_HEADER to "true"))
        playwrightContext.newPage().use { page ->
            page.navigate(
                "http://127.0.0.1:${
                    System.getenv("GALLERYOFDREAMS_WEBSERVER_PORT")?.toIntOrNull() ?: 8080
                }$pathWithQueryParameters"
            )

            var start = System.currentTimeMillis()

            while (true) {
                if (System.currentTimeMillis() - start >= 5_000)
                    throw BrowserRenderTookTooLong()

                val test = page.evaluate("window.composePageIsReady")
                if (test is Boolean && test)
                    break

                logger.info { "Waiting for $pathWithQueryParameters compose page is ready check... Elapsed: ${System.currentTimeMillis() - start}ms" }
                Thread.sleep(100)
            }

            logger.info { "Took ${System.currentTimeMillis() - start}ms to wait for Compose Page is Ready $pathWithQueryParameters variable! " }
            start = System.currentTimeMillis()
            val innerHTML = page.locator("#root").innerHTML()
            logger.info { "Took ${System.currentTimeMillis() - start}ms to query root @ \"$pathWithQueryParameters\"'s innerHTML page!" }
            start = System.currentTimeMillis()
            pageCache[pathWithQueryParameters] = innerHTML
            logger.info { "Took ${System.currentTimeMillis() - start}ms to cache root @ \"$pathWithQueryParameters\"'s innerHTML page!" }

            return innerHTML
        }
    }

    class BrowserRenderTookTooLong : RuntimeException()
}