package com.mayowa.permissiondemo.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.PhotoCaptureDestination
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.PermissionMeta
import com.mayowa.permissiondemo.ui.permissions.PermissionStateManager.PermissionIntent
import com.mayowa.permissiondemo.ui.permissions.PermissionUIWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    navController: NavController,
    viewModel: EntryScreenViewModel,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    PermissionUIWrapper(
        permissionStateManager = viewModel.permissionStateManager,
        permissions = EntryScreenViewModel.allPermissions,
        onPermissionIntentInvoked = { viewModel.onEvent(EntryScreenViewModel.Event.OnPendingIntentInvoked(it)) },
        screenContent = { unapprovedPermissions ->
            val requirePermissions = viewModel.permissionStateManager::requirePermissions
            AppScaffold(
                title = stringResource(id = R.string.media_screen_name),
                canGoBack = false,
                modifier = Modifier.fillMaxSize(),
                actions = { EntryScreenActions(requirePermissions, viewModel::onEvent) },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        val requiredPermissions = EntryScreenViewModel.CAMERA_PERMISSIONS
                        requirePermissions(PermissionIntent.LaunchCameraScreen(requiredPermissions)) {
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
                    EntryScreenContent(unapprovedPermissions, state, viewModel::onEvent, requirePermissions)
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

@SuppressLint("InlinedApi")
@Composable
private fun EntryScreenContent(
    unapprovedPermissions: Set<PermissionMeta>,
    state: EntryScreenViewModel.State,
    onEvent: (EntryScreenViewModel.Event) -> Unit,
    requirePermissions: (PermissionIntent, () -> Unit) -> Unit,
) {
    val permissionsToRequest = remember(unapprovedPermissions) {
        EntryScreenViewModel.MEDIA_PERMISSIONS
            .mapNotNull { neededPermission ->
                unapprovedPermissions
                    .firstOrNull {
                        it.permission == neededPermission
                    }
            }
    }

    if (permissionsToRequest.isNotEmpty()) {
        EmptyStateScreen(requirePermissions, onEvent)
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                items(state.photoUris, key = { it.mediaUri.path!! }) { photo ->
                    AsyncImage(model = photo.mediaUri, contentDescription = null)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isEmpty()) {
            onEvent(EntryScreenViewModel.Event.RefreshRequested)
        }
    }
}

@Composable
private fun EmptyStateScreen(requirePermissions: (PermissionIntent, () -> Unit) -> Unit, onEvent: (EntryScreenViewModel.Event) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.precious_moments_caption),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier.height(240.dp),
            painter = painterResource(id = R.drawable.img_art_person_capture),
            contentDescription = ""
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.save_pictures_caption),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            onClick = {
                requirePermissions(PermissionIntent.FetchMediaPhotos(EntryScreenViewModel.MEDIA_PERMISSIONS)) {
                    onEvent(EntryScreenViewModel.Event.GetStartedButtonTapped)
                }
            }
        ) {
            Text(
                text = stringResource(R.string.get_started),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

}

@Composable
private fun EntryScreenActions(requirePermissions: (PermissionIntent, () -> Unit) -> Unit, onEvent: (EntryScreenViewModel.Event) -> Unit) {
    val pickMultiplePhotosLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
        onEvent(EntryScreenViewModel.Event.RefreshRequested)
    }

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
            onClick = {
                requirePermissions(PermissionIntent.FetchMediaPhotos(EntryScreenViewModel.MEDIA_PERMISSIONS)) {
                    pickMultiplePhotosLauncher.launch(EntryScreenViewModel.MEDIA_PERMISSIONS.toTypedArray())
                }
            },
        )
    }
}