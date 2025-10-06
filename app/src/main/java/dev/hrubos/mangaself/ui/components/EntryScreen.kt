package dev.hrubos.mangaself.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share

@Composable
fun FloatingTopMenu(
    buttonSize: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialIconButton(icon = Icons.Default.Menu, size = buttonSize)
                    MaterialIconButton(icon = Icons.Default.Add, size = buttonSize)
                }

                // Right buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialIconButton(icon = Icons.Default.Settings, size = buttonSize)
                    MaterialIconButton(icon = Icons.Default.Share, size = buttonSize)
                }
            }
        }
    }
}

@Composable
fun MaterialIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, size: Dp) {
    IconButton(
        onClick = { /* Handle click */ },
        modifier = Modifier.size(size)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
