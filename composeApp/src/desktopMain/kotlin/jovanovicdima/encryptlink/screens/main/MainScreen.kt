package jovanovicdima.encryptlink.screens.main

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
import jovanovicdima.encryptlink.data.models.EncryptionAlgorithm
import jovanovicdima.encryptlink.utils.*
import java.awt.FileDialog
import java.awt.Frame
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

    LaunchedEffect(selectedAlgorithm, key, outputFolderPath, selectedAlgorithm, ivRC6) {
        isNoError =
            selectedAlgorithm != null && outputFolderPath != null && (selectedAlgorithm != EncryptionAlgorithm.RC6 || (ivRC6?.length == 16 && key.length == 16))
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
    }
}