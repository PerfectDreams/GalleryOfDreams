package net.perfectdreams.galleryofdreams.backend.utils

import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.WebhookMessageCreateBuilder
import dev.kord.rest.builder.message.modify.WebhookMessageModifyBuilder
import dev.kord.rest.builder.webhook.WebhookModifyBuilder
import dev.kord.rest.ratelimit.ExclusionRequestRateLimiter
import dev.kord.rest.ratelimit.RequestRateLimiter
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.WebhookService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

// Webhook Client that uses Kord's REST Client
internal val jsonDefault = Json {
    encodeDefaults = false
    allowStructuredMapKeys = true
    ignoreUnknownKeys = true
    isLenient = true
}

fun WebhookClient(
    webhookId: Snowflake,
    webhookToken: String,
    requestRateLimiter: RequestRateLimiter = ExclusionRequestRateLimiter(),
    clock: Clock = Clock.System,
    parser: Json = jsonDefault,
): WebhookClient {
    val client = HttpClient(CIO) {
        expectSuccess = false
    }
    return WebhookClient(
        webhookId,
        webhookToken,
        WebhookService(KtorRequestHandler(client, requestRateLimiter, clock, parser))
    )
}

class WebhookClient(
    val webhookId: Snowflake,
    val webhookToken: String,
    val webhookService: WebhookService
) {
    suspend fun getWebhookWithToken() = webhookService.getWebhookWithToken(webhookId, webhookToken)

    suspend inline fun modifyWebhookWithToken(
        builder: WebhookModifyBuilder.() -> Unit
    ) = webhookService.modifyWebhookWithToken(webhookId, webhookToken, builder)

    suspend fun deleteWebhookWithToken(reason: String? = null) = webhookService.deleteWebhookWithToken(webhookId, webhookToken)

    suspend inline fun executeWebhook(
        wait: Boolean? = null,
        threadId: Snowflake? = null,
        builder: WebhookMessageCreateBuilder.() -> Unit
    ) = webhookService.executeWebhook(webhookId, webhookToken, wait, threadId, builder)

    suspend inline fun editWebhookMessage(
        messageId: Snowflake,
        builder: WebhookMessageModifyBuilder.() -> Unit
    ) = webhookService.editWebhookMessage(webhookId, webhookToken, messageId, builder)
}