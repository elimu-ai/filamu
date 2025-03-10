package ai.elimu.filamu.util

import ai.elimu.filamu.BuildConfig
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@WorkerThread
suspend fun Context.readVideoBytes(fileId: Long): ByteArray? {
    val uri = Uri.parse("content://" + BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
            + ".provider.video_provider/videos/")

    val videoUri = ContentUris.withAppendedId(uri, fileId)

    try {
        contentResolver.openInputStream(videoUri).use { inputStream ->
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
        Log.e("readVideoBytes", "exception: " + e.message)
        return null
    }
}

fun Context.extractFirstFrameFromVideo(videoBytes: ByteArray): Bitmap? {
    try {
        // Step 1: Save the ByteArray to a temporary file
        val tempFile = File.createTempFile("temp_video", ".mp4", this.cacheDir)
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
        Log.e("tuancoltech", "extractFirstFrameFromVideo exception: " + e.message)
        e.printStackTrace()
    }
    return null
}