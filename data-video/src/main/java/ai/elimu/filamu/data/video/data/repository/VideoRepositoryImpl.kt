package ai.elimu.filamu.data.video.data.repository

import ai.elimu.filamu.data.video.data.repository.local.LocalVideoDataSourceImpl
import ai.elimu.model.v2.gson.content.VideoGson
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val localDataSource: LocalVideoDataSourceImpl,
): VideoRepository {

    override suspend fun getVideos(): List<VideoGson> {
        return localDataSource.getVideos()
    }
}