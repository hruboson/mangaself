package dev.hrubos.mangaself.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hrubos.mangaself.model.DEFAULT_FUNC
import dev.hrubos.mangaself.model.DEFAULT_FUNC_STRING

@Composable
fun FloatingTopMenu(
    buttonSize: Dp = 48.dp,
    onBack: () -> Unit = DEFAULT_FUNC,
    onShowFavourite: () -> Unit = DEFAULT_FUNC,
    onSettings: () -> Unit = DEFAULT_FUNC,
    onInfo: () -> Unit = DEFAULT_FUNC,
    onSearch: (String) -> Unit = DEFAULT_FUNC_STRING,
    title: String = ""
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
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
                            onClick = onShowFavourite,
                            icon = Icons.Default.StarBorder,
                            size = buttonSize
                        )
                    }
                }

                // Center search bar
                if(onSearch != DEFAULT_FUNC_STRING || title != "") {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val background = if(title != "") Color.Transparent else MaterialTheme.colorScheme.surface
                        Box(
                            modifier = Modifier
                                .background(
                                    color = background,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .height(38.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if(onSearch != DEFAULT_FUNC_STRING) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = {
                                            searchQuery = it
                                            onSearch(it)
                                        },
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                                        modifier = Modifier.weight(1f),
                                        cursorBrush = SolidColor(Color.White)
                                    )
                                }
                                if(title != ""){
                                    val displayTitle = if (title.length >= 20) title.take(20) + "..." else title
                                    Text(displayTitle, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
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
                            onClick = onInfo,
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
