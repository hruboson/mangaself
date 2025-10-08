package dev.hrubos.mangaself.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.model.DEFAULT_FUNC

@Composable
fun FloatingTopMenu(
    buttonSize: Dp = 48.dp,
    onBack: () -> Unit = DEFAULT_FUNC,
    onShowFavourite: () -> Unit = DEFAULT_FUNC,
    onSettings: () -> Unit = DEFAULT_FUNC,
    onInfo: () -> Unit = DEFAULT_FUNC
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 38.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.9f).height(IntrinsicSize.Min) // shrink-wrap height
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if(onBack != DEFAULT_FUNC) {
                        MaterialIconButton(
                            onClick = onBack,
                            icon = Icons.Default.ChevronLeft,
                            size = buttonSize
                        )
                    }
                    if(onShowFavourite != DEFAULT_FUNC){
                        MaterialIconButton(
                            onClick = onBack,
                            icon = Icons.Default.StarBorder,
                            size = buttonSize
                        )
                    }
                }

                // Center search bar
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        placeholder = { Text("Search...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        shape = RoundedCornerShape(50)
                    )
                }


                // Right buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if(onSettings != DEFAULT_FUNC) {
                        MaterialIconButton(
                            onClick = onSettings,
                            icon = Icons.Default.Settings,
                            size = buttonSize
                        )
                    }
                    if(onInfo != DEFAULT_FUNC) {
                        MaterialIconButton(
                            onClick = {},
                            icon = Icons.Default.Info,
                            size = buttonSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialIconButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, size: Dp) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier.size(size)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
