package jovanovicdima.encryptlink.screens.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import jovanovicdima.encryptlink.data.local.client.TCPClient
import jovanovicdima.encryptlink.data.models.EncryptionAlgorithm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Cursor
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ClientScreen(
    modifier: Modifier = Modifier
) {
    val client: TCPClient = remember { TCPClient() }
    val isClientConnected: Boolean by client.isConnected.collectAsState()
    val progress: Float? by client.progress.collectAsState()

    var serverHost: String by remember { mutableStateOf("") }
    var serverPort: String by remember { mutableStateOf("") }

    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var selectedAlgorithm: EncryptionAlgorithm? by remember { mutableStateOf(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(
                visible = isClientConnected
//                visible = true
            ) {
                Row() {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Please select encoding algorithm")

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    modifier = Modifier.size(20.dp).minimumInteractiveComponentSize()
                                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                                    checked = selectedAlgorithm == EncryptionAlgorithm.Bifid,
                                    onCheckedChange = {
                                        selectedAlgorithm = EncryptionAlgorithm.Bifid
                                    })
                                Text("Bifid")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    modifier = Modifier.size(20.dp).minimumInteractiveComponentSize()
                                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                                    checked = selectedAlgorithm == EncryptionAlgorithm.RC6,
                                    onCheckedChange = {
                                        selectedAlgorithm = EncryptionAlgorithm.RC6
                                    },
                                    enabled = false
                                )
                                Text("RC6")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
//                                Checkbox(
//                                    modifier = Modifier.size(20.dp).minimumInteractiveComponentSize()
//                                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
//                                    checked = selectedAlgorithm == EncryptionAlgorithm.OFB,
//                                    onCheckedChange = {
//                                        selectedAlgorithm = EncryptionAlgorithm.OFB
//                                    },
//                                    enabled = false,
//                                )
                                Text("OFB")
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    val diag = FileDialog(Frame(), "")
                                    diag.isVisible = true
                                    val filename = diag.file
                                    val directory = diag.directory
                                    diag.dispose()

                                    if (filename != null && directory != null) {
                                        try {
                                            val file = File(directory, filename)
                                            val byteArray = file.readBytes()
                                            println("File size: ${byteArray.size} bytes")
                                            // Use your byteArray here
                                            GlobalScope.launch(Dispatchers.IO) {
                                                client.sendData(byteArray)
                                            }

                                        } catch (e: Exception) {
                                            println("Error reading file: ${e.message}")
                                        }
                                    } else {
                                        println("No file selected")
                                    }
                                }) {
                                Text("Select a file to send")
                            }
                        }
                    }

                    Column {
                        if (progress != null) {
                            LinearProgressIndicator(
                                progress = { progress ?: 0f }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.End)
        ) {
            TextField(
                modifier = Modifier.widthIn(min = 8.dp),
                value = serverHost,
                enabled = !isClientConnected,
                onValueChange = {

                    serverHost = it
                },
                label = {
                    Text(text = "Host")
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.titleSmall
            )

            TextField(
                modifier = Modifier.widthIn(min = 8.dp),
                value = serverPort,
                enabled = !isClientConnected,
                onValueChange = {
                    if (it.length > 5) return@TextField

                    val port = it.toIntOrNull()
                    if (it != "" && port == null) return@TextField
                    if (port != null && (port > 65535 || port == 0)) return@TextField

                    serverPort = it
                },
                label = {
                    Text(text = "Port")
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.titleSmall
            )

            if (isClientConnected) {
                Button(
                    onClick = {
                        client.disconnect()
                    }) {
                    Text("Disconnect")
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            client.connect(host = serverHost, port = serverPort.toInt())
                        }
                    }, enabled = serverPort != ""
                ) {
                    Text("Connect")
                }
            }
        }
    }
}