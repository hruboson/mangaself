package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.hrubos.mangaself.ui.components.FloatingTopMenu

@Composable
fun AboutScreen(onBack: () -> Unit){
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        FloatingTopMenu(onBack = onBack)
    }
}