package ai.elimu.filamu.ui

import ai.elimu.filamu.data.video.viewmodel.VideoViewModel
import ai.elimu.filamu.data.video.viewmodel.VideoViewModelImpl
import ai.elimu.filamu.databinding.ActivityVideosBinding
import ai.elimu.filamu.databinding.ActivityVideosCoverViewBinding
import ai.elimu.filamu.ui.video.VideoActivity
import ai.elimu.filamu.util.SingleClickListener
import ai.elimu.model.v2.gson.content.VideoGson
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    }

    override fun onStart() {
        Timber.tag(TAG).i("onStart")
        super.onStart()

        // Reset the state of the GridLayout
        binding.videosProgressBar.visibility = View.VISIBLE
        binding.gridLayoutVideos.visibility = View.GONE
        binding.gridLayoutVideos.removeAllViews()

        initData()
    }

    private fun initViewModels() {
        videoViewModel = ViewModelProvider(this)[VideoViewModelImpl::class.java]
    }

    private fun initData() {
        videoViewModel.getAllVideos { videos ->
            Timber.tag(TAG).i("videos.size(): " + videos.size)
            showVideos(videos)
        }
    }

    private fun showVideos(videos: List<VideoGson>) {
        CoroutineScope(Dispatchers.IO).launch {
            var video: VideoGson

            // Create a View for each Video in the list
            for (index in videos.indices) {
                video = videos[index]
                Timber.tag(TAG).i("video.getId(): " + video.id)
                Timber.tag(TAG).i("video.getTitle(): \"" + video.title + "\"")

                val finalVideo = video
                val videoView = ActivityVideosCoverViewBinding.inflate(layoutInflater, binding.gridLayoutVideos, false)

                videoViewModel.getThumbUrl(finalVideo) { thumbUrl ->
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

                        Timber.tag(TAG).i("video.getId(): " + finalVideo.id
                                + ". Title: " + finalVideo.title)

                        val intent = Intent(applicationContext, VideoActivity::class.java)
                        intent.putExtra(
                            VideoActivity.EXTRA_KEY_VIDEO_ID,
                            finalVideo.id
                        )
                        startActivity(intent)
                    }
                })

                withContext(Dispatchers.Main) {
                    binding.gridLayoutVideos.addView(videoView.root)
                    if (binding.gridLayoutVideos.childCount == videos.size) {
                        binding.videosProgressBar.visibility = View.GONE
                        binding.gridLayoutVideos.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
