package com.pricetracker.terramonitor.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.material3.*
@Composable
fun BluetoothPermissionButton() {
    val requestBluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                println("Permission Bluetooth accordée ✅")
            } else {
                println("Permission refusée ❌")
            }
        }
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Button(onClick = {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }) {
            Text("Demander Permission Bluetooth")
        }
    }
}