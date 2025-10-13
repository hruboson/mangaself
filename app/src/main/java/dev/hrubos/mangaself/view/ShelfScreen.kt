package dev.hrubos.mangaself.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.R
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel

data class TabItem(
    val name: String,
    val screen: String
)


@Composable
fun ShelfWrapper(
    modifier: Modifier = Modifier,
    shelfViewModel: ShelfViewModel,
    profileViewModel: ProfileViewModel,
    onSettings: () -> Unit,
    onFolderSelected: (Uri) -> Unit,
    onPublicationClick: (Publication) -> Unit
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
                        0 -> ShelfScreen(shelfViewModel, profileViewModel, onPublicationClick)
                        1 -> AddMangaScreen(shelfViewModel, profileViewModel, onFolderSelected)
                        else -> Text("Unknown Screen")
                    }
                }
            }
        }
    }
}

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
    onPublicationClick: (Publication) -> Unit
) {
    val selectedProfile by profileViewModel.selectedProfile.collectAsState()
    val publications by shelfViewModel.publications.collectAsState(emptyList())

    LaunchedEffect(selectedProfile?.id) {
        selectedProfile?.let { profile ->
            shelfViewModel.loadPublicationsOfProfile(profile.id)
        }
    }

    // Simple Column with top bar and list
    Column(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(publications, key = { it.systemPath }) { publication ->
                PublicationGridItem(
                    publication = publication,
                    onClick = { onPublicationClick(publication) }
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
                text = "Browse local folders",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PublicationGridItem(
    publication: Publication,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { /* TODO batch select */ }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        val coverImage = if(publication.coverPath.isNotBlank()) publication.coverPath else R.drawable.cover_placeholder

        // Cover image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverImage)
                .crossfade(true)
                .build(),
            contentDescription = publication.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top-left: chapters info
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 1f),
                            Color.Transparent,
                        )
                    )
                ),
            contentAlignment = Alignment.TopStart
        ) { Text(
                text = publication.lastChapterRead.toString() + "/" + publication.chapters.size.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Bottom: title
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 1f)
                        )
                    )
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = if (publication.title.isNotBlank()) publication.title else "Untitled",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PublicationDetail(
    shelfViewModel: ShelfViewModel,
    path: String,
    onBack: () -> Unit,
){
    /**
     *
     * TODO rescan chapters
     *
     */

    val publication by shelfViewModel.publication.collectAsState()
    val isScanning by shelfViewModel.isScanning.collectAsState()

    LaunchedEffect(path) {
        shelfViewModel.loadPublication(path)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            publication?.let { pub ->
                FloatingTopMenu(
                    onBack = onBack,
                    title = pub.title
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = pub.title,
                        onValueChange = { shelfViewModel.editPublicationTitle(it) },
                        label = { Text("Title") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = pub.description,
                        onValueChange = { shelfViewModel.editPublicationDescription(it) },
                        label = { Text("Description") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chapters list
                    Text("Chapters", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val sortedChapters = pub.chapters.sortedBy { it.position }
                        items(sortedChapters.size) { index ->
                            val chapter = sortedChapters[index]

                            // determine if chapter is fully read
                            val isFullyRead = chapter.pageLastRead == chapter.pages

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Navigate TODO ... onChapterClick(chapter) */ }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Pages: ${chapter.pages}, Last read: ${chapter.pageLastRead}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                if (isFullyRead) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Chapter fully read",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }else{
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Read chapter",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            shelfViewModel.removePublication(profileId = Configuration.selectedProfileId)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove from library")
                    }
                }
            } ?: Text("Loading...")
        }
    }

    if (isScanning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Scanning chapters...", color = Color.White)
            }
        }
    }
}

fun onSearchPlaceholder(str: String){
    Log.v("Search:", str)
}