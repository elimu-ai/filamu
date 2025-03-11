package ai.elimu.filamu.data.video.viewmodel

import ai.elimu.filamu.data.video.data.repository.VideoRepository
import ai.elimu.filamu.data.video.di.IoScope
import ai.elimu.model.v2.gson.content.VideoGson
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoViewModelImpl @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val videoRepository: VideoRepository
): ViewModel(), VideoViewModel {

    private val thumbBaseUrl: String by lazy {
        "http://tha.elimu.ai/video/"
    }

    private val _uiState = MutableStateFlow<LoadVideosUiState>(LoadVideosUiState.Loading)
    override val uiState: StateFlow<LoadVideosUiState> = _uiState.asStateFlow()

    override fun getAllVideos() {
        ioScope.launch {
            _uiState.emit(LoadVideosUiState.Loading)
            val videos = videoRepository.getVideos()
            _uiState.emit(LoadVideosUiState.Success(videos))
        }
    }

    override fun getThumb(videoId: Long, onResult: (Bitmap?) -> Unit) {
        ioScope.launch {
            val videoBytes = videoRepository.readVideoBytes(videoId)
            val thumb = videoBytes?.let {
                videoRepository.extractFirstFrameFromVideo(it)
            } ?: let {
                null
            }
            withContext(Dispatchers.Main) {
                onResult.invoke(thumb)
            }
        }
    }

    override fun getThumbUrl(video: VideoGson, onResult: (String) -> Unit) {
        ioScope.launch {
            val thumbUrlBuilder = StringBuilder()
            thumbUrlBuilder.append(thumbBaseUrl)
                .append(video.id).append("_")
                .append("r").append(video.revisionNumber)
                .append("_thumbnail.png")
            withContext(Dispatchers.Main) {
                onResult.invoke(thumbUrlBuilder.toString())
            }
        }
    }

    override fun readVideoBytes(fileId: Long, onResult: (ByteArray?) -> Unit) {
        ioScope.launch {
            val bytes = videoRepository.readVideoBytes(fileId)
            withContext(Dispatchers.Main) {
                onResult.invoke(bytes)
            }
        }
    }
}