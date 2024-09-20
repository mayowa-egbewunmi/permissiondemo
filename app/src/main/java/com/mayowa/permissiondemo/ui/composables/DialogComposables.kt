package com.mayowa.permissiondemo.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CameraPermissionRationaleDialog(onRequestPermission: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(text = "Camera Permission Required") },
        text = { Text(text = "We need access to your camera to capture photos.") },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
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
fun CameraPermissionSettingsDialog(onSettingsTapped: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(text = "Camera Permission Required") },
        text = { Text(text = "We need access to your camera to capture photos. Please enable camera access in your device settings.") },
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