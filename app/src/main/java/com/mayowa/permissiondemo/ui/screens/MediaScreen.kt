package com.mayowa.permissiondemo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.Photo
import com.mayowa.permissiondemo.models.randomSizedPhotos
import com.mayowa.permissiondemo.ui.composables.AppAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    onPhotoCaptureClick: () -> Unit,
) {
    AppScaffold(
        title = stringResource(id = R.string.media_screen_name),
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
    randomSizedPhotos: List<Photo>,
) {

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(randomSizedPhotos) { photo ->
                AppAsyncImage(filePath = photo.mediaUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun MediaScreenPreview() {
    MediaScreen(
        onPhotoCaptureClick = {}
    )
}