package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

@Composable
fun AddProfileScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            Button(onClick = {
                viewModel.addProfile(name) {
                    onBack() // Navigate back to EntryScreen for now, change to mangaView later
                }
            }) {
                Text("Add Profile")
            }
        }
    }
}