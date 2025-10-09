package dev.hrubos.mangaself.view

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
    LaunchedEffect(pagerState.currentPage) {
        selectedTabItem = pagerState.currentPage
    }

    FloatingTopMenu(onShowFavourite = { }, onSettings = onSettings, onSearch = ::onSearchPlaceholder)

    Box(modifier = Modifier) {

        Column(modifier.fillMaxSize()) {

            /*TabRow(selectedTabItem) {
                listTabItem.forEachIndexed { index, tabItem ->
                    Tab(selected = index == selectedTabItem, onClick = {
                        selectedTabItem = index
                    }, text = {
                        Text(tabItem.name)
                    })
                }
            }*/

            HorizontalPager(state = pagerState, modifier.fillMaxSize()) {
                when (selectedTabItem) {
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