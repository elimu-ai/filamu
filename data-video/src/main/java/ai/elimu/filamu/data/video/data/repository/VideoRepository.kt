package ai.elimu.filamu.data.video.data.repository

import ai.elimu.model.v2.gson.content.VideoGson

interface VideoRepository {
    suspend fun getVideos(): List<VideoGson>
}