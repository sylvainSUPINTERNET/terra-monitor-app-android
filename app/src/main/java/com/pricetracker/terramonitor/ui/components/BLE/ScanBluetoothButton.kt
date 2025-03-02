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
import android.os.CountDownTimer
import android.util.Log
import android.util.MutableBoolean
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pricetracker.terramonitor.R
import kotlinx.coroutines.launch


@Composable
fun ScanBluetoothButton(modifier: Modifier) {
    val context = LocalContext.current
    var scanStarted by remember { mutableStateOf(false) }

//    val gradientBrush = Brush.radialGradient(
//        colors = listOf(
//            Color(0xFF833AB4), // Violet foncé
//            Color(0xFF6200EE), // Violet moyen
//            Color(0xFFB5179E)  // Rose violacé
//        )
//    )

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
        Log.d("BLE-STATE", "startScan ...")
        Log.d("ScanCallback-BLE", bluetoothScanner.toString());
        bluetoothScanner.startScan(filters, settings, callbackBle) // BLE scan
        scanStarted = true
        /*
        object: CountDownTimer(10000,1000) {
            override fun onTick(millisUntilFinished: Long) {
                //Log.d("ScanCallback-BLE", "onTick: $millisUntilFinished")
            }

            override fun onFinish() {
                Log.d("BLE-STATE", "stopScan")
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                } else {
                    bluetoothScanner.stopScan(callbackBle)
                }
            }
        }.start()

         */



        if ( !isBluetoothEnabled ) {

            val pulseAnimation = remember { mutableStateOf(true) }
            val animatedScale by animateFloatAsState(
                targetValue = if (pulseAnimation.value) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "pulseAnimation"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier=Modifier.shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            clip = true,
                            ambientColor = Color(0xFF6200EE),
                            spotColor = Color(0xFFB5179E),
                        )
//                            .graphicsLayer(
//                            scaleX = animatedScale,
//                            scaleY = animatedScale,
//                            shadowElevation = animatedScale * 8.dp.value
//                        )
                    ) {
                        Button(
                            onClick = {
                                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    enableBluetoothLauncher.launch(enableBtIntent)
                                }
                            },
                            modifier = Modifier.size(128.dp).clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6200EE),
                                contentColor = Color.White
                            )

                        ) {
                            Icon(
                                painter = painterResource(R.drawable.bluetooth),
                                contentDescription = stringResource(id = R.string.ble_icon),
                                modifier = Modifier.size(76.dp).padding(10.dp)
                            )
                        }
                    }

                }
            }
        } else {
            var deviceSelected by remember { mutableStateOf("") }
            var showDialog by remember { mutableStateOf(false) }
            val SSID = remember { mutableStateOf(TextFieldValue("")) }
            val wifiPassword = remember { mutableStateOf(TextFieldValue("")) }

            Column (){
                if ( scanStarted ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF6200EE),
                    )
                }
                Spacer(
                    modifier = Modifier.height(32.dp).padding(8.dp)
                )
                LazyColumn() {
                    items(bleItemsList) { item ->
                        Surface(
                            modifier = Modifier.clickable {
                                showDialog = true;
                                deviceSelected = item;
                            },
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


                        if (showDialog) {

                            var passwordVisible by remember { mutableStateOf(false) }

                            AlertDialog(
                                onDismissRequest = {
                                    showDialog = false
                                },
                                title = {
                                    Column (
                                        modifier = Modifier.padding(2.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text( fontWeight = FontWeight.Medium, text="WiFi Login")
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            fontWeight = FontWeight.Thin,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center,
                                            text = buildAnnotatedString {
                                                append("Add ")
                                                withStyle(style = SpanStyle(
                                                    fontWeight = FontWeight.Light,
                                                    color = Color(0xFF6200EE),
                                                    fontSize = 16.sp
                                                )) {
                                                    append(deviceSelected)
                                                }
                                                append(" to your managed devices")
                                            }
                                        )
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier.padding(2.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        OutlinedTextField(
                                            value = SSID.value,
                                            onValueChange = { SSID.value = it },
                                            label = { Text("SSID")}
                                        )
                                        OutlinedTextField(
                                            value = wifiPassword.value,
                                            onValueChange = { wifiPassword.value = it },
                                            label = { Text("Password") },
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            trailingIcon = {
                                                IconButton(onClick = {passwordVisible = !passwordVisible}){
                                                    if (passwordVisible) Icon(
                                                        painter = painterResource(R.drawable.visibility_off),
                                                        contentDescription = stringResource(id = R.string.visibility_off)
                                                    ) else Icon(
                                                        painter = painterResource(R.drawable.visibility),
                                                        contentDescription = stringResource(id = R.string.visibility)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                },
                                confirmButton = {
                                    Column(
                                        modifier= Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ){
                                        Button(
                                            onClick = {
                                                showDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF6200EE),
                                                contentColor = Color.White
                                            ),
                                        ) {
                                            Text("Confirm")
                                        }
                                    }

                                }
                            )
                        }
                    }
                }
            }
        }

    }
}
