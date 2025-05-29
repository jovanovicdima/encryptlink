package jovanovicdima.encryptlink.screens.server

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jovanovicdima.encryptlink.data.local.server.TCPServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ServerScreen(
    modifier: Modifier = Modifier
) {
    val server: TCPServer = remember { TCPServer() }
    val isServerRunning: Boolean by server.isRunning.collectAsState()

    var serverPort: String by remember { mutableStateOf("") }

    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(
                visible = isServerRunning
            ) {
                Column {
                    Text("Please select encoding algorithm")


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
                value = serverPort,
                enabled = !isServerRunning,
                onValueChange = {
                if (it.length > 5) return@TextField

                val port = it.toIntOrNull()
                if (it != "" && port == null) return@TextField
                if (port != null && (port > 65535 || port == 0)) return@TextField

                serverPort = it
            }, label = {
                Text(text = "Port")
            }, singleLine = true, colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.Transparent,
            ), textStyle = MaterialTheme.typography.titleSmall
            )

            if (isServerRunning) {
                Button(
                    onClick = {
                        server.stop()
                    }) {
                    Text("Stop Server")
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            server.start(port = serverPort.toInt())
                        }
                    }, enabled = serverPort != ""
                ) {
                    Text("Start Server")
                }
            }
        }
    }
}