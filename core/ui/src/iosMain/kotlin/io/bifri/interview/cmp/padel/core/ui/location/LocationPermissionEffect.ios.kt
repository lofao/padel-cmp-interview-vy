package io.bifri.interview.cmp.padel.core.ui.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.CoreLocation.CLLocationManager

@Composable
actual fun LocationPermissionEffect() {
    LaunchedEffect(Unit) {
        val locationManager = CLLocationManager()
        locationManager.requestWhenInUseAuthorization()
    }
}
