package net.perfectdreams.loritta.morenitta.interactions.modals

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.modals.ModalInteraction
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext

/**
 * Context of the executed command
 */
class ModalContext(
    loritta: GalleryOfDreamsBackend,
    override val event: ModalInteraction
) : InteractionContext(loritta) {
    suspend fun deferEdit(): InteractionHook = event.deferEdit().await()
}