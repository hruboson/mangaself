package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLibrary
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
fun EntryScreen(
    viewModel: ProfileViewModel,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToShelf: (String) -> Unit
){
    
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
            Spacer(modifier = Modifier.height(40.dp))

            AppLogo(modifier = Modifier.align(Alignment.CenterHorizontally))

            TextH1(text = "Profiles", modifier = Modifier.align(Alignment.CenterHorizontally))

            /**
             * Rows of buttons each corresponding to a profile
             * Upon pressing the button the user is redirected to screen tbd
             */
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (profiles.isEmpty()) {
                    // Show a button when there are no profiles
                    Button(
                        onClick = { onNavigateToAddProfile() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add First Profile")
                    }
                } else {
                    profiles.forEach { profile ->
                        Button(
                            onClick = { onNavigateToShelf(profile.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = profile.name)

                                Icon(
                                    imageVector = Icons.Default.LocalLibrary,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // moves last button row all the way down
            Row (modifier = Modifier.align(Alignment.CenterHorizontally)){
                Button(
                    onClick = { onNavigateToAddProfile() }) {
                    Text("Switch Repository")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    onClick = { onNavigateToAddProfile() }) {
                    Text("Add Profile")
                }
            }
        }
    }
}