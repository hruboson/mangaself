package dev.hrubos.mangaself.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

class EntryScreen : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EntryScreen(viewModel)
        }
    }
}

@Composable
fun EntryScreen(viewModel: ProfileViewModel){
    var profilesText by remember { mutableStateOf("Loading...") }

    // run once when the composable is first shown
    LaunchedEffect(Unit) {
        viewModel.getProfiles { text ->
            profilesText = if (text.isNotEmpty()) text else "No profiles found"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FloatingTopMenu()

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.addProfile {
                // refresh the profiles list after adding
                viewModel.getProfiles { text ->
                    profilesText = if (text.isNotEmpty()) text else "No profiles found"
                }
            }
        }) {
            Text("Add Test Profile")
        }

        Text(
            text = profilesText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}