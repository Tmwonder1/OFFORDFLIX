/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.data.models.cinepro

import kotlinx.serialization.Serializable

@Serializable
data class CineProResponse(
    val files: List<CineProFile>,
    val subtitles: List<CineProSubtitle>
)

@Serializable
data class CineProFile(
    val file: String,
    val type: String, // "hls", "mp4", "embed"
    val lang: String
)

@Serializable
data class CineProSubtitle(
    val url: String,
    val lang: String,
    val type: String // "srt", "vtt", etc.
)
