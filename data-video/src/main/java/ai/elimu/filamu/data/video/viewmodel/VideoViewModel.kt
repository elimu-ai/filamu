package ai.elimu.filamu.data.video.viewmodel

import ai.elimu.model.v2.gson.content.VideoGson
import android.graphics.Bitmap
import kotlinx.coroutines.flow.StateFlow

/**
 * A sealed hierarchy describing the state of the feed of videos resources.
 */
sealed interface LoadVideosUiState {
    /**
     * The videos are still loading.
     */
    data object Loading : LoadVideosUiState

    /**
     * The videos are loaded with the given list of videos resources.
     */
    data class Success(
        /**
         * The list of videos resources
         */
        val videos: List<VideoGson>,
    ) : LoadVideosUiState
}

interface VideoViewModel {
    val uiState: StateFlow<LoadVideosUiState>
    fun getAllVideos()
    fun getThumb(videoId: Long, onResult: (Bitmap?) -> Unit)
    fun getThumbUrl(video: VideoGson, onResult: (String) -> Unit)
    fun readVideoBytes(fileId: Long, onResult: (ByteArray?) -> Unit)
}