package com.mayowa.permissiondemo.ui.screens.photos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.Photo
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mayowa.permissiondemo.models.randomSizedPhotos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    onPhotoCaptureClick: () -> Unit

) {
    AppScaffold(
        title = stringResource(id = R.string.photos_screen_name),
        canGoBack = false,
        modifier = Modifier.fillMaxSize(),
        actions = { MediaScreenActions() },
        floatingActionButton = {
            FloatingActionButton(onClick = onPhotoCaptureClick) {
                Icon(painterResource(id = R.drawable.ic_camara), contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MediaScreenContent(randomSizedPhotos)
        }
    }
}

@Composable
private fun MediaScreenActions() {
    val menuExpanded = remember { mutableStateOf(false) }
    IconButton(onClick = { menuExpanded.value = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "More"
        )
    }
    DropdownMenu(
        expanded = menuExpanded.value,
        onDismissRequest = { menuExpanded.value = false },
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            },
            text = { Text(stringResource(id = R.string.add_media)) },
            onClick = { },
        )
    }
}

@Composable
private fun MediaScreenContent(
    randomSizedPhotos: List<Photo>
) {
    val context = LocalContext.current
    val loader = remember(context) {
        ImageLoader.Builder(context).build()
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(randomSizedPhotos) { photo ->
                AsyncImage(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(photo.mediaUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = loader,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}