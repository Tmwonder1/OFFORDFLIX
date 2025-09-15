/*
 * Copyright 2025 Google LLC
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

package com.google.jetstream.presentation.screens.videoPlayer.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberPlayer(context: Context) = remember {
    // Create HTTP data source with custom headers support
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(30000)
        .setReadTimeoutMs(30000)
        .setUserAgent("Offordflix/1.0")
    
    // Create data source factory that supports both HTTP and local files
    val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
    
    // Create media source factory that supports multiple formats
    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
    
    // Create renderers factory with smart decoder fallback
    val renderersFactory = DefaultRenderersFactory(context).apply {
        // Enable extension renderers (software decoders) as fallback
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        // Enable decoder fallback when hardware fails
        setEnableDecoderFallback(true)
        // Custom MediaCodec selector to filter problematic hardware decoders
        setMediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
            if (mimeType.startsWith("video/")) {
                val decoders = MediaCodecUtil.getDecoderInfos(
                    mimeType, requiresSecureDecoder, requiresTunnelingDecoder
                )
                // Only filter out the most problematic decoders, but allow c2.android.avc.decoder if it's the only one
                val filteredDecoders = if (decoders.size <= 1) {
                    // If there's only one decoder, keep it even if it's problematic
                    println("MediaCodec: Only one decoder available (${decoders.firstOrNull()?.name}), keeping it")
                    decoders
                } else {
                    // Filter out problematic ones only if we have alternatives
                    decoders.filter { decoder ->
                        val name = decoder.name
                        val isProblematic = name.contains("OMX.MS.AVC.Decoder")
                        
                        if (isProblematic) {
                            println("MediaCodec: Filtering out problematic decoder: $name")
                        } else {
                            println("MediaCodec: Keeping decoder: $name")
                        }
                        !isProblematic
                    }
                }
                println("MediaCodec: Available decoders for $mimeType: ${filteredDecoders.size}")
                filteredDecoders.forEach { decoder ->
                    println("MediaCodec: - ${decoder.name}")
                }
                filteredDecoders
            } else {
                // Use default selection for audio
                MediaCodecUtil.getDecoderInfos(
                    mimeType, requiresSecureDecoder, requiresTunnelingDecoder
                )
            }
        }
    }
    
    ExoPlayer.Builder(context)
        .setRenderersFactory(renderersFactory)
        .setSeekForwardIncrementMs(10)
        .setSeekBackIncrementMs(10)
        .setMediaSourceFactory(mediaSourceFactory)
        .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        .build()
        .apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            // Add error listener for debugging and recovery
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    println("ExoPlayer Error: ${error.message}")
                    println("Error Code: ${error.errorCode}")
                    println("Cause: ${error.cause}")
                    
                    // Log specific decoder errors and attempt recovery
                    when (error.errorCode) {
                        4003 -> {
                            println("MediaCodec decoder error - hardware decoder failed")
                            println("Attempting to restart with software decoder...")
                            
                            // Try to recover by restarting the current media item
                            try {
                                val currentPosition = currentPosition
                                val currentMediaItem = currentMediaItem
                                if (currentMediaItem != null) {
                                    stop()
                                    clearMediaItems()
                                    setMediaItem(currentMediaItem)
                                    seekTo(currentPosition)
                                    prepare()
                                    println("ExoPlayer: Decoder recovery attempted")
                                }
                            } catch (recoveryException: Exception) {
                                println("ExoPlayer: Decoder recovery failed: ${recoveryException.message}")
                            }
                        }
                        else -> {
                            error.printStackTrace()
                        }
                    }
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val stateString = when (playbackState) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                    println("ExoPlayer State: $stateString")
                }
                
                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                    println("VideoPlayer: Video size changed - ${videoSize.width}x${videoSize.height}")
                    println("VideoPlayer: Pixel aspect ratio: ${videoSize.pixelWidthHeightRatio}")
                }
            })
        }
}
