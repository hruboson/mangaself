package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

@Composable
fun EntryScreen(viewModel: ProfileViewModel, onNavigateToAdd: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FloatingTopMenu()

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavigateToAdd() }) {
            Text("Add Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        var profilesText by remember { mutableStateOf("Loading...") }
        LaunchedEffect(Unit) {
            viewModel.getProfiles { profilesText = it }
        }

        Text(text = profilesText)
    }
}