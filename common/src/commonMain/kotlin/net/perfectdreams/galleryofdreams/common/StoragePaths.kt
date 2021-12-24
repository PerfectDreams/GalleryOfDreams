package net.perfectdreams.galleryofdreams.common

object StoragePaths {
    fun FanArt(file: String) = StoragePath("fan-arts", file)

    data class StoragePath(
        val folder: String,
        val file: String
    ) {
        fun join() = "$folder/$file"
    }
}