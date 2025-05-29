package jovanovicdima.encryptlink.data.local.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

class TCPServer() {
    private var serverSocket: ServerSocket? = null
    private val clients = ConcurrentHashMap<String, ClientHandler>()

    private val _isRunning: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    var onDataReceived: ((clientId: String, data: ByteArray) -> Unit)? = null

    suspend fun start(port: Int) = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(port)
            _isRunning.update { true }

            println("Server listening on port: $port")

            while (_isRunning.value) {
                try {
                    val clientSocket = serverSocket?.accept()
                    clientSocket?.let { socket ->
                        val clientId = "${socket.inetAddress.hostAddress}:${socket.port}"
                        val clientHandler = ClientHandler(clientId, socket) { id ->
                            clients.remove(id)
                        }
                        clients[clientId] = clientHandler

                        launch {
                            clientHandler.handle()
                        }
                    }
                } catch (e: SocketException) {
                    if (_isRunning.value) {
                        println("Socket error: ${e.message}")
                    }
                } catch (e: Exception) {
                    println("Error accepting client: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Server error: ${e.message}")
        }
    }

    fun stop() {
        _isRunning.update { false }
        clients.values.forEach { it.disconnect() }
        clients.clear()
        serverSocket?.close()
    }

    inner class ClientHandler(
        private val clientId: String, private val socket: Socket, private val onDisconnect: (String) -> Unit
    ) {
        private val inputStream = DataInputStream(BufferedInputStream(socket.getInputStream()))
        private var isConnected = true

        suspend fun handle() = withContext(Dispatchers.IO) {
            try {
                while (isConnected && !socket.isClosed) {
                        val dataSize = inputStream.readLong()
                        println("Receiving data from $clientId: $dataSize bytes")

                        val chunkSize = 8192 // 8KB chunks, adjust as needed
                        var totalBytesRead = 0L

                        while (totalBytesRead < dataSize) {
                            val remainingBytes = (dataSize - totalBytesRead).toInt()
                            val currentChunkSize = minOf(chunkSize, remainingBytes)

                            val chunk = ByteArray(currentChunkSize)
                            inputStream.readFully(chunk)

                            totalBytesRead += currentChunkSize

                            // Process chunk immediately instead of storing everything
                            onDataReceived?.invoke(clientId, chunk)

                            // Optional: Add a small delay to prevent overwhelming the receiver
                            // yield()
//                            println("Received data from $clientId: ${chunk.contentToString()}")

                        }
                }
            } catch (e: EOFException) {
                // Client disconnected
            } catch (e: IOException) {
                println("Client $clientId error: ${e.message}")
            } finally {
                disconnect()
            }
        }

        fun disconnect() {
            if (isConnected) {
                isConnected = false
                try {
                    inputStream.close()
                    socket.close()
                } catch (e: IOException) {
                    // Ignore
                }
                onDisconnect(clientId)
            }
        }
    }
}