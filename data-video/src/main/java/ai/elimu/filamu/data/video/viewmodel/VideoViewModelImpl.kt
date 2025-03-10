package ai.elimu.filamu.data.video.viewmodel

import ai.elimu.filamu.data.video.data.repository.VideoRepository
import ai.elimu.filamu.data.video.di.IoScope
import ai.elimu.model.v2.gson.content.VideoGson
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoViewModelImpl @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val videoRepository: VideoRepository
): ViewModel(), VideoViewModel {

    override fun getAllVideos(onResult: (List<VideoGson>) -> Unit) {
        ioScope.launch {
            val videos = videoRepository.getVideos()
            withContext(Dispatchers.Main) {
                onResult(videos)
            }
        }
    }
}