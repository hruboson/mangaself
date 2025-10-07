package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hrubos.db.Profile
import dev.hrubos.mangaself.ui.components.AppLogo
import dev.hrubos.mangaself.ui.components.TextH1
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

@Composable
fun EntryScreen(viewModel: ProfileViewModel, onNavigateToAdd: () -> Unit){
    
    var profiles: List<Profile> by remember { mutableStateOf(listOf<Profile>()) }

    LaunchedEffect(Unit) {
        viewModel.getProfiles { profiles = it }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(50.dp),
        ) {
            AppLogo(modifier = Modifier.align(Alignment.CenterHorizontally))

            TextH1(text = "Profiles", modifier = Modifier.align(Alignment.CenterHorizontally))

            /**
             * Rows of buttons each corresponding to a profile
             * Upon pressing the button the user is redirected to screen
             */
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                profiles.forEach { profile ->
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween, // text left, icon right
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = profile.name)

                            // Show icon based on profile type
                            val icon = if (profile.isLocal) Icons.Default.Storage else Icons.Default.Cloud
                            Icon(
                                imageVector = icon,
                                contentDescription = if (profile.isLocal) "Local Profile" else "Remote Profile",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // moves last button all the way down
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onNavigateToAdd() }) {
                Text("Add Profile")
            }
        }
    }
}