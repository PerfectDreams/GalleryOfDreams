package net.perfectdreams.galleryofdreams.backend.components

import kotlinx.html.*

// From GalleryOfDreams, but with some changes (example: instead of <button> it is a <a> link)
// Current Page is 0 indexed!
fun FlowContent.pagination(currentPage: Int, maxPages: Int) {
    val maxPagesZeroIndexed = maxPages - 1

    // Amount of pages >= 0, no need for pagination
    // (The amount is less than zero if there isn't any elements on the page!)
    if (0 >= maxPagesZeroIndexed)
        return

    div(classes = "pagination") {
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

        // Because it is zero indexed, we don't need to -1
        val leftArrowPage = currentPage
        button(classes = "arrow") {
            name = "page"
            value = leftArrowPage.toString()

            if (!showLeftArrow)
                style = "visibility: hidden;"

            +"<"
        }

        if (showQuickJumpToStart) {
            button(classes = "inactive") {
                name = "page"
                value = "1"

                +"1"
            }

            span(classes = "ellipsis") {
                + "..."
            }
        }

        for (i in centeredRange) {
            val iPlusOne = i + 1

            button(classes = if (currentPage == i) "active" else "inactive") {
                name = "page"
                value = iPlusOne.toString()

                +(iPlusOne).toString()
            }
        }

        if (showQuickJumpToEnd) {
            span(classes = "ellipsis") {
                + "..."
            }

            button(classes = "inactive") {
                name = "page"
                value = maxPages.toString()

                +maxPages.toString()
            }
        }


        val rightArrowPage = currentPage + 2
        button(classes = "arrow") {
            name = "page"
            value = rightArrowPage.toString()

            if (!showRightArrow) {
                style = "visibility: hidden;"
            }

            +">"
        }
    }
}