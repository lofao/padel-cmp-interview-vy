package io.bifri.interview.cmp.padel.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined

@Composable
@Suppress("ModifierMissing")
actual fun LocationPermissionEffect() {
    val locationManager = remember { CLLocationManager() }
    LaunchedEffect(locationManager) {
        if (locationManager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
            locationManager.requestWhenInUseAuthorization()
        }
    }
}
