package net.perfectdreams.galleryofdreams.backend.utils

import com.github.benmanes.caffeine.cache.Caffeine
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.i18nhelper.core.I18nContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

// A very hacky server side rendering support, renders the page with Playwright and then serves the page for bots
class HackyServerSideRendering(val m: GalleryOfDreamsBackend) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val SKIP_SSR_HEADER = "X-Skip-Hacky-SSR"
    }

    private val crawlers = Json.decodeFromStream<List<Crawler>>(HackyServerSideRendering::class.java.getResourceAsStream("/crawler-user-agents.json"))

    val pageCache = Caffeine.newBuilder()
        .expireAfterAccess(7L, TimeUnit.DAYS)
        .maximumSize(10_000)
        .build<String, String>()
        .asMap()

    val languageBrowsers = ConcurrentHashMap<I18nContext, BrowserWrapper>()

    fun isACrawlerRequest(call: ApplicationCall): Boolean = call.request.userAgent()?.let {
        isACrawler(it)
    } ?: false

    fun isACrawler(userAgent: String) = crawlers.any { Regex(it.pattern).containsMatchIn(userAgent) }

    suspend fun getOrRenderRootElementPageHTMLForCrawlers(call: ApplicationCall, i18nContext: I18nContext): String {
        return if (isACrawlerRequest(call))
            getOrRenderRootElementPageHTML(call, i18nContext)
        else
            return ""
    }

    suspend fun getOrRenderRootElementPageHTML(call: ApplicationCall, i18nContext: I18nContext): String {
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
            logger.info { "(User-Agent: ${call.request.userAgent()}) Returning cached page for $pathWithQueryParameters... Currently cached pages: ${pageCache.size}" }
            return cached
        }

        val browserWrapper = languageBrowsers.getOrPut(i18nContext) { BrowserWrapper() }
        logger.info { "(User-Agent: ${call.request.userAgent()}) Taking permit to render page $pathWithQueryParameters... Currently cached pages: ${pageCache.size}" }

        try {
            return browserWrapper.getOrCreatePage {
                val start = System.currentTimeMillis()
                val result = renderRootElementPageHTML(it, pathWithQueryParameters)
                pageCache[pathWithQueryParameters] = result
                logger.info { "(User-Agent: ${call.request.userAgent()}) Successfully rendered $pathWithQueryParameters page in ${System.currentTimeMillis() - start}ms! Let's party!! Currently cached pages: ${pageCache.size}" }
                result
            }
        } catch (e: BrowserRenderTookTooLong) {
            logger.error { "(User-Agent: ${call.request.userAgent()}) Page render for $pathWithQueryParameters took more than 5000ms! We are going to provide an empty string for the request... Currently cached pages: ${pageCache.size}" }
            return ""
        } catch (e: Exception) {
            logger.error(e) { "(User-Agent: ${call.request.userAgent()}) Page render for $pathWithQueryParameters failed! We are going to shutdown the browser and retry the request... Currently cached pages: ${pageCache.size}" }
            browserWrapper.invalidateBrowser()
            return getOrRenderRootElementPageHTML(call, i18nContext)
        }
    }

    private fun renderRootElementPageHTML(page: Page, pathWithQueryParameters: String): String {
        val start = System.currentTimeMillis()
        var debugStart = System.currentTimeMillis()
        logger.info { "Preparing to load page $pathWithQueryParameters for Hacky SSR..." }

        var isFirstPathLoad = false
        if (!page.url().contains("127.0.0.1")) {
            isFirstPathLoad = true
            logger.info { "Navigating to Gallery Of Dreams' local URL..." }
            page.navigate("http://127.0.0.1:${System.getenv("GALLERYOFDREAMS_WEBSERVER_PORT")?.toIntOrNull() ?: 8080}$pathWithQueryParameters")

            val startComposePageLoop = System.currentTimeMillis()
            // We need to wait the script to be loaded
            while (true) {
                if (System.currentTimeMillis() - start >= 5_000)
                    throw BrowserRenderTookTooLong()

                val test = page.evaluate("window.composePageIsReady")
                if (test is Boolean && test)
                    break

                logger.info { "Waiting for $pathWithQueryParameters compose page is ready check... Elapsed: ${System.currentTimeMillis() - startComposePageLoop}ms" }
                page.waitForTimeout(100.0)
            }
        }

        if (!isFirstPathLoad) {
            logger.info { "Switching Gallery Of Dreams' path..." }

            // Reset the page ready check
            page.evaluate("window.composePageIsReady = false")

            // Switch to new path...
            page.evaluate(
                "pathWithQueryParams => frontend.net.perfectdreams.galleryofdreams.frontend.switchToProperScreenBasedOnPathHackySSR(pathWithQueryParams)",
                pathWithQueryParameters
            )

            val startComposePageLoop = System.currentTimeMillis()

            while (true) {
                if (System.currentTimeMillis() - start >= 5_000)
                    throw BrowserRenderTookTooLong()

                val test = page.evaluate("window.composePageIsReady")
                if (test is Boolean && test)
                    break

                logger.info { "Waiting for $pathWithQueryParameters compose page is ready check... Elapsed: ${System.currentTimeMillis() - startComposePageLoop}ms" }
                page.waitForTimeout(100.0)
            }
        }

        logger.info { "Took ${System.currentTimeMillis() - debugStart}ms to wait for Compose Page is Ready $pathWithQueryParameters variable! " }
        debugStart = System.currentTimeMillis()
        val innerHTML = page.locator("#root").innerHTML()
        logger.info { "Took ${System.currentTimeMillis() - debugStart}ms to query root @ \"$pathWithQueryParameters\"'s innerHTML page!" }
        logger.info { "Took ${System.currentTimeMillis() - start}ms to render \"$pathWithQueryParameters\"!" }

        return innerHTML
    }

    class BrowserWrapper {
        private val browserMutex = Mutex()
        private var instance: BrowserInstance? = null

        suspend fun <T> getOrCreatePage(block: suspend (Page) -> (T)): T {
            return browserMutex.withLock {
                val instance = instance
                if (instance == null) {
                    val playwright = Playwright.create()
                    val playwrightChromium = playwright.chromium().launch(
                        // BrowserType.LaunchOptions().setHeadless(false)
                    )
                    val playwrightContext = playwrightChromium.newContext(
                        // Very smol to avoid loading stuff that we don't need (because if larger, it may load images that we don't want)
                        // (because headless load images even if we don't need them, we do actually filter images tho, but still)
                        Browser.NewContextOptions()
                            .setViewportSize(0, 0)
                            .setExtraHTTPHeaders(mapOf(SKIP_SSR_HEADER to "true"))
                    )
                    val page = playwrightContext.newPage().apply {
                        route(Pattern.compile("\\.(png|jpeg|jpg|gif)(\\?.+)?\$")) {
                            it.abort()
                        }
                    }

                    val newInstance = BrowserInstance(
                        playwright,
                        playwrightChromium,
                        playwrightContext,
                        page
                    )
                    this.instance = newInstance
                    block.invoke(newInstance.page)
                } else {
                    block.invoke(instance.page)
                }
            }
        }

        suspend fun invalidateBrowser() {
            browserMutex.withLock {
                val instance = instance
                if (instance != null) {
                    instance.page.close()
                    instance.context.close()
                    instance.browser.close()
                    instance.playwright.close()
                }
                this.instance = null
            }
        }

        class BrowserInstance(
            // Only one thread can access these at the same time!
            val playwright: Playwright,
            val browser: Browser,
            val context: BrowserContext,
            val page: Page
        )
    }

    class BrowserRenderTookTooLong : RuntimeException()
}