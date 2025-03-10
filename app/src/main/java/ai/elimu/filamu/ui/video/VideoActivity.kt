package ai.elimu.filamu.ui.video

import ai.elimu.filamu.data.ByteArrayDataSourceFactory
import ai.elimu.filamu.databinding.ActivityVideoBinding
import ai.elimu.filamu.util.readVideoBytes
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class VideoActivity : AppCompatActivity() {
    
    private val TAG = VideoActivity::class.java.name

    private lateinit var videoPlayer: ExoPlayer
    private lateinit var binding: ActivityVideoBinding

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityVideoBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val videoId = intent.getLongExtra(EXTRA_KEY_VIDEO_ID, 0)
        Timber.tag(TAG).i("videoId: $videoId")

        val renderersFactory = DefaultRenderersFactory(this).setEnableDecoderFallback(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(
                buildUponParameters()
                    .setAllowAudioMixedMimeTypeAdaptiveness(true)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setForceLowestBitrate(false)
            )
        }

        videoPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .build()
        binding.playerView.player = videoPlayer

        CoroutineScope(Dispatchers.IO).launch {
            val videoBytes = readVideoBytes(videoId) ?: return@launch
            val videoCodec = detectVideoCodec(this@VideoActivity, videoBytes)

            val codecInfo = MediaCodecUtil.getDecoderInfo("video/avc", false, false)
            Timber.tag(TAG).d("Decoder: ${codecInfo?.name ?: "Not Found"}")


            Timber.tag(TAG).d("videoBytes.length: " + videoBytes.size + ". codec: " + videoCodec)
            withContext(Dispatchers.Main) {
                // Create MediaSource from ByteArray
                val dataSourceFactory = ByteArrayDataSourceFactory(videoBytes)
                val mediaItem = MediaItem.Builder().setUri(Uri.EMPTY)
                    .setMimeType(MimeTypes.VIDEO_H264)
                    .build()

                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)

                videoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        Log.v(TAG, "onPlaybackStateChanged: " + playbackState)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Timber.tag(TAG)
                            .e("onPlayerError: " + error.errorCode + "\nmessage: " + error.message + "\ncause: " + error.cause)
                    }

                    override fun onRenderedFirstFrame() {
                        super.onRenderedFirstFrame()
                        Timber.tag(TAG).d("onRenderedFirstFrame")
                    }

                })

                // Prepare and play
                videoPlayer.setMediaSource(mediaSource)
                videoPlayer.playWhenReady = true
                videoPlayer.prepare()
                videoPlayer.play()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.release()
    }

    private fun detectVideoCodec(context: Context, videoBytes: ByteArray): String? {
        // Step 1: Save ByteArray to a temporary file
        val tempFile = File(context.cacheDir, "temp_video.mp4")
        FileOutputStream(tempFile).use { it.write(videoBytes) }

        // Step 2: Use MediaExtractor to read the file
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(tempFile.absolutePath)

            // Step 3: Iterate through tracks to find the video track
            for (i in 0 until extractor.trackCount) {
                val format: MediaFormat = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)

                if (mime?.startsWith("video/") == true) {
                    return mime // Example: "video/avc" (H.264), "video/hevc" (H.265)
                }
            }
            null // No video track found
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            extractor.release()
        }
    }

    companion object {
        const val EXTRA_KEY_VIDEO_ID: String = "extra_key_video_id"
    }
}
