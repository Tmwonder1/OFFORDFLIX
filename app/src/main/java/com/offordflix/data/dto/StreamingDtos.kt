package com.offordflix.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamingResponseDto(
    val success: Boolean? = null,
    val data: StreamingDataDto? = null,
    val timestamp: String? = null
)

@Serializable
data class StreamingDataDto(
    val files: List<StreamFileDto> = emptyList(),
    val subtitles: List<StreamSubtitleDto> = emptyList()
)

@Serializable
data class StreamFileDto(
    val file: String,
    val type: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    val headers: Map<String, String>? = null
)

@Serializable
data class StreamSubtitleDto(
    val url: String,
    @SerialName("lang") val language: String? = null,
    val label: String? = null
)


