package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

// ===[ CUSTOM SELECT MENU:TM: ]===
// Because styling a default select menu is hard as fuc
// Inspired by Discord's Select Menu
@Composable
fun SelectMenu(
    entries: List<SelectMenuEntry>,
    maxValues: Int? = 1
) {
    val singleValueSelectMenu = maxValues == 1
    var isSelectMenuVisible by remember { mutableStateOf(false) }

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
                    val currentlySelectedOption = entries.firstOrNull { it.selected }
                    // TODO: Better Max Values handling
                    if (currentlySelectedOption == null || maxValues != 1) {
                        Text("Click Here!")
                    } else {
                        currentlySelectedOption.content.invoke()
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
            }) {
                entries.forEach { entry ->
                    Div(
                        attrs = {
                            onClick {
                                val isAlreadySelected = entry.selected
                                if (singleValueSelectMenu && isAlreadySelected)
                                    return@onClick

                                val shouldInvoke = maxValues == null || singleValueSelectMenu || (maxValues > entries.count { it.selected })

                                if (shouldInvoke) {
                                    if (entry.selected) {
                                        entry.onDeselect.invoke()
                                    } else {
                                        entry.onSelect.invoke()
                                    }
                                }

                                if (singleValueSelectMenu) {
                                    // When you select something (ONLY IN A SINGLE MAX VALUE MENU), it should automatically close
                                    isSelectMenuVisible = false
                                }
                            }

                            classes("select-menu-entry")

                            if (entry.selected)
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

data class SelectMenuEntry(
    val content: @Composable () -> (Unit),
    val selected: Boolean,
    val onSelect: () -> (Unit),
    val onDeselect: () -> (Unit)
)