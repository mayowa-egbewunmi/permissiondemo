@file:OptIn(ExperimentalMaterial3Api::class)

package com.mayowa.permissiondemo.ui.screens.photos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.mayowa.permissiondemo.AppScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(
    navController: NavController
) {
    AppScaffold(
        canGoBack = true,
        onGoBackTapped = {
            navController.navigateUp()
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Blue
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Photo Capture Screen"
            )
        }
    }

}