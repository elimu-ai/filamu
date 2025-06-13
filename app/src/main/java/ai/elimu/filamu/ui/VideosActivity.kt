package ai.elimu.filamu.ui

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.common.utils.ui.setLightStatusBar
import ai.elimu.common.utils.ui.setStatusBarColorCompat
import ai.elimu.filamu.BuildConfig
import ai.elimu.filamu.R
import ai.elimu.filamu.data.video.viewmodel.LoadVideosUiState
import ai.elimu.filamu.data.video.viewmodel.VideoViewModel
import ai.elimu.filamu.data.video.viewmodel.VideoViewModelImpl
import ai.elimu.filamu.databinding.ActivityVideosBinding
import ai.elimu.filamu.databinding.ActivityVideosCoverViewBinding
import ai.elimu.filamu.ui.video.VideoActivity
import ai.elimu.filamu.util.SingleClickListener
import ai.elimu.model.v2.enums.analytics.LearningEventType
import ai.elimu.model.v2.gson.content.VideoGson
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class VideosActivity : AppCompatActivity() {

    private lateinit var videoViewModel: VideoViewModel
    private lateinit var binding: ActivityVideosBinding

    var TAG: String = VideosActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModels()
        initData()

        window.apply {
            setLightStatusBar()
            setStatusBarColorCompat(R.color.colorPrimaryDark)
        }
    }

    override fun onStart() {
        Timber.tag(TAG).i("onStart")
        super.onStart()

        // Reset the state of the GridLayout
        binding.videosProgressBar.visibility = View.VISIBLE
        binding.gridLayoutVideos.visibility = View.GONE
        binding.gridLayoutVideos.removeAllViews()
    }

    private fun initViewModels() {
        videoViewModel = ViewModelProvider(this)[VideoViewModelImpl::class.java]
    }

    private fun initData() {
        videoViewModel.getAllVideos()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                videoViewModel.uiState.collect { uiState ->
                    Timber.tag(TAG).d("uiState collected uiState: $uiState")
                    when (uiState) {
                        is LoadVideosUiState.Loading ->
                            binding.videosProgressBar.visibility = View.VISIBLE

                        is LoadVideosUiState.Success -> {
                            binding.videosProgressBar.visibility = View.GONE
                            showVideos(uiState.videos)
                        }
                    }
                }
            }
        }
    }

    private fun showVideos(videos: List<VideoGson>) {
        // Create a View for each Video in the list
        for (video in videos) {
            Timber.tag(TAG).i("video.getId(): %s", video.id)
            Timber.tag(TAG).i("video.getTitle(): %s", video.title)

            val videoView = ActivityVideosCoverViewBinding.inflate(layoutInflater, binding.gridLayoutVideos, false)

            videoViewModel.getThumbUrl(video) { thumbUrl ->
                val coverImageView =
                    videoView.coverImageView
                Glide.with(this@VideosActivity).load(thumbUrl).into(coverImageView)
            }

            val coverTitleTextView =
                videoView.coverTitleTextView
            coverTitleTextView.text = video.title


            videoView.root.setOnClickListener(object : SingleClickListener() {
                override fun onSingleClick(v: View?) {
                    Timber.tag(TAG).i("onClick")

                    Timber.tag(TAG).i("video.getId(): ${video.id}. Title: ${video.title}")

                    val intent = Intent(applicationContext, VideoActivity::class.java)
                    intent.putExtra(
                        VideoActivity.EXTRA_KEY_VIDEO_ID,
                        video.id
                    )
                    intent.putExtra(VideoActivity.EXTRA_KEY_VIDEO_TITLE, video.title)

                    LearningEventUtil.reportVideoLearningEvent(
                        videoGson = video,
                        learningEventType = LearningEventType.VIDEO_OPENED,
                        context = this@VideosActivity,
                        analyticsApplicationId = BuildConfig.ANALYTICS_APPLICATION_ID)

                    startActivity(intent)
                }
            })

            binding.gridLayoutVideos.addView(videoView.root)
            if (binding.gridLayoutVideos.childCount == videos.size) {
                binding.videosProgressBar.visibility = View.GONE
                binding.gridLayoutVideos.visibility = View.VISIBLE
            }
        }
    }
}
