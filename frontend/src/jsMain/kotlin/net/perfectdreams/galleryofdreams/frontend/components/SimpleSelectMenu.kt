package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.*
import kotlinx.browser.document
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.Event

// ===[ CUSTOM SELECT MENU:TM: ]===
// Because styling a default select menu is hard as fuc
// Inspired by Discord's Select Menu
@Composable
fun <T> SimpleSelectMenu(
    entries: List<SimpleSelectMenuEntry<T>>,
    maxValues: Int? = 1,
    onClose: (List<T>) -> Unit
) {
    val singleValueSelectMenu = maxValues == 1
    var isSelectMenuVisible by remember { mutableStateOf(false) }
    var clickEventListener by remember { mutableStateOf<((Event) -> Unit)?>(null) }
    var keydownEventListener by remember { mutableStateOf<((Event) -> Unit)?>(null) }
    val selectedEntries = entries.filter { it.selected }.map { it.value }.toMutableStateList()

    // This is a hack to support changing elements via key up/key down
    // When we press the key, the recomp key is changed, causing a SelectMenu recomposition
    var recomp by remember { mutableStateOf(0) }

    console.log("SelectMenu recomposition hack counter: $recomp")

    key(recomp) {
        Div(attrs = {
            classes("select-wrapper")
        }) {
            Div(
                attrs = {
                    classes("select")

                    // According to the docs, "classes" acumulate instead of replacing!
                    if (isSelectMenuVisible)
                        classes("open")

                    onClick {
                        isSelectMenuVisible = !isSelectMenuVisible
                    }
                }
            ) {
                Div {
                    Div(attrs = {
                        classes("currently-selected-option-content")
                    }) {
                        val currentlySelectedOption = selectedEntries.firstOrNull()
                        // TODO: Better Max Values handling
                        if (currentlySelectedOption == null || maxValues != 1) {
                            Text("Click Here!")
                        } else {
                            val currentlySelectedOptionData = entries.first { it.value == currentlySelectedOption }
                            currentlySelectedOptionData.content.invoke()
                        }
                    }
                    Div(attrs = { classes("chevron") }) {
                        UIIcon(IconManager.chevronDown)
                    }
                }
            }

            if (isSelectMenuVisible) {
                Div(attrs = {
                    classes("menu")

                    ref {
                        val clickCallback: ((Event) -> Unit) = {
                            isSelectMenuVisible = false
                            onClose.invoke(selectedEntries)
                        }

                        val keydownCallback: ((Event) -> Unit) = {}

                        document.addEventListener("click", clickCallback)
                        document.addEventListener("keydown", keydownCallback)

                        clickEventListener = clickCallback
                        keydownEventListener = keydownCallback

                        onDispose {
                            document.removeEventListener("click", clickEventListener)
                            document.removeEventListener("keydown", keydownEventListener)
                            clickEventListener = null
                        }
                    }
                }) {
                    entries.forEach { entry ->
                        Div(
                            attrs = {
                                onClick {
                                    val isAlreadySelected = entry.selected
                                    if (singleValueSelectMenu && isAlreadySelected) {
                                        // Do not propagate to our click event listener
                                        it.stopPropagation()

                                        // But do close the select menu anyway
                                        isSelectMenuVisible = false
                                        onClose.invoke(selectedEntries)
                                        return@onClick
                                    }

                                    val shouldInvoke = maxValues == null || singleValueSelectMenu || (maxValues > selectedEntries.size)

                                    if (!singleValueSelectMenu && entry.value in selectedEntries) {
                                        selectedEntries.remove(entry.value)
                                    } else {
                                        if (shouldInvoke) {
                                            if (entry.value in selectedEntries) {
                                                selectedEntries.remove(entry.value)
                                            } else {
                                                if (singleValueSelectMenu) {
                                                    selectedEntries.removeAt(0)
                                                }

                                                selectedEntries.add(entry.value)
                                            }
                                        }
                                    }

                                    if (singleValueSelectMenu) {
                                        // When you select something (ONLY IN A SINGLE MAX VALUE MENU), it should automatically close
                                        isSelectMenuVisible = false
                                        onClose.invoke(selectedEntries)
                                    }

                                    // Do not propagate to our click event listener
                                    it.stopPropagation()
                                }

                                classes("select-menu-entry")

                                if (entry.value in selectedEntries)
                                    classes("selected")
                            }
                        ) {
                            entry.content.invoke()
                        }
                    }
                }
            }
        }
    }
}

data class SimpleSelectMenuEntry<T>(
    val content: @Composable () -> (Unit),
    val value: T,
    val selected: Boolean
)