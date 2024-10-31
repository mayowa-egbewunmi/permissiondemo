package com.mayowa.permissiondemo.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.models.PermissionMeta
import com.mayowa.permissiondemo.ui.modals.MultiplePermissionsRationaleScreen
import com.mayowa.permissiondemo.utils.LocalPermissionUtil
import com.mayowa.permissiondemo.utils.getActivity

@Composable
fun PermissionUIWrapper(
    permissionStateManager: PermissionStateManager,
    permissions: Set<String>,
    onPermissionIntentInvoked: (PermissionStateManager.PermissionIntent) -> Unit,
    screenContent: @Composable (unapprovedPermissions: Set<PermissionMeta>) -> Unit,
) {
    val permissionUtil = LocalPermissionUtil.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by permissionStateManager.state.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val requestedPermissions = result.map { it.key }.toSet()
        val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), permissions)
        permissionStateManager.onEvent(PermissionStateManager.Event.PermissionStateUpdated(requestedPermissions, unapprovedPermissions.toSet()))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), permissions)
                permissionStateManager.onEvent(PermissionStateManager.Event.OnScreenStarted(unapprovedPermissions.toSet()))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    screenContent(state.unapprovedPermissions)

    val permissionAction = state.permissionAction
    if (state.permissionIntent != null && permissionAction != null) {
        PermissionScreenContent(
            permissionAction = permissionAction,
            permissionLauncher = permissionLauncher,
            onEvent = permissionStateManager::onEvent,
            onPermissionIntentInvoked = onPermissionIntentInvoked,
        )
    }
}

@Composable
private fun PermissionScreenContent(
    permissionAction: PermissionAction,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    onEvent: (PermissionStateManager.Event) -> Unit,
    onPermissionIntentInvoked: (PermissionStateManager.PermissionIntent) -> Unit,
) {
    val context = LocalContext.current
    val permissionsToRequest = permissionAction.permissionsToRequest.map { it.permission }.toSet()
    when (permissionAction) {
        is PermissionAction.RequestPermission -> {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
        is PermissionAction.ShowRationale -> {
            MultiplePermissionsRationaleScreen(
                requiresSettings = permissionAction.requiresSettings,
                requiredPermissions = permissionsToRequest,
                onClose = { onEvent(PermissionStateManager.Event.PermissionRequestCancelled) },
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
                    onPermissionIntentInvoked(permissionAction.intent)
                    onEvent(PermissionStateManager.Event.PermissionIntentConsumed)
                }
            }
        }
    }
}