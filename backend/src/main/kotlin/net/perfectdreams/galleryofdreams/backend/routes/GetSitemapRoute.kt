package net.perfectdreams.galleryofdreams.backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDeviantArtConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistTwitterConnections
import net.perfectdreams.galleryofdreams.common.MediaTypeUtils
import net.perfectdreams.galleryofdreams.common.StoragePaths
import net.perfectdreams.galleryofdreams.common.data.DeviantArtSocialConnection
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.galleryofdreams.common.data.FanArt
import net.perfectdreams.galleryofdreams.common.data.FanArtArtist
import net.perfectdreams.galleryofdreams.common.data.TwitterSocialConnection
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class GetSitemapRoute(val m: GalleryOfDreamsBackend) : BaseRoute("/sitemap.xml") {
    companion object {
        private val STATIC_PAGES = listOf(
            "/",
            "/fan-arts"
        )
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val docFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            .apply {
                // https://stackoverflow.com/a/41538813/7271796
                isNamespaceAware = true
            }

        // TODO: Move this somewhere else
        val fanArtArtistsData = m.transaction {
            val fanArtArtists = FanArtArtists.selectAll().map { fanArtArtist ->
                val discordSocialConnections = FanArtArtistDiscordConnections.selectAll()
                    .where { FanArtArtistDiscordConnections.artist eq fanArtArtist[FanArtArtists.id] }
                val twitterSocialConnections = FanArtArtistTwitterConnections.selectAll()
                    .where { FanArtArtistTwitterConnections.artist eq fanArtArtist[FanArtArtists.id] }
                val deviantArtSocialConnections = FanArtArtistDeviantArtConnections.selectAll()
                    .where { FanArtArtistDeviantArtConnections.artist eq fanArtArtist[FanArtArtists.id] }

                val fanArts = FanArts.selectAll().where { FanArts.artist eq fanArtArtist[FanArtArtists.id] }.map {
                    FanArt(
                        it[FanArts.id].value,
                        it[FanArts.slug],
                        it[FanArts.title],
                        it[FanArts.description],
                        it[FanArts.createdAt],
                        0, // unused
                        it[FanArts.file],
                        it[FanArts.preferredMediaType],
                        FanArtTags.selectAll().where { FanArtTags.fanArt eq it[FanArts.id] }.map {
                            it[FanArtTags.tag]
                        },
                    )
                }

                FanArtArtist(
                    fanArtArtist[FanArtArtists.id].value,
                    fanArtArtist[FanArtArtists.slug],
                    fanArtArtist[FanArtArtists.name],
                    fanArts,
                    listOf(),
                    discordSocialConnections.map {
                        DiscordSocialConnection(it[FanArtArtistDiscordConnections.discordId])
                    } + twitterSocialConnections.map {
                        TwitterSocialConnection(it[FanArtArtistTwitterConnections.handle])
                    } + deviantArtSocialConnections.map {
                        DeviantArtSocialConnection(it[FanArtArtistDeviantArtConnections.handle])
                    }
                )
            }

            fanArtArtists
        }

        val docBuilder: DocumentBuilder = docFactory.newDocumentBuilder()

        // root elements
        val doc = docBuilder.newDocument()

        // TODO: Transform this into a nice DSL
        val rootElement = doc.createElement("urlset").apply {
            // https://developers.google.com/search/docs/advanced/crawling/localized-versions
            setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9")
            setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:image", "http://www.google.com/schemas/sitemap-image/1.1")
            setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xhtml", "http://www.w3.org/1999/xhtml")

            // https://developers.google.com/search/blog/2012/05/multilingual-and-multinational-site
            for ((_, language) in m.languageManager.languageContexts) {
                // Static Pages
                for (page in STATIC_PAGES) {
                    appendChild(
                        doc.createElement("url").apply {
                            appendChild(
                                doc.createElement("loc").apply {
                                    textContent = m.websiteUrl + "/${language.get(I18nKeysData.WebsiteLocaleIdPath)}$page"
                                }
                            )

                            for ((_, language) in m.languageManager.languageContexts) {
                                appendChild(
                                    doc.createElementNS("http://www.w3.org/1999/xhtml", "xhtml:link").apply {
                                        setAttribute("rel", "alternate")
                                        setAttribute("hreflang", language.get(I18nKeysData.WebsiteLocaleIdPath))
                                        setAttribute("href", m.websiteUrl + "/${language.get(I18nKeysData.WebsiteLocaleIdPath)}$page")
                                    }
                                )
                            }
                        }
                    )
                }

                // Dynamic Pages
                for (fanArtArtist in fanArtArtistsData) {
                    appendChild(
                        doc.createElement("url").apply {
                            appendChild(
                                doc.createElement("loc").apply {
                                    textContent = m.websiteUrl + "/${language.get(I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}"
                                }
                            )

                            for ((_, language) in m.languageManager.languageContexts) {
                                appendChild(
                                    doc.createElementNS("http://www.w3.org/1999/xhtml", "xhtml:link").apply {
                                        setAttribute("rel", "alternate")
                                        setAttribute("hreflang", language.get(I18nKeysData.WebsiteLocaleIdPath))
                                        setAttribute("href", m.websiteUrl + "/${language.get(I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}")
                                    }
                                )
                            }
                        }
                    )

                    for (fanArt in fanArtArtist.fanArts) {
                        appendChild(
                            doc.createElement("url").apply {
                                appendChild(
                                    doc.createElement("loc").apply {
                                        textContent = m.websiteUrl + "/${language.get(I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}/${fanArt.slug}"
                                    }
                                )

                                for ((_, language) in m.languageManager.languageContexts) {
                                    appendChild(
                                        doc.createElementNS("http://www.w3.org/1999/xhtml", "xhtml:link").apply {
                                            setAttribute("rel", "alternate")
                                            setAttribute("hreflang", language.get(I18nKeysData.WebsiteLocaleIdPath))
                                            setAttribute("href", m.websiteUrl + "/${language.get(I18nKeysData.WebsiteLocaleIdPath)}/artists/${fanArtArtist.slug}/${fanArt.slug}")
                                        }
                                    )
                                }

                                // https://developers.google.com/search/docs/advanced/sitemaps/image-sitemaps?hl=pt-br
                                appendChild(
                                    doc.createElement("image:image").apply {
                                        appendChild(
                                            doc.createElement("image:loc").apply {
                                                textContent = "https://assets.perfectdreams.media/galleryofdreams/fan-arts/${StoragePaths.FanArt("${fanArt.file}.${MediaTypeUtils.convertContentTypeToExtension(fanArt.preferredMediaType)}").join()}"
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        doc.appendChild(rootElement)

        val stringWriter = StringWriter()
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        val source = DOMSource(doc)
        val result = StreamResult(stringWriter)

        transformer.transform(source, result)

        call.respondText(
            stringWriter.toString(),
            ContentType.Application.Xml
        )
    }
}