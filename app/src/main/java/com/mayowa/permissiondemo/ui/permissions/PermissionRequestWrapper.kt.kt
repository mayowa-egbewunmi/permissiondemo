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
import com.mayowa.permissiondemo.utils.LocalPermissionUtil
import com.mayowa.permissiondemo.utils.getActivity

@Composable
fun PermissionWrapper(
    permissionStateManager: PermissionStateManager,
    permissions: List<String>,
    onPendingIntentInvoked: (PermissionStateManager.PendingPermissionIntent) -> Unit,
    screenContent: @Composable (requirePermissions: (PermissionStateManager.PendingPermissionIntent, () -> Unit) -> Unit) -> Unit,
) {
    val permissionUtil = LocalPermissionUtil.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by permissionStateManager.state.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
        val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), permissions)
        val isRationaleRequired = permissionUtil.shouldShowRationale(context.getActivity(), unapprovedPermissions)
        permissionStateManager.onEvent(PermissionStateManager.Event.PermissionStateUpdated(unapprovedPermissions.toSet(), isRationaleRequired))
    }
    screenContent(permissionStateManager::requirePermissions)

    val permissionAction = state.permissionAction
    if (state.pendingPermissionIntent != null && permissionAction != null) {
        PermissionScreenContent(
            permissionAction = permissionAction,
            permissionLauncher = permissionLauncher,
            onEvent = permissionStateManager::onEvent,
            onPendingIntentInvoked = onPendingIntentInvoked,
        )
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val unapprovedPermissions = permissionUtil.filterNotGranted(context.getActivity(), permissions)
                val isRationaleRequired = permissionUtil.shouldShowRationale(context.getActivity(), permissions)
                permissionStateManager.onEvent(PermissionStateManager.Event.OnScreenLaunch(unapprovedPermissions.toSet(), isRationaleRequired))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun PermissionScreenContent(
    permissionAction: PermissionAction,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    onEvent: (PermissionStateManager.Event) -> Unit,
    onPendingIntentInvoked: (PermissionStateManager.PendingPermissionIntent) -> Unit,
) {
    val context = LocalContext.current
    when (permissionAction) {
        is PermissionAction.RequestPermission -> {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(permissionAction.unapprovedPermissions.toTypedArray())
            }
        }

        is PermissionAction.ShowRationale -> {
            MultiplePermissionsRationaleScreen(
                requiresSettings = permissionAction.requiresSettings,
                requiredPermissions = permissionAction.unapprovedPermissions,
                onClose = { onEvent(PermissionStateManager.Event.OnPermissionRequestCancelled) },
                onProceed = {
                    if (permissionAction.requiresSettings) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    } else {
                        permissionLauncher.launch(permissionAction.unapprovedPermissions.toTypedArray())
                    }
                }
            )
        }

        is PermissionAction.Proceed -> {
            LaunchedEffect(Unit) {
                permissionAction.intent?.let {
                    onEvent(PermissionStateManager.Event.OnPendingIntentConsumed)
                    when (permissionAction.intent) {
                        PermissionStateManager.PendingPermissionIntent.LaunchCameraScreen -> {
                            onPendingIntentInvoked(permissionAction.intent)
                        }
                    }
                }
            }
        }
    }
}