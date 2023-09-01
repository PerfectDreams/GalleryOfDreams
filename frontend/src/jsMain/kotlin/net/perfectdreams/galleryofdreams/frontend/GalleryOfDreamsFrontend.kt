package net.perfectdreams.galleryofdreams.frontend

import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.utils.io.core.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.frontend.components.*
import net.perfectdreams.galleryofdreams.frontend.utils.*
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.*
import org.w3c.dom.url.URL

class GalleryOfDreamsFrontend {
    var isLeftSidebarOpen = false
    val leftSidebarElement: HTMLElement
        get() = document.querySelector("#left-sidebar") as HTMLElement
    val leftSidebarMobileElement: HTMLElement
        get() = document.querySelector("#mobile-left-sidebar") as HTMLElement

    private fun openSidebar() {
        leftSidebarElement.addClass("is-open")
        leftSidebarMobileElement.addClass("is-open")
        leftSidebarElement.removeClass("is-closed")
        leftSidebarMobileElement.removeClass("is-closed")

        isLeftSidebarOpen = true
    }

    private fun closeSidebar() {
        leftSidebarElement.removeClass("is-open")
        leftSidebarMobileElement.removeClass("is-open")
        leftSidebarElement.addClass("is-closed")
        leftSidebarMobileElement.addClass("is-closed")

        isLeftSidebarOpen = false
    }

    private fun toggleSidebar() {
        if (!isLeftSidebarOpen) {
            openSidebar()
        } else {
            closeSidebar()
        }
    }

    fun start() {
        document.addEventListener("htmx:load", { elt ->
            val targetElement = elt.asDynamic().target as HTMLElement

            val hamburgerButton = targetElement.querySelector("#hamburger-button")
            hamburgerButton?.addEventListener("click", {
                toggleSidebar()
            })

            document.querySelectorAll("[power-close-sidebar='true']").asList().forEach {
                it.addEventListener("click", {
                    closeSidebar()
                })
            }

            targetElement.querySelectorAll("select").asList().forEach {
                if (it is HTMLSelectElement) {
                    if (it.hasAttribute("power-select") && !it.hasAttribute("powered-up")) {
                        // It is a power select!
                        // We are going to render our select menu below it
                        val htmlOptions = it.querySelectorAll("option")
                            .asList()
                            .filterIsInstance<HTMLOptionElement>()

                        val customSelectMenu = document.createElement("div")
                        it.insertAdjacentElement("afterend", customSelectMenu)

                        val selectedEntries = mutableStateListOf<String>()

                        htmlOptions.forEach {
                            println(it.value + " is selected? ${it.selected}")
                            if (it.selected)
                                selectedEntries.add(it.value)
                        }

                        // Do we have any parent form?
                        var formElement: HTMLFormElement? = null
                        var currentParentElement = it.parentElement

                        while (currentParentElement != null) {
                            if (currentParentElement is HTMLFormElement) {
                                formElement = currentParentElement
                                break
                            }

                            currentParentElement = currentParentElement.parentElement
                        }

                        console.log("form element")
                        console.log(formElement)

                        renderComposable(customSelectMenu) {
                            var isFirstCheck by remember { mutableStateOf(true) }

                            val options = htmlOptions.map {
                                SimpleSelectMenuEntry(
                                    {
                                        val html = it.getAttribute("option-html")
                                        if (html != null) {
                                            Span(
                                                attrs = {
                                                    ref { htmlDivElement ->
                                                        htmlDivElement.outerHTML = html

                                                        onDispose {}
                                                    }
                                                }
                                            ) {}
                                        } else {
                                            Text(it.text)
                                        }
                                    },
                                    it.value,
                                    it.value in selectedEntries
                                )
                            }

                            val maxValuesAsString = it.getAttribute("max-values")
                            val maxValues = if (maxValuesAsString == "null")
                                null
                            else
                                maxValuesAsString?.toIntOrNull() ?: 1

                            SimpleSelectMenu(
                                options,
                                maxValues,
                                onClose = {
                                    selectedEntries.clear()
                                    selectedEntries.addAll(it)
                                }
                            )

                            for (entry in selectedEntries) {
                                Input(InputType.Hidden) {
                                    name(it.name)
                                    value(entry)
                                }
                            }

                            if (formElement != null) {
                                key(selectedEntries) {
                                    println(isFirstCheck)

                                    if (!isFirstCheck) {
                                        GlobalScope.launch {
                                            console.log("triggering on form element detected")
                                            trigger(formElement, "submit", mapOf<Any, Any>())
                                        }
                                    }

                                    isFirstCheck = false
                                }
                            }
                        }

                        // Delete the original select menu
                        it.remove()
                    }
                }
            }
        })
    }
}