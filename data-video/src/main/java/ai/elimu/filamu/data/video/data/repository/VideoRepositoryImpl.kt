package ai.elimu.filamu.data.video.data.repository

import ai.elimu.filamu.data.video.data.repository.local.LocalVideoDataSource
import ai.elimu.model.v2.gson.content.VideoGson
import android.graphics.Bitmap
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val localDataSource: LocalVideoDataSource,
): VideoRepository {

    override suspend fun getVideos(): List<VideoGson> {
        return localDataSource.getVideos()
    }

    override suspend fun readVideoBytes(fileId: Long): ByteArray? {
        return localDataSource.readVideoBytes(fileId)
    }

    override suspend fun extractFirstFrameFromVideo(videoBytes: ByteArray): Bitmap? {
        return localDataSource.extractFirstFrameFromVideo(videoBytes)
    }
}