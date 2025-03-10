package ai.elimu.filamu.data.video.data.repository.local

import ai.elimu.model.v2.gson.content.VideoGson

interface LocalVideoDataSource {
    suspend fun getVideos(): List<VideoGson>
}