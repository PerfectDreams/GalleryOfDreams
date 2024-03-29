package net.perfectdreams.galleryofdreams.common.data.api

import kotlinx.serialization.Serializable

@Serializable
sealed class CheckFanArtResponse

@Serializable
class FanArtExistsResponse : CheckFanArtResponse()

@Serializable
class FanArtDoesNotExistResponse : CheckFanArtResponse()