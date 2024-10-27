package com.mayowa.permissiondemo.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.PERMISSION_RATIONALE

@Composable
fun PermissionRationaleDialog(
    requiredPermissions: Set<String>,
    onRequestPermission: () -> Unit,
    onClose: () -> Unit,
) {
    val rationaleText = remember(requiredPermissions) {
        requiredPermissions.mapNotNull { PERMISSION_RATIONALE[it] }.joinToString("\n\n")
    }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text(text = stringResource(id = R.string.permissions_required)) },
        text = { Text(text = rationaleText) },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string._continue))
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun PermissionSettingsDialog(
    requiredPermissions: Set<String>,
    onSettingsTapped: () -> Unit,
    onClose: () -> Unit,
) {
    val rationaleText = remember(requiredPermissions) {
        requiredPermissions.mapNotNull { PERMISSION_RATIONALE[it] }.joinToString("\n\n")
    }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text(text = stringResource(id = R.string.permissions_required)) },
        text = { Text(text = rationaleText) },
        confirmButton = {
            Button(onClick = onSettingsTapped) {
                Text("Launch Settings")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}