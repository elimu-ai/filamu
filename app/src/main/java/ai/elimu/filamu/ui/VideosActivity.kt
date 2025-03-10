package ai.elimu.filamu.ui

import ai.elimu.filamu.R
import ai.elimu.filamu.data.video.viewmodel.VideoViewModel
import ai.elimu.filamu.data.video.viewmodel.VideoViewModelImpl
import ai.elimu.filamu.databinding.ActivityVideosBinding
import ai.elimu.filamu.databinding.ActivityVideosCoverViewBinding
import ai.elimu.filamu.ui.video.VideoActivity
import ai.elimu.filamu.util.SingleClickListener
import ai.elimu.filamu.util.extractFirstFrameFromVideo
import ai.elimu.filamu.util.readVideoBytes
import ai.elimu.model.v2.gson.content.VideoGson
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ProgressBar
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
    private var videosGridLayout: GridLayout? = null
    private var videosProgressBar: ProgressBar? = null

    private lateinit var videoViewModel: VideoViewModel
    private lateinit var binding: ActivityVideosBinding

    var TAG: String = VideosActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videosGridLayout = binding.gridLayoutVideos
        videosProgressBar = binding.videosProgressBar

        initViewModels()
    }

    override fun onStart() {
        Log.i(javaClass.name, "onStart")
        super.onStart()

        // Reset the state of the GridLayout
        videosProgressBar!!.visibility = View.VISIBLE
        videosGridLayout!!.visibility = View.GONE
        videosGridLayout!!.removeAllViews()

        initData()
    }

    private fun initViewModels() {
        videoViewModel = ViewModelProvider(this)[VideoViewModelImpl::class.java]
    }

    private fun initData() {
        videoViewModel.getAllVideos { videos ->
            Log.i(TAG, "videos.size(): " + videos.size)
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

                var thumb: Bitmap?
                val finalVideo = video
                val videoView = ActivityVideosCoverViewBinding.inflate(layoutInflater, videosGridLayout, false)
                CoroutineScope(Dispatchers.IO).launch {
                    val videoBytes = readVideoBytes(finalVideo.id) ?: return@launch
                    Timber.tag(TAG).d("Extracting thumb for video id: " + finalVideo.id)
                    thumb = this@VideosActivity.extractFirstFrameFromVideo(videoBytes)
                    Timber.tag(TAG)
                        .d("thumb.w: " + thumb?.width + ". h: " + thumb?.height + ". videoID: " + finalVideo.id)
                    withContext(Dispatchers.Main) {
                        val coverImageView =
                            videoView.coverImageView
                        thumb?.let {
                            Glide.with(this@VideosActivity).load(thumb).into(coverImageView)
                        }
                    }
                }

                val coverTitleTextView =
                    videoView.coverTitleTextView
                coverTitleTextView.text = video.title


                videoView.root.setOnClickListener(object : SingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        Log.i(TAG, "onClick")

                        Log.i(TAG, "video.getId(): " + finalVideo.id)
                        Log.i(TAG, "video.getTitle(): " + finalVideo.title)

                        val intent = Intent(applicationContext, VideoActivity::class.java)
                        intent.putExtra(
                            VideoActivity.EXTRA_KEY_VIDEO_ID,
                            finalVideo.id
                        )
                        startActivity(intent)
                    }
                })

                runOnUiThread {
                    videosGridLayout!!.addView(videoView.root)
                    if (videosGridLayout!!.childCount == videos.size) {
                        videosProgressBar!!.visibility = View.GONE
                        videosGridLayout!!.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
