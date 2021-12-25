package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.common.i18n.I18nKeysData
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.w3c.dom.HTMLAnchorElement

@Composable
fun LocalizedA(
    i18nContext: I18nContext,
    href: String? = null,
    attrs: AttrBuilderContext<HTMLAnchorElement>? = null,
    content: ContentBuilder<HTMLAnchorElement>? = null
) {
    val localizedHref = if (href != null)
        "/${i18nContext.get(I18nKeysData.WebsiteLocaleIdPath)}$href"
    else
        null
    A(localizedHref, attrs, content)
}