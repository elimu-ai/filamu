package ai.elimu.filamu.data

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.ByteArrayInputStream
import java.io.InputStream

@UnstableApi
class ByteArrayDataSource(private val byteArray: ByteArray) : DataSource {
    private var inputStream: InputStream? = null
    private var readBytes = 0L

    override fun open(dataSpec: DataSpec): Long {
        inputStream = ByteArrayInputStream(byteArray)
        readBytes = 0L
        return byteArray.size.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val bytesRead = inputStream?.read(buffer, offset, length) ?: -1
        if (bytesRead > 0) {
            readBytes += bytesRead
        }
        return bytesRead
    }

    override fun addTransferListener(transferListener: TransferListener) {
    }

    override fun getUri(): Uri? = null

    override fun close() {
        inputStream?.close()
        inputStream = null
    }
}