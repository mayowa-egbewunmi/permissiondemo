package com.mayowa.permissiondemo.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.MainActivity.Companion.requiredPermissions
import com.mayowa.permissiondemo.PhotoCaptureDestination
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.ui.composables.AppAsyncImage
import com.mayowa.permissiondemo.ui.modals.CustomPermissionModalScreen
import com.mayowa.permissiondemo.utils.PermissionUtil
import com.mayowa.permissiondemo.utils.getActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(navController: NavController, viewModel: EntryScreenViewModel) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
        val unGrantedPermissions = PermissionUtil.filterPermissionsNotGranted(context.getActivity(), requiredPermissions)
        val isRationaleRequired = PermissionUtil.shouldShowRequestPermissionRationale(context.getActivity(), unGrantedPermissions)
        viewModel.onEvent(EntryScreenViewModel.Event.PermissionRequirementUpdated(unGrantedPermissions.toSet(), isRationaleRequired))
    }
    AppScaffold(
        title = stringResource(id = R.string.media_screen_name),
        canGoBack = false,
        showAppBar = !state.customRationaleDisplayed,
        modifier = Modifier.fillMaxSize(),
        actions = { EntryScreenActions() },
        floatingActionButton = {
            if (!state.customRationaleDisplayed) {
                FloatingActionButton(onClick = {
                    val requiredPermissions = PermissionUtil.filterPermissionsNotGranted(context.getActivity(), requiredPermissions)
                    val isRationaleRequired = PermissionUtil.shouldShowRequestPermissionRationale(context.getActivity(), requiredPermissions)
                    viewModel.onEvent(EntryScreenViewModel.Event.TakePhotoTapped(requiredPermissions.toSet(), isRationaleRequired))
                }) {
                    Icon(painterResource(id = R.drawable.ic_camara), contentDescription = "")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            EntryScreenContent(permissionLauncher, navController, state, viewModel::onEvent)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                val unGrantedPermissions = PermissionUtil.filterPermissionsNotGranted(context.getActivity(), requiredPermissions)
                val isRationaleRequired = PermissionUtil.shouldShowRequestPermissionRationale(context.getActivity(), requiredPermissions)
                viewModel.onEvent(EntryScreenViewModel.Event.OnScreenLaunch(unGrantedPermissions.toSet(), isRationaleRequired))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun EntryScreenContent(
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    navController: NavController,
    state: EntryScreenViewModel.State,
    onEvent: (EntryScreenViewModel.Event) -> Unit,
) {

    val context = LocalContext.current

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
    val permissionAction = state.permissionAction
    if (state.pendingUiIntent != null && permissionAction != null) {
        when (permissionAction) {
            is PermissionAction.RequestPermission -> {
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(permissionAction.unGrantedPermissions.toTypedArray())
                }
            }

            is PermissionAction.ShowRationale -> {
                CustomPermissionModalScreen(
                    isPermissionRequestable = true,
                    requiredPermissions = permissionAction.unGrantedPermissions,
                    onClose = { onEvent(EntryScreenViewModel.Event.OnPermissionRequestCancelled) },
                    onScreenLaunch = { onEvent(EntryScreenViewModel.Event.OnCustomRationaleDisplayed) },
                    onProceed = {
                        permissionLauncher.launch(permissionAction.unGrantedPermissions.toTypedArray())
                    }
                )
            }

            is PermissionAction.LaunchSettings -> {
                CustomPermissionModalScreen(
                    isPermissionRequestable = false,
                    requiredPermissions = permissionAction.unGrantedPermissions,
                    onClose = { onEvent(EntryScreenViewModel.Event.OnPermissionRequestCancelled) },
                    onScreenLaunch = { onEvent(EntryScreenViewModel.Event.OnCustomRationaleDisplayed) },
                    onProceed = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                )
            }

            is PermissionAction.ProceedWithIntent -> {
                LaunchedEffect(Unit) {
                    when (permissionAction.intent) {
                        EntryScreenViewModel.UiIntent.LaunchCameraScreen -> {
                            navController.navigate(PhotoCaptureDestination)
                            onEvent(EntryScreenViewModel.Event.OnPendingIntentConsumed)
                        }

                        null -> Unit
                    }
                }
            }
        }
    }
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