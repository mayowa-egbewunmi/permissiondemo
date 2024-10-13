package com.mayowa.permissiondemo.ui.modals

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.R

@Composable
fun CustomPermissionModalScreen(
    modifier: Modifier = Modifier,
    title: String,
    rationale: String,
    @DrawableRes icon: Int,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .then(modifier)
    ) {
        IconButton(modifier = Modifier.align(Alignment.End), onClick = {}) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "",
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = ""
                )
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            onClick = onProceed
        ) {
            Text(
                text = stringResource(id = R.string.allow),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview
@Composable
fun PermissionModalScreenPreview() {
    CustomPermissionModalScreen(
        onProceed = {},
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        title = "Permission Required",
        rationale = "This app needs camera permission to function properly",
        icon = R.drawable.ic_art_camera
    )
}