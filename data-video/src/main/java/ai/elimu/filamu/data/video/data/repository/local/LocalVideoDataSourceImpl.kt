package ai.elimu.filamu.data.video.data.repository.local

import ai.elimu.content_provider.utils.ContentProviderUtil
import ai.elimu.filamu.data.video.BuildConfig
import ai.elimu.model.v2.gson.content.VideoGson
import android.app.Application
import android.content.ContentUris
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import androidx.core.net.toUri

class LocalVideoDataSourceImpl @Inject constructor(
    private val context: Application
) : LocalVideoDataSource {

    override suspend fun getVideos(): List<VideoGson> {
        return ContentProviderUtil.getAllVideoGSONs(
            context, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
    }

    override suspend fun readVideoBytes(fileId: Long): ByteArray? {
        val uri = ("content://" + BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
                + ".provider.video_provider/videos/").toUri()

        val videoUri = ContentUris.withAppendedId(uri, fileId)

        try {
            context.contentResolver.openInputStream(videoUri).use { inputStream ->
                ByteArrayOutputStream().use { byteBuffer ->
                    if (inputStream == null) return null
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                        byteBuffer.write(buffer, 0, bytesRead)
                    }
                    return byteBuffer.toByteArray()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.tag("readVideoBytes").e("exception: %s", e.message)
            return null
        }
    }

    override suspend fun extractFirstFrameFromVideo(videoBytes: ByteArray): Bitmap? {
        var tempFile: File? = null
        return try {
            tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(videoBytes) }
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(tempFile.absolutePath)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            Timber.tag("extractFirstFrameFromVideo").e("exception: %s", e.message)
            e.printStackTrace()
            null
        } finally {
            tempFile?.delete()
        }
    }
}