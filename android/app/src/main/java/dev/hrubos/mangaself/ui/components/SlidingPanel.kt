package dev.hrubos.mangaself.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SlidingPanel(
    expanded: Boolean,
    text: String,
    textName: String,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val panelWidthPx = with(density) { 250.dp.roundToPx() }
    val interactionSource = remember { MutableInteractionSource() }

    AnimatedVisibility(
        visible = expanded,
        enter = slideInHorizontally(
            initialOffsetX = { -panelWidthPx },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -panelWidthPx },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Clickable overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null, // disables ripple/highlight
                        interactionSource = interactionSource,
                        onClick = onClose
                    )
                    .background(Color.Transparent) // ensure it stays fully transparent
            )

            // Panel
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .zIndex(1f),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            textName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = onClose) {
                            Text("X", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text.ifBlank { "No $textName available." })
                }
            }
        }
    }
}