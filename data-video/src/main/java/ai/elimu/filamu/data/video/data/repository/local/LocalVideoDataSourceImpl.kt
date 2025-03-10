package ai.elimu.filamu.data.video.data.repository.local

import ai.elimu.content_provider.utils.ContentProviderUtil
import ai.elimu.filamu.data.video.BuildConfig
import ai.elimu.model.v2.gson.content.VideoGson
import android.app.Application
import android.content.ContentUris
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class LocalVideoDataSourceImpl @Inject constructor(
    private val context: Application
) : LocalVideoDataSource {

    override suspend fun getVideos(): List<VideoGson> {
        return ContentProviderUtil.getAllVideoGSONs(
            context, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
    }

    override suspend fun readVideoBytes(fileId: Long): ByteArray? {
        val uri = Uri.parse("content://" + BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
                + ".provider.video_provider/videos/")

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
            Timber.tag("readVideoBytes").e("exception: " + e.message)
            return null
        }
    }

    override suspend fun extractFirstFrameFromVideo(videoBytes: ByteArray): Bitmap? {
        try {
            // Step 1: Save the ByteArray to a temporary file
            val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(videoBytes) }

            // Step 2: Use MediaMetadataRetriever to extract the first frame
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(tempFile.absolutePath)

            // Step 3: Get frame at the first millisecond (0 ms)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            // Release resources
            retriever.release()

            // Delete the temp file
            tempFile.delete()

            return bitmap
        } catch (e: Exception) {
            Timber.tag("extractFirstFrameFromVideo")
                .e("exception: " + e.message)
            e.printStackTrace()
        }
        return null
    }
}