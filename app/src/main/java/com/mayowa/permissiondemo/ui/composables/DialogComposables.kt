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
    requiredPermissions: List<String>,
    onRequestPermission: () -> Unit, onClose: () -> Unit,
) {
    val rationaleText = remember(requiredPermissions) {
        requiredPermissions.map { PERMISSION_RATIONALE[it] }.joinToString("\n\n")
    }

    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(text = stringResource(id = R.string.permissions_required)) },
        text = { Text(text = rationaleText) },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Continue")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CameraPermissionSettingsDialog(
    requiredPermissions: List<String>,
    onSettingsTapped: () -> Unit, onClose: () -> Unit,
) {
    val rationaleText = remember(requiredPermissions) {
        requiredPermissions.map { PERMISSION_RATIONALE[it] }.joinToString("\n\n")
    }

    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
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