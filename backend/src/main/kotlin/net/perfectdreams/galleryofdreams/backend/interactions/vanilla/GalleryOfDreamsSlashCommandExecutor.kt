package net.perfectdreams.galleryofdreams.backend.interactions.vanilla

import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.styled

abstract class GalleryOfDreamsSlashCommandExecutor : LorittaSlashCommandExecutor() {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val guild = context.guildOrNull
        if (guild == null) {
            context.reply(true) {
                styled("Você apenas pode usar este comando em um servidor!")
            }
            return
        }

        if (!context.member.roles.any { it.idLong == 924649809103691786L }) {
            context.reply(true) {
                content = "Você não tem o poder de adicionar fan arts na galeria!"
            }
            return
        }

        executeGalleryOfDreams(context, args)
    }

    abstract suspend fun executeGalleryOfDreams(context: ApplicationCommandContext, args: SlashCommandArguments)
}