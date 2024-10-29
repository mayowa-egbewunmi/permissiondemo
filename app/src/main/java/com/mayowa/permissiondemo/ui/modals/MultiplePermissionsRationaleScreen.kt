package com.mayowa.permissiondemo.ui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.models.PERMISSION_ICONS
import com.mayowa.permissiondemo.models.PERMISSION_RATIONALE
import com.mayowa.permissiondemo.models.PERMISSION_TITLE

@Composable
fun MultiplePermissionsRationaleScreen(
    requiresSettings: Boolean,
    requiredPermissions: Set<String>,
    onScreenLaunch: () -> Unit,
    onClose: () -> Unit,
    onProceed: () -> Unit,
) {
    val displayedPermissions = remember(requiredPermissions) { requiredPermissions.filter { PERMISSION_RATIONALE[it] != null } }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { displayedPermissions.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(modifier = Modifier.align(Alignment.End), onClick = onClose) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_ii),
                contentDescription = "",
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val title = remember(page) { PERMISSION_TITLE[displayedPermissions.elementAt(page)] }
            val rationaleText = remember(page) { PERMISSION_RATIONALE[displayedPermissions.elementAt(page)] }
            val icon = remember(page) { PERMISSION_ICONS[displayedPermissions.elementAt(page)] }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title!!,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        modifier = Modifier.height(240.dp),
                        painter = painterResource(id = icon!!),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rationaleText!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (displayedPermissions.size > 1) {
            CustomHorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = displayedPermissions.size,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = Color.Blue,
                inactiveColor = Color.Gray
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            onClick = onProceed
        ) {
            Text(
                text = stringResource(id = if (requiresSettings) R.string.launch_settings else R.string._continue),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
    LaunchedEffect(Unit) {
        onScreenLaunch()
    }
}

@Composable
fun CustomHorizontalPagerIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray,
    indicatorSize: Dp = 16.dp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { pageIndex ->
            val isSelected = pageIndex == pagerState.currentPage
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}

@Preview
@Composable
fun PermissionModalScreenPreview() {
    MultiplePermissionsRationaleScreen(
        requiresSettings = true,
        onProceed = {},
        onScreenLaunch = {},
        onClose = {},
        requiredPermissions = setOf("android.permission.CAMERA"),
    )
}