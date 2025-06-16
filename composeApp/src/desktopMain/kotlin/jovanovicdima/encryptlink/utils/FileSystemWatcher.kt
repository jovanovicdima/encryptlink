package jovanovicdima.encryptlink.utils

import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileSystemWatcher() {
    private var watcher: DirectoryWatcher? = null
    private var watchJob: Job? = null

    private val _isRunning: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun startWatching(folderPath: String, onNewFile: (Path) -> Unit) {
        val path = Paths.get(folderPath).toAbsolutePath()

        watcher = DirectoryWatcher.builder()
            .path(path)
            .listener { event ->
                when (event.eventType()) {
                    DirectoryChangeEvent.EventType.CREATE -> {
                        val filePath = event.path()
                        if (Files.isRegularFile(filePath)) {
                            println("New file detected: ${filePath.fileName}")
                            onNewFile(filePath)
                        }
                    }
                    else -> null
                }
            }
            .build()

        _isRunning.update { true }
        watchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                watcher?.watchAsync()?.get()
            } catch (e: Exception) {
                _isRunning.update { false }
                println("Error in directory watcher: ${e.message}")
            }
        }

        println("Started watching folder: $folderPath")
    }

    fun stopWatching() {
        try {
            watcher?.close()
            watchJob?.cancel()
            println("Stopped watching folder")
        } catch (e: Exception) {
            println("Error stopping watcher: ${e.message}")
        }
        _isRunning.update { watchJob?.isActive == true }
    }
}