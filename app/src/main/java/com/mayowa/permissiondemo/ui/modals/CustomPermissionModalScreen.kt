package com.mayowa.permissiondemo.ui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.PERMISSION_ICONS
import com.mayowa.permissiondemo.models.PERMISSION_RATIONALE

@Composable
fun CustomPermissionModalScreen(
    isPermissionRequestable: Boolean,
    requiredPermissions: Set<String>,
    onClose: () -> Unit,
    onProceed: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { requiredPermissions.size })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(modifier = Modifier.align(Alignment.End), onClick = onClose) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "",
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val rationaleText = remember(page) { PERMISSION_RATIONALE[requiredPermissions.elementAt(page)] }
            val icon = remember(page) { PERMISSION_ICONS[requiredPermissions.elementAt(page)] }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.permissions_required),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rationaleText!!,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = icon!!),
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
                text = stringResource(id = if (isPermissionRequestable) R.string._continue else R.string.launch_settings),
                style = MaterialTheme.typography.labelMedium
            )
        }

//        // Indicator
//        HorizontalPagerIndicator(
//            pagerState = pagerState,
//            pageCount = pages.size,
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//                .padding(16.dp),
//            activeColor = Color.Blue,
//            inactiveColor = Color.Gray
//        )
    }
}

@Preview
@Composable
fun PermissionModalScreenPreview() {
    CustomPermissionModalScreen(
        isPermissionRequestable = true,
        onProceed = {},
        onClose = {},
        requiredPermissions = setOf("android.permission.CAMERA"),
    )
}