package dev.hrubos.mangaself.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel

data class TabItem(
    val name: String,
    val screen: String
)

/**
 * Shows library of user content for given profile in grid.
 * Batch actions:
 *  - mark favourite
 *  - remove from library
 *  - ?(delete from device memory)?
 */
@Composable
fun ShelfScreen(
    shelfViewModel: ShelfViewModel,
    profileViewModel: ProfileViewModel,
) {
    val selectedProfile by profileViewModel.selectedProfile.collectAsState()
    val publications by shelfViewModel.publications.collectAsState(emptyList())

    LaunchedEffect(Unit) {
        selectedProfile?.let { profile ->
            shelfViewModel.loadPublicationsOfProfile(profile.id)
        }
    }

    // Simple Column with top bar and list
    Column(modifier = Modifier.fillMaxSize()) {

        // List of publication titles
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(publications) { publication ->
                Text(
                    text = publication.systemPath,
                    //style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AddMangaScreen(
    shelfViewModel: ShelfViewModel,
    profileViewModel: ProfileViewModel,
    onFolderSelected: (Uri) -> Unit
) {
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { onFolderSelected(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {folderPickerLauncher.launch(null) }
        ) {
            Text(
                text = "Browse local files",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ShelfWrapper(
    modifier: Modifier = Modifier,
    shelfViewModel: ShelfViewModel,
    profileViewModel: ProfileViewModel,
    onSettings: () -> Unit,
    onFolderSelected: (Uri) -> Unit,
){
    val listTabItem = listOf(
        TabItem("Library", "shelfscreen"),
        TabItem("Add new", "addmangascreen")
    )
    var selectedTabItem by remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(initialPage = 0) { listTabItem.size }
    LaunchedEffect(selectedTabItem) {
        pagerState.animateScrollToPage(selectedTabItem)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTabItem = pagerState.currentPage
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Shared top menu
            FloatingTopMenu(
                onShowFavourite = { },
                onSettings = onSettings,
                onSearch = ::onSearchPlaceholder
            )

            // Tabs and content below the top menu
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Tab buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listTabItem.forEachIndexed { index, tabItem ->
                        val isSelected = index == selectedTabItem
                        Button(
                            onClick = { selectedTabItem = index },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tabItem.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> ShelfScreen(shelfViewModel, profileViewModel)
                        1 -> AddMangaScreen(shelfViewModel, profileViewModel, onFolderSelected)
                        else -> Text("Unknown Screen")
                    }
                }
            }
        }
    }
}

fun onSearchPlaceholder(str: String){
    Log.v("Search:", str)
}