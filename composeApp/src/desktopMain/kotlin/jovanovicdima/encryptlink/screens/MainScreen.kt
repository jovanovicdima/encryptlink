package jovanovicdima.encryptlink.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jovanovicdima.encryptlink.components.RadioTabs
import jovanovicdima.encryptlink.data.models.EncryptionAlgorithm
import jovanovicdima.encryptlink.data.remote.client.TCPClient
import jovanovicdima.encryptlink.data.remote.server.TCPServer
import jovanovicdima.encryptlink.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileSystemView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var isAlgorithmDropdownExpanded: Boolean by remember { mutableStateOf(false) }
    var selectedAlgorithm: EncryptionAlgorithm? by remember { mutableStateOf(null) }
    var algorithmName: String by remember { mutableStateOf("Select algorithm") }

    var key: String by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    var outputFolderPath: String? by remember { mutableStateOf(null) }
    var selectedFilePath: String? by remember { mutableStateOf(null) }

    var ivRC6: String? by remember { mutableStateOf(null) }

    var isNoError: Boolean by remember { mutableStateOf(true) }

    val fileSystemWatcher = FileSystemWatcher()
    val isFileSystemWatcherActive: Boolean by fileSystemWatcher.isRunning.collectAsStateWithLifecycle()
    var inputFolderPath: String? by remember { mutableStateOf(null) }

    var selectedTab: String by remember { mutableStateOf("Server") }

    val server: TCPServer = remember { TCPServer() }
    val isServerRunning: Boolean by server.isRunning.collectAsState()
    var serverPort: String by remember { mutableStateOf("") }
    var serverInputFolderPath: String? by remember { mutableStateOf(null) }

    val client: TCPClient = remember { TCPClient() }
    val isClientConnected: Boolean by client.isConnected.collectAsState()
    val progress: Float? by client.progress.collectAsState()
    var isClientSending: Boolean by remember { mutableStateOf(false) }

    var clientTargetHost: String by remember { mutableStateOf("") }
    var clientTargetPort: String by remember { mutableStateOf("") }

    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedAlgorithm, key, outputFolderPath, ivRC6) {
        isNoError = selectedAlgorithm != null && outputFolderPath != null && when (selectedAlgorithm) {
            EncryptionAlgorithm.RC6 -> ivRC6?.length == 16 && key.length == 16
            EncryptionAlgorithm.Bifid -> true
            else -> false
        }
    }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            })
    }

    LaunchedEffect(selectedAlgorithm) {
        algorithmName = when (selectedAlgorithm) {
            EncryptionAlgorithm.Bifid -> {
                ivRC6 = null
                "Algorithm: Bifid"
            }

            EncryptionAlgorithm.RC6 -> {
                "Algorithm: RC6 (OFB Mode)"
            }

            else -> {
                ivRC6 = null
                "Select algorithm"
            }
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).clickable(
            indication = null, interactionSource = interactionSource
        ) { focusManager.clearFocus() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EncryptLink",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = Modifier.fillMaxWidth().background(
                color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp)
            ).border(
                border = BorderStroke(
                    width = 2.dp, color = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(20.dp)
            ).padding(12.dp)
        ) {
            Text(
                text = "Settings",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth().clickable {},
                expanded = isAlgorithmDropdownExpanded,
                onExpandedChange = {
                    isAlgorithmDropdownExpanded = !isAlgorithmDropdownExpanded
                }) {
                TextField(
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .pointerHoverIcon(PointerIcon.Hand, true),
                    value = algorithmName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAlgorithmDropdownExpanded) },
                    enabled = false,
                    colors = TextFieldDefaults.colors().copy(
                        disabledContainerColor = Color.Transparent,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledIndicatorColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = isAlgorithmDropdownExpanded,
                    onDismissRequest = { isAlgorithmDropdownExpanded = false }) {
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), text = {
                        Text(
                            text = "Bifid", style = MaterialTheme.typography.bodyMedium
                        )
                    }, onClick = {
                        selectedAlgorithm = EncryptionAlgorithm.Bifid
                        isAlgorithmDropdownExpanded = false
                    }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )

                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), text = {
                        Text(
                            text = "RC6 (OFB Mode)", style = MaterialTheme.typography.bodyMedium
                        )
                    }, onClick = {
                        selectedAlgorithm = EncryptionAlgorithm.RC6
                        isAlgorithmDropdownExpanded = false
                    }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }

            TextField(
                modifier = Modifier.fillMaxWidth(), value = key, onValueChange = { key = it }, label = {
                Text(
                    text = when (selectedAlgorithm) {
                        EncryptionAlgorithm.RC6 -> "Key (16 characters)"
                        EncryptionAlgorithm.Bifid -> "Key (default: \"ABCDEFGHIKLMNOPQRSTUVWXYZ\")"
                        else -> "Key"
                    }, style = MaterialTheme.typography.bodyMedium
                )
            }, colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ), textStyle = MaterialTheme.typography.bodyMedium
            )

            AnimatedVisibility(selectedAlgorithm == EncryptionAlgorithm.RC6) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = ivRC6 ?: "",
                    onValueChange = { ivRC6 = it },
                    label = { Text(text = "IV for RC6 (16 characters)", style = MaterialTheme.typography.bodyMedium) },
                    colors = TextFieldDefaults.colors().copy(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).clickable {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                    val chooser = JFileChooser(FileSystemView.getFileSystemView())
                    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    chooser.dialogTitle = "Select a Folder"
                    chooser.isAcceptAllFileFilterUsed = false

                    val result = chooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        outputFolderPath = chooser.selectedFile.path
                    }
                }) {
                TextField(
                    value = if (outputFolderPath != null) "Output Path: $outputFolderPath" else "Select Output Path",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors().copy(
                        disabledContainerColor = Color.Transparent,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledIndicatorColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).clickable {
                    val fileDialog = FileDialog(Frame(), "")
                    fileDialog.isVisible = true
                    val filename = fileDialog.file
                    val directory = fileDialog.directory
                    fileDialog.dispose()

                    if (filename != null && directory != null) {
                        selectedFilePath = "$directory/$filename"
                    }
                }) {
                TextField(
                    value = if (selectedFilePath != null) "File: $selectedFilePath" else "Select File For Encryption",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors().copy(
                        disabledContainerColor = Color.Transparent,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledIndicatorColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        try {
                            if (selectedAlgorithm == EncryptionAlgorithm.Bifid) {
                                encryptFileBifid(
                                    outputFolderPath = outputFolderPath!!,
                                    encryptionFilePath = selectedFilePath!!,
                                    key = key
                                )
                            } else if (selectedAlgorithm == EncryptionAlgorithm.RC6) {
                                encryptFileRC6(
                                    outputFolderPath = outputFolderPath!!,
                                    encryptionFilePath = selectedFilePath!!,
                                    key = key,
                                    iv = ivRC6!!
                                )
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unknown error"
                            showErrorDialog = true
                        }
                    }, enabled = isNoError && selectedFilePath != null
                ) {
                    Text(
                        text = "Encrypt File", style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        try {
                            if (selectedAlgorithm == EncryptionAlgorithm.Bifid) {
                                decryptFileBifid(
                                    outputFolderPath = outputFolderPath!!,
                                    encryptionFilePath = selectedFilePath!!,
                                    key = key
                                )
                            } else if (selectedAlgorithm == EncryptionAlgorithm.RC6) {
                                decryptFileRC6(
                                    outputFolderPath = outputFolderPath!!,
                                    encryptionFilePath = selectedFilePath!!,
                                    key = key,
                                    iv = ivRC6!!
                                )
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unknown error"
                            showErrorDialog = true
                        }
                    }, enabled = isNoError && selectedFilePath != null
                ) {
                    Text(text = "Decrypt File", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        AnimatedVisibility(selectedAlgorithm != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                modifier = Modifier.fillMaxWidth().background(
                    color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp)
                ).border(
                    border = BorderStroke(
                        width = 2.dp, color = MaterialTheme.colorScheme.primary
                    ), shape = RoundedCornerShape(20.dp)
                ).padding(12.dp)
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "File System Watcher",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).clickable {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                        val chooser = JFileChooser(FileSystemView.getFileSystemView())
                        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                        chooser.dialogTitle = "Select a Folder"
                        chooser.isAcceptAllFileFilterUsed = false

                        val result = chooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            inputFolderPath = chooser.selectedFile.path
                        }
                    }) {
                    TextField(
                        value = if (inputFolderPath != null) "Input Path: $inputFolderPath" else "Select Input Path",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors().copy(
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledIndicatorColor = Color.Transparent,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true), onClick = {
                        try {
                            if (!isNoError) {
                                throw Exception("Please configure the settings before using File System Watcher")
                            }

                            if (!isFileSystemWatcherActive) {
                                fileSystemWatcher.startWatching(inputFolderPath!!) { path ->
                                    try {
                                        println(path.toString())
                                        if (selectedAlgorithm == EncryptionAlgorithm.Bifid) {
                                            encryptFileBifid(
                                                outputFolderPath = outputFolderPath!!,
                                                encryptionFilePath = path.toString(),
                                                key = key
                                            )
                                        } else if (selectedAlgorithm == EncryptionAlgorithm.RC6) {
                                            encryptFileRC6(
                                                outputFolderPath = outputFolderPath!!,
                                                encryptionFilePath = path.toString(),
                                                key = key,
                                                iv = ivRC6!!
                                            )
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Unknown error"
                                        showErrorDialog = true
                                    }
                                }
                            } else {
                                fileSystemWatcher.stopWatching()
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unknown error"
                            showErrorDialog = true
                        }
                    }, enabled = inputFolderPath != null, border = BorderStroke(
                        width = 2.dp, color = if (isFileSystemWatcherActive) Color.Green else Color.Red
                    ), colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (isFileSystemWatcherActive) "Active" else "Not Active",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        AnimatedVisibility(selectedAlgorithm != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                modifier = Modifier.fillMaxWidth().background(
                    color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp)
                ).border(
                    border = BorderStroke(
                        width = 2.dp, color = MaterialTheme.colorScheme.primary
                    ), shape = RoundedCornerShape(20.dp)
                ).padding(12.dp)
            ) {
                RadioTabs(
                    modifier = Modifier.fillMaxWidth(),
                    tabs = listOf("Server", "Client"),
                    onTabSelected = {
                        selectedTab = it

                        if (selectedTab == "Server") {
                            client.disconnect()
                        } else if (selectedTab == "Client") {
                            server.stop()
                        }
                    },
                    selectedTab = selectedTab,
                    selectedTabTextColor = MaterialTheme.colorScheme.background,
                )

                if (selectedTab == "Server") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).clickable {
                                if (isServerRunning) {
                                    return@clickable
                                }

                                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                                val chooser = JFileChooser(FileSystemView.getFileSystemView())
                                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                chooser.dialogTitle = "Select a Folder"
                                chooser.isAcceptAllFileFilterUsed = false

                                val result = chooser.showOpenDialog(null)
                                if (result == JFileChooser.APPROVE_OPTION) {
                                    serverInputFolderPath = chooser.selectedFile.path
                                }
                            }) {
                            TextField(
                                value = if (serverInputFolderPath != null) "Output Path: $serverInputFolderPath" else "Select Output Path",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = TextFieldDefaults.colors().copy(
                                    disabledContainerColor = Color.Transparent,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledIndicatorColor = Color.Transparent,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium

                            )
                        }

                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = serverPort,
                            enabled = !isServerRunning,
                            onValueChange = {
                                if (it.length > 5) return@TextField

                                val port = it.toIntOrNull()
                                if (it != "" && port == null) return@TextField
                                if (port != null && (port > 65535 || port == 0)) return@TextField

                                serverPort = it
                            },
                            singleLine = true,
                            label = {
                                Text(
                                    text = "Port", style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = Color.Transparent,
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true), onClick = {
                                try {
                                    if (isServerRunning) {
                                        server.stop()
                                    } else {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            server.start(port = serverPort.toInt(), serverInputFolderPath!!)
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Unknown error"
                                    showErrorDialog = true
                                }
                            }, enabled = serverInputFolderPath != null && serverPort.isNotBlank()
                        ) {
                            Text(
                                text = if (isServerRunning) "Stop Server" else "Start Server",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = clientTargetHost,
                            enabled = !isClientConnected,
                            onValueChange = {
                                clientTargetHost = it
                            },
                            label = {
                                Text(text = "Host", style = MaterialTheme.typography.bodyMedium)
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = Color.Transparent,
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = clientTargetPort,
                            enabled = !isClientConnected,
                            onValueChange = {
                                if (it.length > 5) return@TextField

                                val port = it.toIntOrNull()
                                if (it != "" && port == null) return@TextField
                                if (port != null && (port > 65535 || port == 0)) return@TextField

                                clientTargetPort = it
                            },
                            label = {
                                Text(text = "Port", style = MaterialTheme.typography.bodyMedium)
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = Color.Transparent,
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand, true), onClick = {
                                try {
                                    if (isClientConnected) {
                                        client.disconnect()
                                    } else {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            client.connect(host = clientTargetHost, port = clientTargetPort.toInt())
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Unknown error"
                                    showErrorDialog = true
                                }
                            }, enabled = clientTargetPort.isNotBlank() && clientTargetHost.isNotBlank()
                        ) {
                            Text(
                                text = if (isClientConnected) "Disconnect" else "Connect",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    AnimatedVisibility(isClientConnected) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { progress ?: 0f },
                                modifier = Modifier.fillMaxWidth(),
                                gapSize = (-15).dp,
                                drawStopIndicator = {},
                                trackColor = Color.Gray,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            if (!(selectedAlgorithm != null && (selectedAlgorithm != EncryptionAlgorithm.RC6 || (ivRC6?.length == 16 && key.length == 16)))) {
                                                throw Exception("Please configure the settings before sending files.")
                                            }

                                            val diag = FileDialog(Frame(), "")
                                            diag.isVisible = true
                                            val filename = diag.file
                                            val directory = diag.directory
                                            diag.dispose()

                                            isClientSending = true

                                            if (filename != null && directory != null) {
                                                val file = File(directory, filename)
                                                when (selectedAlgorithm) {
                                                    EncryptionAlgorithm.Bifid -> {
                                                        if (file.extension != "txt") {
                                                            throw Exception("Only .txt files are allowed.")
                                                        }

                                                        val text = file.readText()
                                                        val encryptedText = encryptDataBifid(
                                                            data = text, key = key
                                                        )

                                                        val newFileName =
                                                            "${file.nameWithoutExtension}-BifidEncrypted.${file.extension}"

                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            client.sendFile(
                                                                encryptedText.toByteArray(), newFileName
                                                            )
                                                        }
                                                    }

                                                    EncryptionAlgorithm.RC6 -> {
                                                        val data = file.readBytes()
                                                        val encryptedData = encryptDataRC6(
                                                            data = data, key = key, iv = ivRC6!!.toByteArray()
                                                        )

                                                        val newFileName =
                                                            "${file.nameWithoutExtension}-RC6Encrypted.${file.extension}"

                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            client.sendFile(encryptedData, newFileName, onFinishCallback = {
                                                                isClientSending = false
                                                            })
                                                        }
                                                    }

                                                    else -> {
                                                        throw Exception("Please select algorithm")
                                                    }
                                                }
                                            } else {
                                                println("No file selected")
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Unknown error"
                                            showErrorDialog = true
                                        }
                                    }
                                }, enabled = isClientConnected && progress == null && !isClientSending
                            ) {
                                if (progress == null && !isClientSending) {
                                    Text("Select a file to encrypt and send")
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeWidth = 1.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}