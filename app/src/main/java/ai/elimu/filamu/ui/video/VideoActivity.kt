package ai.elimu.filamu.ui.video

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.filamu.BuildConfig
import ai.elimu.filamu.data.ByteArrayDataSourceFactory
import ai.elimu.filamu.data.video.viewmodel.VideoViewModel
import ai.elimu.filamu.data.video.viewmodel.VideoViewModelImpl
import ai.elimu.filamu.databinding.ActivityVideoBinding
import ai.elimu.model.v2.enums.analytics.LearningEventType
import ai.elimu.model.v2.gson.content.VideoGson
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import timber.log.Timber

@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {
    
    private val TAG = VideoActivity::class.java.name

    private lateinit var videoPlayer: ExoPlayer
    private lateinit var binding: ActivityVideoBinding
    private lateinit var videoViewModel: VideoViewModel
    private var videoId: Long = 0L
    private var videoTitle: String = ""
    private var isVideoPlaybackCompleted = false

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityVideoBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initViewModels()

        videoId = intent.getLongExtra(EXTRA_KEY_VIDEO_ID, 0)
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

        videoViewModel.readVideoBytes(videoId) { bytes ->
            bytes ?: return@readVideoBytes
            Timber.tag(TAG).d("videoBytes.length: %s", bytes.size)

            // Create MediaSource from ByteArray
            val dataSourceFactory = ByteArrayDataSourceFactory(bytes)
            val mediaItem = MediaItem.Builder().setUri(Uri.EMPTY)
                .setMimeType(MimeTypes.VIDEO_H264)
                .build()

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)

            videoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    Timber.tag(TAG).v("onPlaybackStateChanged: %s", playbackState)

                    if (playbackState == Player.STATE_ENDED) {
                        videoTitle = intent.getStringExtra(EXTRA_KEY_VIDEO_TITLE) ?: ""
                        val extraData = JSONObject().apply {
                            put(ANALYTICS_PLAYBACK_POSITION, videoPlayer.duration)
                        }
                        LearningEventUtil.reportVideoLearningEvent(
                            videoGson = VideoGson().apply {
                                id = videoId
                                title = videoTitle
                            },
                            additionalData = extraData,
                            learningEventType = LearningEventType.VIDEO_COMPLETED,
                            context = this@VideoActivity,
                            analyticsApplicationId = BuildConfig.ANALYTICS_APPLICATION_ID)
                        isVideoPlaybackCompleted = true
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Timber.tag(TAG)
                        .e("onPlayerError: ${error.errorCode}\nmessage: ${error.message}\ncause: ${error.cause}")
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

    override fun onDestroy() {
        super.onDestroy()

        if (!isVideoPlaybackCompleted) {
            val extraData = JSONObject().apply {
                put(ANALYTICS_PLAYBACK_POSITION, videoPlayer.currentPosition)
            }
            LearningEventUtil.reportVideoLearningEvent(
                videoGson = VideoGson().apply {
                    id = videoId
                    title = videoTitle
                },
                additionalData = extraData,
                learningEventType = LearningEventType.VIDEO_CLOSED_BEFORE_COMPLETION,
                context = this@VideoActivity,
                analyticsApplicationId = BuildConfig.ANALYTICS_APPLICATION_ID)
        }

        videoPlayer.release()
    }

    private fun initViewModels() {
        videoViewModel = ViewModelProvider(this)[VideoViewModelImpl::class.java]
    }

    companion object {
        const val EXTRA_KEY_VIDEO_ID: String = "extra_key_video_id"
        const val EXTRA_KEY_VIDEO_TITLE = "extra_key_video_title"

        private const val ANALYTICS_PLAYBACK_POSITION = "video_playback_position_ms"
    }
}
