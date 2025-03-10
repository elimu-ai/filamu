package ai.elimu.filamu.data.video.data.repository.local

import ai.elimu.content_provider.utils.ContentProviderUtil
import ai.elimu.filamu.data.video.BuildConfig
import ai.elimu.model.v2.gson.content.VideoGson
import android.app.Application
import javax.inject.Inject

class LocalVideoDataSourceImpl @Inject constructor(
    private val context: Application
) : LocalVideoDataSource {

    override suspend fun getVideos(): List<VideoGson> {
        return ContentProviderUtil.getAllVideoGSONs(
            context, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
    }
}