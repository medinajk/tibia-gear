package com.example.tibiagear.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tibiagear.data.remote.ItemImages

/**
 * Sprite do item (32x32 px no jogo), ampliado sem suavizar para manter o visual pixelado.
 * É decorativa: o nome do item ao lado já é lido pelo leitor de tela.
 */
@Composable
fun ItemImage(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    AsyncImage(
        model = ItemImages.urlFor(name),
        contentDescription = null,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None,
    )
}
