package com.mayowa.permissiondemo.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.R

@Composable
fun CameraCaptureIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.size(58.dp).then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_photo_capture_ii),
                contentDescription = null
            )
        }
    )
}

@Composable
fun CameraPauseIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_pause),
                contentDescription = null
            )
        }
    )
}

@Composable
fun CameraPauseIconII(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_pause_ii),
                contentDescription = null
            )
        }
    )
}

@Composable
fun CameraPlayIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_play_i),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraPlayIconII(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_play_ii),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraRecordIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_record),
                contentDescription = null,
            )
        })
}

@Composable
fun CameraPlayIconIII(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_play_iii),
                contentDescription = ""
            )
        }
    )
}



@Composable
fun CameraStopIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_stop),
                contentDescription = null,
            )
        }
    )
}

@Composable
fun CameraFlipIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_rotate),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraCloseIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_close_ii),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraInfoIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_info_ii),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraDiscardIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_trash),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraSaveIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_save),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraFlashIcon(modifier: Modifier = Modifier, flashEnabled: Boolean, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            val flashDrawable = if (flashEnabled) {
                R.drawable.ic_flash_on_ii
            } else {
                R.drawable.ic_flash_off_ii
            }
            Image(
                painter = painterResource(id = flashDrawable),
                contentDescription = ""
            )
        }
    )
}