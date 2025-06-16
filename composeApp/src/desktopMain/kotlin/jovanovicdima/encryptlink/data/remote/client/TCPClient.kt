package jovanovicdima.encryptlink.data.remote.client

import jovanovicdima.encryptlink.utils.SHA1
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

class TCPClient() {
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _progress = MutableStateFlow<Float?>(null)
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

    suspend fun sendFile(
        data: ByteArray, fileName: String, chunkSize: Int = 8192, onFinishCallback: () -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        if (!_isConnected.value || outputStream == null) return@withContext false

        try {
            val hashBytes = SHA1.hash(data)
            val hashLength = hashBytes.size

            val fileSize = data.size.toLong()

            synchronized(outputStream!!) {
                outputStream!!.writeUTF(fileName)
                outputStream!!.writeLong(fileSize)
                outputStream!!.writeInt(hashLength)
                outputStream!!.write(hashBytes)

                var bytesSent = 0L
                var offset = 0

                while (offset < data.size) {
                    val currentChunkSize = minOf(chunkSize, data.size - offset)
                    println(currentChunkSize)
                    outputStream!!.write(data, offset, currentChunkSize)
                    bytesSent += currentChunkSize
                    offset += currentChunkSize

                    _progress.update { bytesSent.toFloat() / fileSize.toFloat() }
                }

                outputStream!!.flush()
                _progress.update { null }
                onFinishCallback()
            }

            println("✔ File sent: $fileName")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disconnect() {
        if (_isConnected.value) {
            _isConnected.update { false }
            try {
                outputStream?.close()
                socket?.close()
            } catch (_: IOException) {
                null
            }
        }
    }

    fun isConnected(): Boolean = _isConnected.value && socket?.isConnected == true && socket?.isClosed == false
}
