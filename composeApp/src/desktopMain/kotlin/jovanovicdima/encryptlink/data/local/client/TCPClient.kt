package jovanovicdima.encryptlink.data.local.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

typealias ProgressCallback = (bytesTransferred: Long, totalBytes: Long) -> Unit

class TCPClient() {
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _progress: MutableStateFlow<Float?> = MutableStateFlow(null)
    val progress: StateFlow<Float?> = _progress.asStateFlow()

    suspend fun connect(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = Socket(host, port)
            outputStream = DataOutputStream(BufferedOutputStream(socket!!.getOutputStream()))
            _isConnected.update { true }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendData(
        data: ByteArray, chunkSize: Int = 8192, progressCallback: ProgressCallback? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!_isConnected.value || outputStream == null) return@withContext false

            val totalSize = data.size.toLong()

            synchronized(outputStream!!) {
                // Send total size first
                outputStream!!.writeLong(totalSize)

                // Send data in chunks with progress tracking
                var bytesSent = 0L
                var offset = 0

                while (offset < data.size) {
                    val currentChunkSize = minOf(chunkSize, data.size - offset)
                    outputStream!!.write(data, offset, currentChunkSize)

                    offset += currentChunkSize
                    bytesSent += currentChunkSize

                    progressCallback?.invoke(bytesSent, totalSize)
                    _progress.update { totalSize.toFloat() / bytesSent.toFloat() }
                }

                outputStream!!.flush()
                _progress.update { null }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        if (_isConnected.value) {
            _isConnected.update { false }
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }

    fun isConnected(): Boolean = _isConnected.value && socket?.isConnected == true && socket?.isClosed == false
}