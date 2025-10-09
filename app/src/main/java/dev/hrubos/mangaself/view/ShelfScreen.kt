package dev.hrubos.mangaself.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
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
 *  ?(- delete from device memory)?
 */
@Composable
fun ShelfScreen(shelfViewModel: ShelfViewModel, onSettings: () -> Unit) {
    Surface (modifier = Modifier.fillMaxSize()) {

    }
}

@Composable
fun AddMangaScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Add manga screen")
    }
}

@Composable
fun ShelfNavigationBar(
    modifier: Modifier = Modifier,
    shelfViewModel: ShelfViewModel,
    onSettings: () -> Unit,
){
    val listTabItem = listOf(
        TabItem("Library", "shelfscreen"),
        TabItem("Add manga", "addmangascreen")
    )
    var selectedTabItem by remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(initialPage = 0) { listTabItem.size }
    LaunchedEffect(selectedTabItem) {
        pagerState.animateScrollToPage(selectedTabItem)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTabItem = pagerState.currentPage
    }

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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    0 -> ShelfScreen(shelfViewModel, onSettings)
                    1 -> AddMangaScreen()
                    else -> Text("Unknown Screen")
                }
            }
        }
    }
}

fun onSearchPlaceholder(str: String){
    Log.v("Search:", str)
}