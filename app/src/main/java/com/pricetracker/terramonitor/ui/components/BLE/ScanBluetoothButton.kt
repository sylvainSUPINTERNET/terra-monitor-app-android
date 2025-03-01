package com.pricetracker.terramonitor.ui.components.BLE

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat


@Composable
fun ScanBluetoothButton(modifier: Modifier) {
    val context = LocalContext.current

    val bleItemsList = remember { mutableStateListOf<String>() }

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothScanner = bluetoothManager.adapter.bluetoothLeScanner

    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled ?: false) }
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION) // Scan BLE for Android <12
    }

    val bluetoothReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    isBluetoothEnabled = (state == BluetoothAdapter.STATE_ON)
                }
            }
        }
    }

    var allPermissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }


    DisposableEffect(Unit) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothReceiver, filter)

        onDispose {
            context.unregisterReceiver(bluetoothReceiver)
        }
    }


    val callbackBle = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("ScanCallback-BLE", "onScanResult: $result")

            val deviceName = if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                result?.device?.name?: "Unknown Device"
            } else {
                "Permission Denied to access device name"
            }

            if ( !bleItemsList.contains(deviceName) ) {
                if ( deviceName.startsWith("TERRA-IOT") ) {
                    bleItemsList.add(deviceName);
                }
            }
            //val toast = Toast.makeText(context, "onScanResult: $result", Toast.LENGTH_LONG)
            //toast.show()
        }
    }



    if ( !allPermissionsGranted ) {

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissionsResult ->
                allPermissionsGranted = permissionsResult.all { it.value }
            }
        )

        Button(
            onClick = {
                if (!allPermissionsGranted) {
                    permissionLauncher.launch(requiredPermissions.toTypedArray())
                }
            }
        ) {
            Text("Scan Bluetooth")
        }
    } else {

        val enableBluetoothLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                isBluetoothEnabled = true
            }
        }

        val filters: List<ScanFilter> = emptyList()
//            listOf(
//                ScanFilter.Builder()
//                    .setDeviceName("TERRA-IOT")
//                    .build()
//            )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        Log.d("ScanCallback-BLE", "startScan ...")
        Log.d("ScanCallback-BLE", bluetoothScanner.toString());
        bluetoothScanner.startScan(filters, settings, callbackBle) // BLE scan

        if ( !isBluetoothEnabled ) {
            Column {
                Button(
                    onClick = {
                        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBluetoothLauncher.launch(enableBtIntent)
                        }
                    }
                ) {
                    Text("Search Terra Monitor ...")
                }
            }
        } else {
            LazyColumn() {
                items(bleItemsList) { item ->

                    Surface(
                        modifier = Modifier.clickable { /* Action */ },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(64.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }


//                    ElevatedCard(
//                        elevation = CardDefaults.cardElevation(
//                            defaultElevation = 6.dp
//                        ),
//                        modifier = Modifier.fillMaxWidth().height(64.dp)
//                            //.size(width = 240.dp, height = 100.dp)
//                    )  {
//                            Text(text = item)
//                        }
//                    }
                }
            }
        }

    }
}
