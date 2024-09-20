package com.mayowa.permissiondemo.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CameraPermissionRationaleDialog(onRequestPermission: () -> Unit) {
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
            Button(onClick = { /* Handle cancel action */ }) {
                Text("Cancel")
            }
        }
    )
}