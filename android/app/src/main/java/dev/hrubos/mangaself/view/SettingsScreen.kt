package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.model.ReadingMode
import dev.hrubos.mangaself.model.ThemeStyle
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.ui.components.ReadingModeDropdown
import dev.hrubos.mangaself.ui.components.ThemeDropdown
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
    onThemeChange: (ThemeStyle) -> Unit,
) {
    val currentProfile by profileViewModel.selectedProfile.collectAsState()
    var name by remember { mutableStateOf(currentProfile?.name ?: "") }
    var readingMode by remember { mutableStateOf(currentProfile?.readingMode ?: ReadingMode.LEFTTORIGHT) }
    var showDeleteDialog by remember { mutableStateOf(false) } // dialog state
    val context = LocalContext.current
    val currentTheme by Configuration.themeFlow(context).collectAsState(initial = ThemeStyle.AUTO)

    LaunchedEffect(currentProfile) {
        currentProfile?.let {
            name = it.name
            readingMode = it.readingMode
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        FloatingTopMenu(onBack = onBack, onInfo = onAbout)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Center section (text field + dropdown)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = { newValue ->
                        name = newValue
                        profileViewModel.updateProfileName(newValue)
                    },
                    singleLine = true,
                    label = { Text("Name") },
                )

                ReadingModeDropdown(
                    selectedOption = readingMode.toString(),
                    onChange = { selectedText ->
                        val selectedMode = ReadingMode.entries.first { it.text == selectedText }
                        profileViewModel.updateProfileReadingMode(selectedMode.text)
                    }
                )

                ThemeDropdown(
                    selectedOption = currentTheme.text,
                    onChange = { selected -> onThemeChange(ThemeStyle from selected) }
                )
            }

            // Bottom buttons row
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        profileViewModel.logout {
                            onLogout()
                        }
                    },
                    modifier = Modifier.weight(0.6f)
                ) {
                    Text("Switch profile")
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(0.4f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete profile")
                }
            }
        }

        // Confirm delete popup
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Profile $name") },
                text = { Text("Are you sure you want to delete this profile? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            profileViewModel.deleteProfile {
                                profileViewModel.logout {
                                    onLogout()
                                }
                            }
                        }
                    ) {
                        Text("Yes, delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}