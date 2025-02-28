package com.pricetracker.terramonitor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pricetracker.terramonitor.ui.components.BLE.ScanBluetoothButton
import com.pricetracker.terramonitor.ui.components.BluetoothButton
import com.pricetracker.terramonitor.ui.components.BluetoothPermissionButton
import com.pricetracker.terramonitor.ui.theme.TerraMonitorTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter;
        bluetoothAdapter.bluetoothLeScanner;

        enableEdgeToEdge()
        setContent {
            TerraMonitorTheme {
                Scaffold(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    ScanBluetoothButton()
                    //BluetoothPermissionButton()
                    //BluetoothButton()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TerraMonitorTheme {
        Greeting("Android")
    }
}