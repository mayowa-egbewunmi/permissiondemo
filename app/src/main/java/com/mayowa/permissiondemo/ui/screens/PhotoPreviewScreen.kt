package com.mayowa.permissiondemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.ui.composables.AppAsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewScreen(
    filePath: String,
) {
    AppScaffold(
        canGoBack = true,
        onGoBackTapped = {},
        modifier = Modifier.fillMaxSize(),
        topBar = {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AppAsyncImage(
                modifier = Modifier.fillMaxSize(),
                filePath = filePath
            )
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f))
            ) {
                IconButton(modifier = Modifier.align(Alignment.CenterEnd), onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun PhotoPreviewScreenPreview() {
    PhotoPreviewScreen(
        filePath = "https://images.pexels.com/photos/7534339/pexels-photo-7534339.jpeg?auto=compress&cs=tinysrgb&w=800&lazy=load"
    )
}