package com.mayowa.permissiondemo.models

import android.Manifest

val PERMISSION_RATIONALE = mapOf(
    Manifest.permission.CAMERA to "We need access to your camera to capture photos.",
    Manifest.permission.ACCESS_MEDIA_LOCATION to "We need access to your media location to organize your photos by location.",
)