package com.mayowa.permissiondemo.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.PhotoCaptureDestination
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.ui.composables.AppAsyncImage
import com.mayowa.permissiondemo.ui.modals.MultiplePermissionsRationaleScreen
import com.mayowa.permissiondemo.utils.LocalPermissionUtil
import com.mayowa.permissiondemo.utils.getActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(navController: NavController, viewModel: EntryScreenViewModel) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionUtil = LocalPermissionUtil.current

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), result.keys.toSet())
        viewModel.onEvent(EntryScreenViewModel.Event.PermissionStateUpdated(result.keys, unapprovedPermissions.toSet()))
    }

    AppScaffold(
        title = stringResource(id = R.string.media_screen_name),
        canGoBack = false,
        modifier = Modifier.fillMaxSize(),
        actions = { EntryScreenActions(permissionLauncher) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), EntryScreenViewModel.CAMERA_PERMISSIONS)
                viewModel.onEvent(EntryScreenViewModel.Event.TakePhotoTapped(EntryScreenViewModel.CAMERA_PERMISSIONS, unapprovedPermissions.toSet()))
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
            val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), EntryScreenViewModel.MEDIA_PERMISSIONS)
            if (unapprovedPermissions.isNotEmpty()) {
                EmptyStateScreen(permissionLauncher)
            } else {
                EntryScreenContent(state = state)
            }
        }
    }
    val permissionAction = state.permissionAction
    if (state.pendingPermissionIntent != null && permissionAction != null) {
        PermissionScreenContent(
            permissionAction = permissionAction,
            permissionLauncher = permissionLauncher,
            launchCameraScreen = { navController.navigate(PhotoCaptureDestination) },
            onEvent = viewModel::onEvent
        )
    }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), EntryScreenViewModel.ALL_PERMISSIONS)
                viewModel.onEvent(EntryScreenViewModel.Event.OnScreenLaunched(unapprovedPermissions.toSet()))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), EntryScreenViewModel.ALL_PERMISSIONS)
        if (unapprovedPermissions.isNotEmpty()) {
            viewModel.onEvent(EntryScreenViewModel.Event.RefreshRequested)
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
            items(state.photoUris) { photo ->
                AppAsyncImage(mediaUri = photo.mediaUri)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun EmptyStateScreen(
    permissionLauncher: ActivityResultLauncher<Array<String>>,
) {
    val context = LocalContext.current
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
            onClick = { permissionLauncher.launch(EntryScreenViewModel.MEDIA_PERMISSIONS.toTypedArray()) }
        ) {
            Text(
                text = stringResource(R.string.get_started),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

}

@Composable
private fun PermissionScreenContent(
    permissionAction: PermissionAction,
    launchCameraScreen: () -> Unit,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    onEvent: (EntryScreenViewModel.Event) -> Unit,
) {
    val permissionsToRequest = permissionAction.permissionsToRequest.map { it.permission }.toSet()
    val context = LocalContext.current
    when (permissionAction) {
        is PermissionAction.RequestPermission -> {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }

        is PermissionAction.ShowRationale -> {
            MultiplePermissionsRationaleScreen(
                requiresSettings = !permissionAction.requiresSettings,
                requiredPermissions = permissionsToRequest,
                onClose = { onEvent(EntryScreenViewModel.Event.OnPermissionRequestCancelled) },
                onScreenLaunch = {},
                onProceed = {
                    if (permissionAction.requiresSettings) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    } else {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                }
            )
        }

        is PermissionAction.Proceed -> {
            LaunchedEffect(Unit) {
                permissionAction.intent?.let {
                    when (permissionAction.intent) {
                        is EntryScreenViewModel.UiIntent.LaunchCameraScreen -> {
                            onEvent(EntryScreenViewModel.Event.OnPendingIntentConsumed)
                            launchCameraScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryScreenActions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
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
                permissionLauncher.launch(EntryScreenViewModel.MEDIA_PERMISSIONS.toTypedArray())
            },
        )
    }
}