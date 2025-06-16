package jovanovicdima.encryptlink.data.remote.server

import jovanovicdima.encryptlink.utils.SHA1
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

class TCPServer() {
    private var serverSocket: ServerSocket? = null
    private val clients = ConcurrentHashMap<String, ClientHandler>()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var saveDirectory: File? = null

    suspend fun start(port: Int, saveDirectoryPath: String) = withContext(Dispatchers.IO) {
        try {
            saveDirectory = File(saveDirectoryPath)
            if (!saveDirectory!!.isDirectory) {
                throw Exception("Input path must be directory")
            }

            serverSocket = ServerSocket(port)
            _isRunning.update { true }
            println("Server listening on port: $port")

            while (_isRunning.value) {
                try {
                    val clientSocket = serverSocket?.accept()
                    clientSocket?.let { socket ->
                        val clientId = "${socket.inetAddress.hostAddress}:${socket.port}"
                        val handler = ClientHandler(clientId, socket) { clients.remove(it) }
                        clients[clientId] = handler
                        launch { handler.handle() }
                    }
                } catch (e: SocketException) {
                    if (_isRunning.value) println("Socket error: ${e.message}")
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
        private val clientId: String,
        private val socket: Socket,
        private val onDisconnect: (String) -> Unit
    ) {
        private val inputStream = DataInputStream(BufferedInputStream(socket.getInputStream()))
        private var isConnected = true

        suspend fun handle() = withContext(Dispatchers.IO) {
            try {
                while (isConnected && !socket.isClosed) {
                    val fileName = inputStream.readUTF()
                    val fileSize = inputStream.readLong()
                    val hashLength = inputStream.readInt()
                    val expectedHash = ByteArray(hashLength)
                    inputStream.readFully(expectedHash)

                    println("Receiving file: $fileName ($fileSize bytes) from $clientId")

                    val file = File(saveDirectory, fileName)
                    val buffer = ByteArray(8192)
                    val fileOutputStream = FileOutputStream(file)

                    var totalRead = 0L
                    while (totalRead < fileSize) {
                        val remaining = fileSize - totalRead
                        val toRead = minOf(remaining, buffer.size.toLong()).toInt()

                        inputStream.readFully(buffer, 0, toRead)

                        fileOutputStream.write(buffer, 0, toRead)
                        totalRead += toRead

                        val percentage = (totalRead * 100 / fileSize).toInt()
                        println("Receiving file: $percentage% ($totalRead/$fileSize bytes)")
                    }
                    fileOutputStream.close()

                    val receivedBytes = file.readBytes()
                    val actualHash = SHA1.hash(receivedBytes)

                    println(expectedHash)

                    if (actualHash.contentEquals(expectedHash)) {
                        println("✔ File received and verified: $fileName")
                    } else {
                        println("✘ Hash mismatch for file: $fileName")
                        file.delete()
                    }
                }
            } catch (e: EOFException) {
                println("Client $clientId disconnected.")
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
                } catch (_: IOException) {
                    null
                }
                onDisconnect(clientId)
            }
        }
    }
}
