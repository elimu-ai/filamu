package ai.elimu.filamu.util

import ai.elimu.filamu.ui.video.model.Subtitle
import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

object SubtitleUtil {

    suspend fun loadSubtitles(assetManager: AssetManager, fileName: String): List<Subtitle> {
        return withContext(Dispatchers.IO) {
            val subtitles = mutableListOf<Subtitle>()
            try {
                val inputStream = assetManager.open(fileName)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val pattern = Pattern.compile("(\\d+)\\n(\\d{2}:\\d{2}:\\d{2},\\d{3}) --> (\\d{2}:\\d{2}:\\d{2},\\d{3})\\n(.*)")

                var line: String?
                val subtitleBuilder = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) {
                        val matcher = pattern.matcher(subtitleBuilder.toString())
                        if (matcher.find()) {
                            val startTime = parseTime(matcher.group(2)!!)
                            val endTime = parseTime(matcher.group(3)!!)
                            val text = matcher.group(4)!!
                            subtitles.add(Subtitle(startTime, endTime, text))
                        }
                        subtitleBuilder.clear()
                    } else {
                        subtitleBuilder.append(line).append("\n")
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error loading subtitles: ${e.message}")
            }
            subtitles
        }
    }

    fun parseTime(time: String): Long {
        val parts = time.split(",", ":").map { it.toInt() }
        return parts[0] * 3600000 + parts[1] * 60000 + parts[2] * 1000 + parts[3]
    }
}