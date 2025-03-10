package ai.elimu.filamu.data

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource

@UnstableApi
class ByteArrayDataSourceFactory(private val byteArray: ByteArray) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return ByteArrayDataSource(byteArray)
    }
}