package dev.hrubos.mangaself.view

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.hrubos.db.Chapter
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.model.ReadingMode
import dev.hrubos.mangaself.model.numericComparator
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    profileViewModel: ProfileViewModel,
    shelfViewModel: ShelfViewModel,
    publication: Publication,
    chapter: Chapter,
    initialPage: Int = 0,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit = {},
    onChapterChange: (Chapter) -> Unit
) {
    var chapter by remember { mutableStateOf(chapter) }

    val context = LocalContext.current
    val chapterDir = DocumentFile.fromTreeUri(context, Uri.parse(chapter.systemPath))
    val pageFiles = remember(chapter.systemPath) { // should this be in model? but seems like a pain in the ass to store the pages separately and probably not optimal
        chapterDir?.listFiles()
            ?.filter { it.isFile && it.name?.matches(Regex(".*\\.(jpg|jpeg|png|webp|pdf)$", RegexOption.IGNORE_CASE)) == true }
            ?.sortedWith(numericComparator)
            ?: emptyList()
    }

    var readingMode by rememberSaveable { mutableStateOf(profileViewModel.getCurrentReadingMode()) }

    // state for LEFTTORIGHT, RIGHTTOLEFT
    val pagerState = rememberPagerState(
        initialPage = if (chapter.pageLastRead > 0) chapter.pageLastRead - 1 else 0,
        pageCount = { pageFiles.size }
    )
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)

        shelfViewModel.updateChapterLastRead(publication, chapter, pagerState.currentPage + 1)
    }

    // state for LONGSTRIP
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (chapter.pageLastRead > 0) chapter.pageLastRead - 1 else 0
    )
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val currentPage = listState.firstVisibleItemIndex
        if (currentPage >= 0) {
            shelfViewModel.updateChapterLastRead(publication, chapter, currentPage + 1)
        }
    }

    var showMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${publication.title} — ${chapter.title}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ChevronLeft, null) } },
                actions = {
                    ModeSelectorDropdown(
                        currentMode = readingMode,
                        onModeSelected = { selected ->
                            readingMode = selected
                            profileViewModel.updateProfileReadingMode(selected.text)
                        }
                    )
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isRtl = readingMode == ReadingMode.RIGHTTOLEFT
                    val currentIndex = publication.chapters.indexOfFirst { it.systemPath == chapter.systemPath }

                    val prevIndex = currentIndex + if (isRtl) 1 else -1
                    val nextIndex = currentIndex + if (isRtl) -1 else 1

                    Button(onClick = {
                        if (prevIndex in publication.chapters.indices) {
                            val newChapter = publication.chapters[prevIndex]
                            chapter = newChapter
                            onChapterChange(newChapter)
                        } else {
                            showMessage = if (isRtl) "No next chapter" else "No previous chapter"
                        }
                    }) {
                        Text(if (isRtl) "Next chapter" else "Previous chapter")
                    }

                    Button(onClick = {
                        if (nextIndex in publication.chapters.indices) {
                            val newChapter = publication.chapters[nextIndex]
                            chapter = newChapter
                            onChapterChange(newChapter)
                        } else {
                            showMessage = if (isRtl) "No previous chapter" else "No next chapter"
                        }
                    }) {
                        Text(if (isRtl) "Previous chapter" else "Next chapter")
                    }
                }

                // floating message
                showMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.8f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = msg, color = androidx.compose.ui.graphics.Color.White)
                    }

                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(1500)
                        showMessage = null
                    }
                }
            }
        }
    ) { padding ->
        if (pageFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No pages found in this chapter")
            }
        } else {
            when (readingMode) {
                ReadingMode.LONGSTRIP -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(pageFiles) { file ->
                            ReaderPage(file)
                        }
                    }
                }
                ReadingMode.LEFTTORIGHT, ReadingMode.RIGHTTOLEFT -> {
                    HorizontalPager(
                        state = pagerState,
                        reverseLayout = (readingMode == ReadingMode.RIGHTTOLEFT),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) { index ->
                        ReaderPage(pageFiles[index])
                    }
                }
            }
        }
    }

    BackHandler { onBack() }
}


@Composable
fun ReaderPage(file: DocumentFile) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    val imageRequest = remember(file.uri) {
        ImageRequest.Builder(context)
            .data(file.uri)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = file.name,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth,
            onState = { state ->
                isLoading = when (state) {
                    is coil.compose.AsyncImagePainter.State.Loading -> true
                    is coil.compose.AsyncImagePainter.State.Error -> false
                    is coil.compose.AsyncImagePainter.State.Success -> false
                    else -> false
                }
            }
        )

        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ModeSelectorDropdown(
    currentMode: ReadingMode,
    onModeSelected: (ReadingMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Reading Mode")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReadingMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = mode.text,
                            color = if (mode == currentMode)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}