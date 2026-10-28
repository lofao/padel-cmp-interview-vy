package io.bifri.interview.cmp.padel.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.CoreLocation.CLLocationManager

@Composable
@Suppress("ModifierMissing")
actual fun LocationPermissionEffect() {
    LaunchedEffect(Unit) {
        val locationManager = CLLocationManager()
        locationManager.requestWhenInUseAuthorization()
    }
}
