package ai.elimu.filamu.data.video.data.repository

import ai.elimu.model.v2.gson.content.VideoGson
import android.graphics.Bitmap

interface VideoRepository {
    suspend fun getVideos(): List<VideoGson>
    suspend fun readVideoBytes(fileId: Long): ByteArray?
    suspend fun extractFirstFrameFromVideo(videoBytes: ByteArray): Bitmap?
}