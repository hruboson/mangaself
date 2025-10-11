package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.model.ReadingMode
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.ui.components.ReadingModeDropdown
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
) {
    val currentProfile by profileViewModel.selectedProfile.collectAsState()
    var name by remember { mutableStateOf(currentProfile?.name ?: "") }
    var readingMode by remember { mutableStateOf(currentProfile?.readingMode ?: ReadingMode.LEFTTORIGHT) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = name,
                onValueChange = { newValue ->
                    name = newValue
                    profileViewModel.updateProfileName(newValue)
                },
                singleLine = true,
                label = { Text("Name") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReadingModeDropdown(
                selectedOption = readingMode.toString(),
                onChange = {
                    selectedText ->  val selectedMode = ReadingMode.entries.first { it.text == selectedText }
                    profileViewModel.updateProfileReadingMode(selectedMode.text) // this is not optimal to store the whole string in db but whatever :-)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    profileViewModel.logout {
                        onLogout() // navigate back to EntryScreen
                    }
                }
            ) {
                Text(
                    text = "Switch profile"
                )
            }
        }
    }
}