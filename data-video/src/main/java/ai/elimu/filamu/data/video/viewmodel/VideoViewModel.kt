package ai.elimu.filamu.data.video.viewmodel

import ai.elimu.model.v2.gson.content.VideoGson
import android.graphics.Bitmap

interface VideoViewModel {
    fun getAllVideos(onResult: (List<VideoGson>) -> Unit)
    fun getThumb(videoId: Long, onResult: (Bitmap?) -> Unit)
    fun readVideoBytes(fileId: Long, onResult: (ByteArray?) -> Unit)
}