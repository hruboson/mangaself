package dev.hrubos.mangaself.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.R

@Composable
fun AppLogo(modifier: Modifier = Modifier){
    Box(
        modifier = modifier
            .size(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFCA24)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "App logo",
            modifier = Modifier.size(200.dp)
        )
    }
}