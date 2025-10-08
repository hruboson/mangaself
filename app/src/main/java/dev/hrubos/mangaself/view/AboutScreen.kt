package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.ui.components.TextH1

@Composable
fun AboutScreen(onBack: () -> Unit){
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        FloatingTopMenu(onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,       // center vertically
            horizontalAlignment = Alignment.CenterHorizontally // center horizontally
        ) {
            TextH1("About")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Author: Ondřej Hruboš")
            Text("Github: github.com/hruboson/mangaself",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextH1("Mangadex API")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Mangadex api was used for acquiring metadata.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text("MangaDex is an ad-free manga reader offering high-quality images!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("api.mangadex.org",
                fontWeight = FontWeight.Bold
            )
        }
    }
}