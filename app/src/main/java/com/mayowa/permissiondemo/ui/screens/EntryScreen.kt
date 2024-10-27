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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.MainActivity.Companion.requiredPermissions
import com.mayowa.permissiondemo.PhotoCaptureDestination
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.ui.composables.AppAsyncImage
import com.mayowa.permissiondemo.ui.permissions.PermissionStateManager.PendingPermissionIntent
import com.mayowa.permissiondemo.ui.permissions.PermissionWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    navController: NavController,
    viewModel: EntryScreenViewModel,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    PermissionWrapper(
        permissionStateManager = viewModel.permissionStateManager,
        permissions = requiredPermissions,
        onPendingIntentInvoked = { viewModel.onEvent(EntryScreenViewModel.Event.OnPendingIntentInvoked(it)) },
        screenContent = { requirePermissions ->
            AppScaffold(
                title = stringResource(id = R.string.media_screen_name),
                canGoBack = false,
                modifier = Modifier.fillMaxSize(),
                actions = { EntryScreenActions() },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        requirePermissions(PendingPermissionIntent.LaunchCameraScreen) {
                            viewModel.onEvent(EntryScreenViewModel.Event.TakePhotoTapped)
                        }
                    }) {
                        Icon(painterResource(id = R.drawable.ic_camara), contentDescription = "")
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    EntryScreenContent(state)
                }
            }
        }
    )

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EntryScreenViewModel.Effect.LaunchCameraScreen -> navController.navigate(PhotoCaptureDestination)
            }
        }
    }
}

@Composable
private fun EntryScreenContent(
    state: EntryScreenViewModel.State,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(state.randomPhotos) { photo ->
                AppAsyncImage(filePath = photo.mediaUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun EntryScreenActions() {
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