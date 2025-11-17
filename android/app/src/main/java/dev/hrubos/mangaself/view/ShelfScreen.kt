package dev.hrubos.mangaself.view

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.hrubos.db.Chapter
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.R
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.ui.components.SlidingPanel
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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
    onPublicationClick: (Publication) -> Unit,
    onToggleFavourite: (Publication) -> Unit,
    onContinueReading: (Publication, Chapter) -> Unit
){
    val listTabItem = listOf(
        TabItem("Library", "shelfscreen"),
        TabItem("Add new", "addmangascreen")
    )
    var selectedTabItem by remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(initialPage = 0) { listTabItem.size }
    val showFavourites by shelfViewModel.showFavourites.collectAsState()

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
                onShowFavourite = { shelfViewModel.toggleShowFavourites() },
                showFavourites = showFavourites,
                onSettings = onSettings,
                onSearch = { query -> shelfViewModel.setSearchQuery(query) }
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
                        0 -> ShelfScreen(shelfViewModel, profileViewModel, onPublicationClick, onToggleFavourite, onContinueReading)
                        1 -> AddMangaScreen(onFolderSelected)
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
    onPublicationClick: (Publication) -> Unit,
    onToggleFavourite: (Publication) -> Unit,
    onContinueReading: (Publication, Chapter) -> Unit
) {
    val selectedProfile by profileViewModel.selectedProfile.collectAsState()
    val publications by shelfViewModel.filteredPublications.collectAsState(emptyList())

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
                    onClick = { onPublicationClick(publication) },
                    onToggleFavourite = { onToggleFavourite(publication) },
                    onContinueReading = onContinueReading
                )
            }
        }
    }
}

@Composable
fun AddMangaScreen(
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
    onClick: () -> Unit,
    onToggleFavourite: (Publication) -> Unit,
    onContinueReading: (Publication, Chapter) -> Unit,
) {
    val chaptersRead: Int = publication.chapters.count { it.pageLastRead == it.pages }

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
        val coverImage = publication.coverPath.ifBlank { R.drawable.cover_placeholder }

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

        // Top-right: favourite icon
        IconButton(
            onClick = {
                onToggleFavourite(publication)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .zIndex(1f) // bring above gradients
        ) {
            Icon(
                imageVector = if (publication.favourite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (publication.favourite) "Unfavourite" else "Favourite",
                tint = MaterialTheme.colorScheme.primary,
            )
        }


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
                text = chaptersRead.toString() + "/" + publication.chapters.size.toString(), // old version counting at least one page read in chapter: publication.lastChapterRead.toString() + "/" + publication.chapters.size.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Bottom row: title on left, continue button on right
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = publication.title.ifBlank { "Untitled" },
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.8f)
            )

            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .aspectRatio(1f) // keep it square
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        val sortedChapters = publication.chapters.sortedBy { it.position }
                        val chapterToContinue = sortedChapters.firstOrNull { it.pageLastRead < it.pages }
                            ?: sortedChapters.lastOrNull()
                        chapterToContinue?.let { onContinueReading(publication, it) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Continue reading",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

    }
}

@OptIn(FlowPreview::class)
@Composable
fun PublicationDetail(
    shelfViewModel: ShelfViewModel,
    path: String,
    onBack: () -> Unit,
    onChapterClick: (Publication, Chapter) -> Unit,
) {
    /**
     *
     * TODO rescan chapters
     *
     */

    val publication by shelfViewModel.publication.collectAsState()
    val isScanning by shelfViewModel.isScanning.collectAsState()

    var showRemoveDialog by remember { mutableStateOf(false) } // confirm dialog state

    var showUpdateConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(path) {
        shelfViewModel.loadPublication(path)
    }

    var titleText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var titleInitialized by remember { mutableStateOf(false) }
    var descriptionInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(publication) {
        publication?.let {
            titleText = it.title
            descriptionText = it.description
        }
    }

    LaunchedEffect(titleText) {
        snapshotFlow { titleText }
            .debounce(500)
            .distinctUntilChanged()
            .collect { newTitle ->
                if (titleInitialized) { // skip first automatic emission
                    publication?.let { pub ->
                        shelfViewModel.editPublicationTitle(newTitle)
                    }
                } else {
                    titleInitialized = true
                }
            }
    }

    LaunchedEffect(descriptionText) {
        snapshotFlow { descriptionText }
            .debounce(500)
            .distinctUntilChanged()
            .collect { newDesc ->
                if (descriptionInitialized) { // skip first automatic emission
                    publication?.let { pub ->
                        shelfViewModel.editPublicationDescription(newDesc)
                    }
                } else {
                    descriptionInitialized = true
                }
            }
    }

    var descriptionExpanded by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                publication?.let { pub ->
                    FloatingTopMenu(
                        onBack = onBack,
                        title = titleText,
                        onInfo = { descriptionExpanded = !descriptionExpanded }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(pub.coverPath.ifBlank { R.drawable.cover_placeholder })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = titleText,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(120.dp)
                                    .aspectRatio(0.75f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.Top),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = titleText,
                                    onValueChange = { titleText = it },
                                    label = { Text("Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = { showRemoveDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text(
                                        "Remove from library",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { shelfViewModel.rescanChapters(pub) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Rescan")
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                publication?.let { pub ->
                                                    shelfViewModel.fetchMetadata(pub)
                                                    showUpdateConfirmation = true
                                                    delay(2000)
                                                    showUpdateConfirmation = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Fetch Info")
                                    }
                                }
                            }
                        }

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
                                val isFullyRead = chapter.pageLastRead == chapter.pages

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onChapterClick(pub, chapter) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
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

                                    Icon(
                                        imageVector = if (isFullyRead)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (isFullyRead) "Chapter fully read" else "Unread chapter",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            val newPage = if (isFullyRead) 0 else chapter.pages
                                            shelfViewModel.updateChapterLastRead(
                                                pub,
                                                chapter,
                                                newPage
                                            )
                                        }
                                    )
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
                                val sortedChapters = pub.chapters.sortedBy { it.position }
                                val chapterToContinue =
                                    sortedChapters.firstOrNull { it.pageLastRead < it.pages }
                                        ?: sortedChapters.lastOrNull()

                                chapterToContinue?.let { chapter ->
                                    onChapterClick(pub, chapter)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue reading")
                        }
                    }
                } ?: Text("Loading...")
            }

            SlidingPanel(
                expanded = descriptionExpanded,
                text = descriptionText,
                textName = "Description",
                onClose = { descriptionExpanded = false}
            )
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

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove ${publication?.title} from library") },
            text = { Text("Are you sure you want to remove this publication from your library? This action will NOT delete any files in your storage.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        shelfViewModel.removePublication(profileId = Configuration.selectedProfileId)
                        onBack()
                    }
                ) {
                    Text("Yes, remove", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showUpdateConfirmation) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Metadata fetched from MangaDex API",
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}