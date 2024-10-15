package net.perfectdreams.galleryofdreams.backend.interactions.vanilla

import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.etherealgambi.data.api.UploadFileResponse
import net.perfectdreams.galleryofdreams.backend.GalleryOfDreamsBackend
import net.perfectdreams.galleryofdreams.backend.tables.FanArtArtists
import net.perfectdreams.galleryofdreams.backend.tables.FanArtTags
import net.perfectdreams.galleryofdreams.backend.tables.FanArts
import net.perfectdreams.galleryofdreams.backend.tables.connections.FanArtArtistDiscordConnections
import net.perfectdreams.galleryofdreams.common.FanArtTag
import net.perfectdreams.galleryofdreams.common.data.DiscordSocialConnection
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.styled
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.security.MessageDigest
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GalleryOfDreamsCommand(val loritta: GalleryOfDreamsBackend) : SlashCommandDeclarationWrapper {
    companion object {
        private val messageLinkRegex = Regex("https?://(?:[A-z]+\\.)?discord\\.com/channels/([0-9]+)/([0-9]+)/([0-9]+)")
        private val allowedFanArtContentTypes = listOf(
            ContentType.Image.PNG,
            ContentType.Image.JPEG,
            ContentType.Image.GIF,
        )
    }

    override fun command() = slashCommand("galleryofdreams", "Comandos relacionados a Galeria dos Sonhos") {
        subcommand("add", "Adiciona uma Fan Art na Galeria dos Sonhos pelo link dela") {
            executor = AddFanArtExecutor(loritta)
        }
    }

    class AddFanArtExecutor(val loritta: GalleryOfDreamsBackend) : GalleryOfDreamsSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val messageUrl = string("message_url", "Link da Mensagem da Fan Art")

            val userOverride = optionalUser("user_override", "Substitui o usuário da Fan Art enviada para outro usuário")
        }

        override val options = Options()

        override suspend fun executeGalleryOfDreams(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val link = messageLinkRegex.matchEntire(args[options.messageUrl])

            if (link == null) {
                context.reply(true) {
                    content = "Você não passou o link de uma mensagem!"
                }
                return
            }

            val (_, guildIdAsString, channelIdAsString, messageIdAsString) = link.groupValues

            val targetMessage = context.event.jda.getGuildById(guildIdAsString)!!.getChannelById(GuildMessageChannel::class.java, channelIdAsString)!!.retrieveMessageById(messageIdAsString).await()

            val attachments = targetMessage.attachments

            if (attachments.isEmpty()) {
                context.reply(true) {
                    styled(
                        "Não existe nenhuma imagem na mensagem que você selecionou!"
                    )
                }
                return
            }

            val artistId = args[options.userOverride]?.user?.idLong ?: targetMessage.author.idLong
            val artistSlug = args[options.userOverride]?.user?.name ?: targetMessage.author.name
            val artistName = args[options.userOverride]?.user?.globalName ?: targetMessage.author.globalName ?: artistSlug

            val matchedFanArtArtist = loritta.transaction {
                FanArtArtists.innerJoin(FanArtArtistDiscordConnections)
                    .selectAll()
                    .where { FanArtArtistDiscordConnections.discordId eq artistId }
                    .firstOrNull()
            }

            context.reply(true) {
                addFanArtMessage(matchedFanArtArtist, context.user, artistId, artistName, artistSlug, targetMessage, null, listOf())
            }
        }

        fun InlineMessage<*>.addFanArtMessage(
            matchedFanArtArtist: ResultRow?,
            user: User,
            // TODO: This is a "artist DISCORD USER ID"
            artistId: Long,
            artistName: String,
            artistSlug: String,
            targetMessage: Message,
            selectedAttachment: Attachment?,
            selectedTags: List<FanArtTag>
        ) {
            content = buildString {
                if (matchedFanArtArtist == null) {
                    append("**(Artista que não está no banco de dados da Galeria dos Sonhos! O artista será criado na galeria dos sonhos ao enviar a fan art)**")
                }
                append(" Configure as informações da Fan Art!")
                if (selectedAttachment != null) {
                    append(" Selecionada: ${selectedAttachment.url}")
                }
            }

            actionRow(
                loritta.interactivityManager.stringSelectMenu({
                    for (attachment in targetMessage.attachments) {
                        addOption(attachment.fileName, attachment.id)
                    }

                    if (selectedAttachment != null)
                        setDefaultValues(selectedAttachment.id)
                }) { context, strings ->
                    val hook = context.deferEdit()

                    val selectedAttachment = targetMessage.attachments.first { it.id == strings.first() }

                    hook.editOriginal(
                        MessageEdit {
                            addFanArtMessage(matchedFanArtArtist, user, artistId, artistName, artistSlug, targetMessage, selectedAttachment, selectedTags)
                        }
                    ).await()
                }
            )

            actionRow(
                loritta.interactivityManager.stringSelectMenu({
                    for (tag in FanArtTag.entries) {
                        addOption(tag.name, tag.name)
                    }

                    this.setMinValues(0)
                    this.setMaxValues(FanArtTag.entries.size)
                    this.setDefaultValues(selectedTags.map { it.name })
                }) { context, strings ->
                    val hook = context.deferEdit()

                    hook.editOriginal(
                        MessageEdit {
                            addFanArtMessage(matchedFanArtArtist, user, artistId, artistName, artistSlug, targetMessage, selectedAttachment, strings.map { FanArtTag.valueOf(it) })
                        }
                    ).await()
                }
            )

            actionRow(
                if (selectedAttachment != null) {
                    loritta.interactivityManager.buttonForUser(
                        user,
                        ButtonStyle.SUCCESS,
                        "Adicionar Fan Art"
                    ) {
                        it.deferChannelMessage(true)

                        // Add fan art to the gallery!
                        val response = loritta.http.get(selectedAttachment.url)

                        if (response.status != HttpStatusCode.OK) {
                            it.reply(true) {
                                styled(
                                    "Tentei baixar a imagem que você selecionou, mas parece que ela foi deletada!"
                                )
                            }
                            return@buttonForUser
                        }

                        val contentType = response.contentType()
                        if (contentType == null) {
                            it.reply(true) {
                                styled(
                                    "O arquivo não tem um Content-Type associado! Bug?"
                                )
                            }
                            return@buttonForUser
                        }

                        if (contentType !in allowedFanArtContentTypes) {
                            it.reply(true) {
                                styled(
                                    "O arquivo não está em um formato que a Gallery of Dreams aceita! Content-Type: $contentType"
                                )
                            }
                            return@buttonForUser
                        }

                        val body = response.bodyAsBytes()
                        val fileHash = sha256Hash(body)

                        // TODO: How to check for duplicate fan arts? (or maybe just... not do it? stuff to think about)
                        val fanArtArtist = loritta.transaction {
                            if (FanArts.selectAll().where { FanArts.fileHash eq fileHash }.count() != 0L)
                                return@transaction null

                            // Query the database again to avoid duplicate runs causing two different artists being created
                            val existingFanArtArtist = FanArtArtists.innerJoin(FanArtArtistDiscordConnections)
                                .selectAll()
                                .where { FanArtArtistDiscordConnections.discordId eq artistId }
                                .firstOrNull()
                            if (existingFanArtArtist != null)
                                return@transaction existingFanArtArtist

                            // Cleans up the user's name to make it be the user's name, if the result is a empty string we use a "ifEmpty" call to change it to the user's ID
                            val fanArtArtistSlug = artistSlug.lowercase()
                                .replace(" ", "-")
                                .replace(Regex("[^A-Za-z0-9-]"), "")
                                .trim()
                                .ifEmpty { artistId.toString() }

                            val slugIsAvailable = FanArtArtists.selectAll().where {
                                FanArtArtists.slug eq fanArtArtistSlug
                            }.count() != 0L

                            val fanArtArtist = FanArtArtists.insert {
                                it[FanArtArtists.name] = artistName
                                if (slugIsAvailable)
                                    it[FanArtArtists.slug] = fanArtArtistSlug
                                else
                                    it[FanArtArtists.slug] = artistId.toString()
                            }.resultedValues!!.first() // very hacky

                            FanArtArtistDiscordConnections.insert {
                                it[FanArtArtistDiscordConnections.artist] = fanArtArtist[FanArtArtists.id]
                                it[FanArtArtistDiscordConnections.discordId] = artistId
                            }

                            fanArtArtist
                        }


                        if (fanArtArtist == null) {
                            it.reply(true) {
                                styled(
                                    "Esta Fan Art já foi enviada! (Ou pode ser que alguém enviou o mesmo arquivo duas vezes...)"
                                )
                            }
                            return@buttonForUser
                        }

                        // The fan art slug is based on the file content
                        // This helps to detect duplicated fan arts!
                        val fanArtSlug = UUID.nameUUIDFromBytes(body).toString()

                        val fanArtCreatedAt = targetMessage.timeCreated.toInstant().toKotlinInstant()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")
                            .withZone(ZoneId.of("America/Sao_Paulo")) // Specify your desired time zone
                        val s = formatter.format(fanArtCreatedAt.toJavaInstant())

                        val ext = when (contentType) {
                            ContentType.Image.PNG -> "png"
                            ContentType.Image.JPEG -> "jpg"
                            ContentType.Image.GIF -> "gif"
                            else -> error("Unsupported Content-Type $contentType")
                        }

                        val artistImageFile = "${s}_${fanArtArtist[FanArtArtists.slug]}_$fanArtSlug.$ext"

                        // Upload to EtherealGambi
                        val fileUploadResponse = loritta.etherealGambiClient.uploadFile(loritta.config.etherealGambi.authorizationToken, "fan-arts/$artistImageFile", true, body)

                        when (fileUploadResponse) {
                            is UploadFileResponse.Success -> {
                                // Add new fan art
                                val newFanArt = loritta.transaction {
                                    val newFanArt = FanArts.insert {
                                        it[FanArts.slug] = fanArtSlug
                                        it[FanArts.artist] = fanArtArtist[FanArtArtists.id]
                                        it[FanArts.title] = null
                                        it[FanArts.description] = null
                                        it[FanArts.createdAt] = fanArtCreatedAt
                                        it[FanArts.file] = artistImageFile
                                        it[FanArts.fileHash] = fileHash
                                        it[FanArts.preferredMediaType] = "unused"
                                    }

                                    // Insert the fan art tags
                                    for (tag in selectedTags) {
                                        FanArtTags.insert {
                                            it[FanArtTags.fanArt] = newFanArt[FanArts.id]
                                            it[FanArtTags.tag] = tag
                                        }
                                    }

                                    newFanArt
                                }

                                val newFanArtUrl = "https://fanarts.perfectdreams.net/artists/${fanArtArtist[FanArtArtists.slug]}/${newFanArt[FanArts.slug]}"

                                it.reply(true) {
                                    styled(
                                        "Fan Art adicionada! <:gabriela_brush:727259143903248486> $newFanArtUrl"
                                    )
                                }

                                GlobalScope.launch {
                                    if (matchedFanArtArtist != null) {
                                        loritta.webhookClient?.send(
                                            WebhookMessageBuilder()
                                                .setContent("<:gabriela_brush:727259143903248486> **Fan Art adicionada!** (<@${artistId}>) <a:lori_lick:957368372025262120> $newFanArtUrl")
                                                .build()
                                        )
                                    } else {
                                        loritta.webhookClient?.send(
                                            WebhookMessageBuilder()
                                                .setContent("<:gabriela_brush:727259143903248486> **Artista e Fan Art adicionadas!** (<@${artistId}>) <a:lori_lick:957368372025262120> $newFanArtUrl")
                                                .build()
                                        )
                                    }
                                }
                            }
                            UploadFileResponse.FileAlreadyExists -> {
                                it.reply(true) {
                                    styled(
                                        "Um arquivo com o mesmo nome já existe! Será que a fan art já foi enviada antes?"
                                    )
                                }
                                return@buttonForUser
                            }
                            UploadFileResponse.PathTraversalDisallowed -> {
                                it.reply(true) {
                                    styled(
                                        "O arquivo possui dois pontos seguidos, e isso não é permitido para evitar exploits de path traversal!"
                                    )
                                }
                                return@buttonForUser
                            }
                            UploadFileResponse.Unauthorized -> {
                                it.reply(true) {
                                    styled(
                                        "Falha na autorização!"
                                    )
                                }
                                return@buttonForUser
                            }
                        }
                    }
                } else {
                    Button.of(ButtonStyle.SUCCESS, "disabled_button_plz_ignore", "Adicionar Fan Art")
                        .asDisabled()
                }
            )
        }

        private fun sha256Hash(byteArray: ByteArray): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(byteArray)
        }

        private fun ByteArray.toHex(): String {
            return joinToString("") { "%02x".format(it) }
        }
    }
}