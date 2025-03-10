package ai.elimu.filamu.data.video.viewmodel

import ai.elimu.model.v2.gson.content.VideoGson

interface VideoViewModel {
    fun getAllVideos(onResult: (List<VideoGson>) -> Unit)
}