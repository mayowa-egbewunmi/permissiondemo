package com.mayowa.permissiondemo.ui.composables

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.Companion.DefaultTransform
import coil.compose.AsyncImagePainter.State
import coil.compose.DefaultModelEqualityDelegate
import coil.compose.EqualityDelegate
import coil.request.ImageRequest

@Composable
fun AppAsyncImage(
    mediaUri: Uri,
    modifier: Modifier = Modifier,
    contentDescription: String? = "",
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    transform: (State) -> State = DefaultTransform,
    onState: ((State) -> Unit)? = null,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    modelEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate,
) {
    val context = LocalContext.current
    val loader = remember(context) { ImageLoader.Builder(context).build() }

    AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .data(mediaUri)
            .crossfade(true)
            .build(),
        imageLoader = loader,
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(modifier),
        contentScale = contentScale,
        alignment = alignment,
        transform = transform,
        onState = onState,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        clipToBounds = clipToBounds,
        modelEqualityDelegate = modelEqualityDelegate,
    )
}