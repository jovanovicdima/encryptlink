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
import jovanovicdima.encryptlink.data.models.EncryptionAlgorithm
import jovanovicdima.encryptlink.utils.decryptFileRC6
import jovanovicdima.encryptlink.utils.encryptFileRC6
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
    var algorithmSelected: EncryptionAlgorithm? by remember { mutableStateOf(null) }
    var algorithmName: String by remember { mutableStateOf("Select algorithm") }

    var key: String by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    var outputFolderPath: String? by remember { mutableStateOf(null) }
    var encryptionFilePath: String? by remember { mutableStateOf(null) }

    var ivRC6: String? by remember { mutableStateOf(null) }

    LaunchedEffect(algorithmSelected) {
        algorithmName = when (algorithmSelected) {
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
                style = MaterialTheme.typography.titleLarge,
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
                    )
                )
                ExposedDropdownMenu(
                    expanded = isAlgorithmDropdownExpanded,
                    onDismissRequest = { isAlgorithmDropdownExpanded = false }) {
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        text = { Text("Bifid") },
                        onClick = {
                            algorithmSelected = EncryptionAlgorithm.Bifid
                            isAlgorithmDropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )

                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        text = { Text("RC6 (OFB Mode)") },
                        onClick = {
                            algorithmSelected = EncryptionAlgorithm.RC6
                            isAlgorithmDropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = key,
                onValueChange = { key = it },
                label = { Text("Key") })

            AnimatedVisibility(algorithmSelected == EncryptionAlgorithm.RC6) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = ivRC6 ?: "",
                    onValueChange = { ivRC6 = it },
                    label = { Text("Plain text for RC6") })
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
                    enabled = false
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand, true).clickable {
                    val diag = FileDialog(Frame(), "")
                    diag.isVisible = true
                    val filename = diag.file
                    val directory = diag.directory
                    diag.dispose()

                    if (filename != null && directory != null) {
                        encryptionFilePath = "$directory/$filename"
                    }
                }) {
                TextField(
                    value = if (encryptionFilePath != null) "File: $encryptionFilePath" else "Select File For Encryption",
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
                    enabled = false
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        encryptFileRC6(outputFolderPath = outputFolderPath!!, encryptionFilePath = encryptionFilePath!!, key = key, iv = ivRC6!!)
                    },
                    enabled = outputFolderPath != null && encryptionFilePath != null && (algorithmSelected != EncryptionAlgorithm.RC6 || !ivRC6.isNullOrBlank())
                ) {
                    Text("Encrypt File")
                }

                Button(
                    onClick = {
                        decryptFileRC6(outputFolderPath = outputFolderPath!!, encryptionFilePath = encryptionFilePath!!, key = key, iv = ivRC6!!)
                    },
                    enabled = outputFolderPath != null && encryptionFilePath != null && (algorithmSelected != EncryptionAlgorithm.RC6 || !ivRC6.isNullOrBlank())
                ) {
                    Text("Decrypt File")
                }
            }
        }
    }
}