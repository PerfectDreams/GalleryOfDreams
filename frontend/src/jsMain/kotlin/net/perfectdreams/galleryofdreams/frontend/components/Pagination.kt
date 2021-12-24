package net.perfectdreams.galleryofdreams.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.galleryofdreams.frontend.utils.IconManager
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

// Current Page is 0 indexed!
@Composable
fun Pagination(currentPage: Int, maxPages: Int, onClickBlock: (Int) -> (Unit)) {
    val maxPagesZeroIndexed = maxPages - 1

    // Amount of pages >= 0, no need for pagination
    // (The amount is less than zero if there isn't any elements on the page!)
    if (0 >= maxPagesZeroIndexed)
        return

    Div(attrs = { classes("pagination") }) {
        val differenceBetweenStartAndCurrent = currentPage
        val differenceBetweenCurrentAndEnd = maxPagesZeroIndexed - currentPage

        // We want to have a CONSISTENT amount of entries in the pagination
        // This avoids the "<" and ">" buttons jumping around like crazy
        // TODO: I think this could be done in a better way, but idk how
        val paginationEntries = mutableListOf(currentPage)
        val range = (0 until maxPages).toMutableList()
        var currentLeftPaginationQueryIndex = currentPage
        var currentRightPaginationQueryIndex = currentPage
        val showLeftArrow = differenceBetweenStartAndCurrent != 0
        val showRightArrow = differenceBetweenCurrentAndEnd != 0

        // 11 = 10 on the sides, +1 for the current page
        // Cancel the loop if the pagination entries == 11 OR if there isn't any more ranges to be retrieved
        // The reason we increase if the quick jumps aren't present is to avoid content shifts!
        val totalPaginationEntries = 11

        while (paginationEntries.size != totalPaginationEntries) {
            val getElementLeft = range.getOrNull(--currentLeftPaginationQueryIndex)
            val getElementRight = range.getOrNull(++currentRightPaginationQueryIndex)

            // If both are null, then it means that there is nothing else for us to read here, so let's just get out of here!
            if (getElementLeft == null && getElementRight == null)
                break

            if (getElementLeft != null)
                paginationEntries.add(0, getElementLeft)
            if (getElementRight != null)
                paginationEntries.add(getElementRight)
        }

        var centeredRange = paginationEntries.first()..paginationEntries.last()

        // We are also only going to show a quick start/end page jump if the page selector is outside of the current range!
        val showQuickJumpToStart = centeredRange.first != 0
        val showQuickJumpToEnd = centeredRange.last != maxPagesZeroIndexed

        if (showQuickJumpToStart && paginationEntries.size >= 2) {
            // The additional entry
            paginationEntries.removeAt(0)
            // The ellipsis
            paginationEntries.removeAt(0)
        }

        if (showQuickJumpToEnd && paginationEntries.size >= 2) {
            // The additional entry
            paginationEntries.removeAt(paginationEntries.size - 1)
            // The ellipsis
            paginationEntries.removeAt(paginationEntries.size - 1)
        }

        centeredRange = paginationEntries.first()..paginationEntries.last()

        Button(
            attrs = {
                onClick {
                    onClickBlock.invoke(currentPage - 1)
                }

                classes("arrow")

                // Required to avoid content shifts
                if (!showLeftArrow) {
                    style {
                        property("visibility", "hidden")
                    }
                }
            }
        ) {
            UIIcon(IconManager.chevronLeft)
        }

        if (showQuickJumpToStart) {
            Button(
                attrs = {
                    onClick {
                        onClickBlock.invoke(0)
                    }

                    classes("inactive")
                }
            ) {
                Text(1.toString())
            }

            Span(attrs = { classes("ellipsis") }) {
                Text("...")
            }
        }

        for (i in centeredRange) {
            Button(
                attrs = {
                    onClick {
                        onClickBlock.invoke(i)
                    }
                    if (currentPage == i)
                        classes("active")
                    else
                        classes("inactive")
                }
            ) {
                Text((i + 1).toString())
            }
        }

        if (showQuickJumpToEnd) {
            Span(attrs = { classes("ellipsis") }) {
                Text("...")
            }

            Button(
                attrs = {
                    onClick {
                        onClickBlock.invoke(maxPagesZeroIndexed)
                    }

                    classes("inactive")
                }
            ) {
                Text((maxPages).toString())
            }
        }

        Button(
            attrs = {
                onClick {
                    onClickBlock.invoke(currentPage + 1)
                }

                classes("arrow")

                // Required to avoid content shifts
                if (!showRightArrow) {
                    style {
                        property("visibility", "hidden")
                    }
                }
            }
        ) {
            UIIcon(IconManager.chevronRight)
        }
    }
}