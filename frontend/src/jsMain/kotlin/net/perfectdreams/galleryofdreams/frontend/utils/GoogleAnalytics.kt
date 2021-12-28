package net.perfectdreams.galleryofdreams.frontend.utils

external fun gtag(vararg elements: dynamic)

fun gtagSafe(vararg elements: dynamic) {
    try {
        gtag(*elements)
    } catch (e: dynamic) {
        // Throws ReferenceError if Google Analytics is not loaded
        println("Google Analytics not found or wasn't loaded! ${e.message}")
    }
}