package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext

/**
 * Context of the executed command
 */
class ApplicationCommandContext(
    loritta: GalleryOfDreamsBackend,
    override val event: GenericCommandInteractionEvent
) : InteractionContext(loritta)