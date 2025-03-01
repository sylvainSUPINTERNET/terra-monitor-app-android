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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
            TerraMonitorTheme(
                darkTheme=false,
                dynamicColor=false
            ) {
                Scaffold(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars).windowInsetsPadding(WindowInsets.navigationBars)
                ) { innerPadding ->
                    Column {
                        Box(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row {
                                Icon(
                                    painter = painterResource(R.drawable.link),
                                    contentDescription = stringResource(id = R.string.pairing_icon),
                                    modifier = Modifier.size(64.dp).padding(10.dp)
                                )
                                Text(
                                    text = "Pairing",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(CenterVertically)
                                )
                            }
                        }
                        Box(
                            //modifier = Modifier.padding(62.dp)
                        ) {
                            ScanBluetoothButton(modifier = Modifier.padding(innerPadding))
                        }
                    }
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