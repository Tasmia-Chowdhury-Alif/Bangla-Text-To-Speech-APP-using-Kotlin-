package com.example.banglatexttospeech

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
import java.util.*

class MainActivity : ComponentActivity() {

    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    lateinit var socket: BluetoothSocket

    private val enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth has been enabled
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
        } else {
            // For Android versions below 12, request only the location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
//        Request Bluetooth permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1
//            )
//        }

        // Initialize Bluetooth adapter
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
            return
        }

        // Automatically prompt to enable Bluetooth if it's not enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        }
        // Automatically prompt to enable Bluetooth if it's not enabled
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
////            startActivityForResult(enableBtIntent, 1)
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                startActivityForResult(enableBtIntent, 1)
//            } else {
//                Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
//                // Optionally, request the permission if not granted.}
//
//            }

            // Compose UI setup
            enableEdgeToEdge()
            setContent {
                BanglaTextToSpeechTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                        // Call a function that displays the Bluetooth devices
                        val pairedDevices = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
                        } else {
                            Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
                            emptyList() // If no permission, return an empty list
                        }

                        BluetoothDeviceList(
                            deviceList = pairedDevices.map { "${it.name}\n${it.address}" },
                            onDeviceClick = { deviceInfo ->
                                val deviceAddress = deviceInfo.substring(deviceInfo.length - 17)
                                val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
                                try {
                                    // Connect to the selected Bluetooth device
                                    if (ActivityCompat.checkSelfPermission(
                                            this@MainActivity,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                                        socket.connect()

                                        // After connection, start the BluetoothReceiverActivity
                                        val intent = Intent(this@MainActivity, BluetoothReceiverActivity::class.java)
                                        intent.putExtra("DEVICE_ADDRESS", deviceAddress)
                                        startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Failed to connect", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }


//            setContent {
//                BanglaTextToSpeechTheme {
//                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                        // Show the Bluetooth device list
////                    val pairedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
//                        val pairedDevices = if (ActivityCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.BLUETOOTH_CONNECT
//                            ) == PackageManager.PERMISSION_GRANTED
//                        ) {
//                            bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
//                        } else {
//                            Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
//                            emptyList() // If no permission, return an empty list
//                        }
//
//                        BluetoothDeviceList(
//                            deviceList = pairedDevices.map { "${it.name}\n${it.address}" },
//                            onDeviceClick = { deviceInfo ->
//                                val deviceAddress = deviceInfo.substring(deviceInfo.length - 17)
//                                val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
//                                try {
//                                    if (ActivityCompat.checkSelfPermission(
//                                            this,
//                                            Manifest.permission.BLUETOOTH_CONNECT
//                                        ) == PackageManager.PERMISSION_GRANTED
//                                    ) {
//                                        socket = device.createRfcommSocketToServiceRecord(MY_UUID)
//                                        socket.connect()
//                                    } else {
//                                        Toast.makeText(
//                                            this,
//                                            "Bluetooth permission required",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//
//
////                                // After connecting, move to the next activity for receiving data
////                                val intent = Intent(this@MainActivity, BluetoothReceiverActivity::class.java)
////                                startActivity(intent)
//
//                                    // Pass the BluetoothSocket to the next activity
//                                    val intent = Intent(
//                                        this@MainActivity,
//                                        BluetoothReceiverActivity::class.java
//                                    ).apply {
////                                    putExtra("BLUETOOTH_SOCKET", socket)
//                                        val intent = Intent(
//                                            this@MainActivity,
//                                            BluetoothReceiverActivity::class.java
//                                        ).apply {
//                                            putExtra(
//                                                "DEVICE_ADDRESS",
//                                                deviceAddress
//                                            ) // Pass device address instead
//                                        }
//                                        startActivity(intent)
//                                    }
//                                    startActivity(intent)
//
//                                } catch (e: Exception) {
//                                    Toast.makeText(
//                                        this@MainActivity,
//                                        "Failed to connect",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        )
//                    }
//                }
//            }
        }
    }

    @Composable
    fun BluetoothDeviceList(deviceList: List<String>, onDeviceClick: (String) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Available Bluetooth Devices")
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(deviceList) { device ->
                    Text(
                        text = device,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onDeviceClick(device) }
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        BanglaTextToSpeechTheme {
            BluetoothDeviceList(deviceList = listOf("Device 1\n00:11:22:33:AA:BB")) { }
        }
    }

//package com.example.banglatexttospeech
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//
//    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    private lateinit var socket: BluetoothSocket
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request Bluetooth permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1
//            )
//        }
//
//        // Initialize Bluetooth adapter
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//
//        // Check if Bluetooth is supported on the device
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        // Automatically prompt to enable Bluetooth if it's not enabled
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, 1)
//        }
//
//        // Compose UI setup
//        enableEdgeToEdge()
//        setContent {
//            BanglaTextToSpeechTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    val pairedDevices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
//                    BluetoothDeviceList(
//                        deviceList = pairedDevices.map { "${it.name}\n${it.address}" },
//                        onDeviceClick = { deviceInfo ->
//                            val deviceAddress = deviceInfo.substring(deviceInfo.length - 17)
//                            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
//                            try {
//                                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
//                                socket.connect()
//
//                                // Pass the BluetoothSocket to the next activity
//                                val intent = Intent(this@MainActivity, BluetoothReceiverActivity::class.java).apply {
//                                    putExtra("BLUETOOTH_SOCKET", socket)
//                                }
//                                startActivity(intent)
//
//                            } catch (e: Exception) {
//                                Toast.makeText(this@MainActivity, "Failed to connect", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BluetoothDeviceList(deviceList: List<String>, onDeviceClick: (String) -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = "Available Bluetooth Devices")
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyColumn {
//            items(deviceList) { device ->
//                Text(
//                    text = device,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                        .clickable { onDeviceClick(device) }
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    BanglaTextToSpeechTheme {
//        BluetoothDeviceList(deviceList = listOf("Device 1\n00:11:22:33:AA:BB")) { }
//    }
//}




//// optimize 2 UI ready and mostly working but not showing the text and does not tts
//package com.example.banglatexttospeech
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//
//    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    lateinit var socket: BluetoothSocket
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request Bluetooth permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1
//            )
//        }
//
//        // Initialize Bluetooth adapter
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//
//        // Check if Bluetooth is supported on the device
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        if (!bluetoothAdapter.isEnabled) {
//            // If Bluetooth is off, request the user to enable it
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            startActivityForResult(enableBtIntent, 1)
//        }
//
//        // Compose UI setup
//        enableEdgeToEdge()
//        setContent {
//            BanglaTextToSpeechTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    // Show the Bluetooth device list
//                    val pairedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
//                    BluetoothDeviceList(
//                        deviceList = pairedDevices.map { "${it.name}\n${it.address}" },
//                        onDeviceClick = { deviceInfo ->
//                            val deviceAddress = deviceInfo.substring(deviceInfo.length - 17)
//                            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
//                            try {
//                                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
//                                socket.connect()
//
//                                // After connecting, move to the next activity for receiving data
//                                val intent = Intent(this@MainActivity, BluetoothReceiverActivity::class.java)
//                                startActivity(intent)
//
//                            } catch (e: Exception) {
//                                Toast.makeText(this@MainActivity, "Failed to connect", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BluetoothDeviceList(deviceList: List<String>, onDeviceClick: (String) -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = "Available Bluetooth Devices")
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyColumn {
//            items(deviceList) { device ->
//                Text(
//                    text = device,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                        .clickable { onDeviceClick(device) }
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    BanglaTextToSpeechTheme {
//        BluetoothDeviceList(deviceList = listOf("Device 1\n00:11:22:33:AA:BB")) { }
//    }
//}



// optimized 1
//package com.example.banglatexttospeech
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.text.BasicText
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//import java.util.*
//
//class MainActivity : ComponentActivity() {
//
//    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    lateinit var socket: BluetoothSocket
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request Bluetooth permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1
//            )
//        }
//
//        // Initialize Bluetooth adapter
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//
//        // Check if Bluetooth is supported on the device
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
//        } else if (!bluetoothAdapter.isEnabled) {
//            // If Bluetooth is off, request the user to enable it
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, 1)
//        }
//
//        // Compose UI setup
//        enableEdgeToEdge()
//        setContent {
//            BanglaTextToSpeechTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    // Show the Bluetooth device list first
//                    val pairedDevices = if (ActivityCompat.checkSelfPermission(
//                            this,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//                    } else {
//                    }
//                    bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
//                    BluetoothDeviceList(
//                        deviceList = pairedDevices.map { "${it.name}\n${it.address}" },
//                        onDeviceClick = { deviceInfo ->
//                            val deviceAddress = deviceInfo.substring(deviceInfo.length - 17)
//                            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
//                            try {
//                                if (device != null) {
//                                    socket = device.createRfcommSocketToServiceRecord(MY_UUID)
//                                }
//                                socket.connect()
//
//                                // After connecting, move to the next page (Activity) for receiving data
//                                val intent = Intent(this@MainActivity, BluetoothReceiverActivity::class.java)
//                                startActivity(intent)
//
//                            } catch (e: Exception) {
//                                Toast.makeText(this@MainActivity, "Failed to connect", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BluetoothDeviceList(deviceList: List<String>, onDeviceClick: (String) -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = "Available Bluetooth Devices")
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyColumn {
//            items(deviceList) { device ->
//                Text(
//                    text = device,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                        .clickable { onDeviceClick(device) }
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    BanglaTextToSpeechTheme {
//        BluetoothDeviceList(deviceList = listOf("Device 1\n00:11:22:33:AA:BB")) { }
//    }
//}



// before Chatgpt fix
//package com.example.banglatexttospeech
//
//import android.bluetooth.BluetoothAdapter
//import android.Manifest
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.widget.ArrayAdapter
//import android.widget.ListView
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.BasicText
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request Bluetooth permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1
//            )
//        }
//
//        // Initialize Bluetooth adapter
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//
//        // Check if Bluetooth is supported on the device
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
//        } else if (!bluetoothAdapter.isEnabled) {
//            // If Bluetooth is off, request the user to enable it
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, 1)
//        }
//
//        // populate the ListView with available Bluetooth devices
//        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
//        val deviceList = ArrayList<String>()
//        val deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
//        val listView: ListView = findViewById(R.id.bluetooth_device_list)
//
//        pairedDevices?.forEach { device ->
//            val deviceName = if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            } else {
//
//            }
//            device.name
//            val deviceAddress = device.address
//            deviceList.add("$deviceName\n$deviceAddress")
//        }
//        listView.adapter = deviceAdapter
//
//        listView.setOnItemClickListener { _, _, position, _ ->
//            val deviceInfo = deviceList[position]
//            Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show()
//            // Extract device address and start Bluetooth connection here
//        }
//
////        set up a Bluetooth connection
//        listView.setOnItemClickListener { _, _, position, _ ->
//            val deviceInfo = deviceList[position]
//            val deviceAddress = deviceInfo.substring(deviceInfo.length - 17)
//            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
//
//            val socket: BluetoothSocket = device?.createRfcommSocketToServiceRecord(MY_UUID) ?:
//            socket.connect()
//
//            // After connecting, move to the next page (Activity) for receiving data
//            val intent = Intent(this, BluetoothReceiverActivity::class.java)
//            startActivity(intent)
//
//        }
//
//
//
//        // Compose UI setup
//        enableEdgeToEdge()
//        setContent {
//            BanglaTextToSpeechTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    // Show the Bluetooth device list first
//                    BluetoothDeviceList()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BluetoothDeviceList() {
//    // TODO: Add logic to display available Bluetooth devices
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = "Available Bluetooth Devices")
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = {
//            // Add logic to handle device selection
//        }) {
//            Text("Connect to Device")
//        }
//    }
//}
//
//@Composable
//fun DisplayReceivedText(text: String, pitch: Float, speed: Float) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.SpaceBetween,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//                .padding(16.dp)
//                .background(MaterialTheme.colorScheme.surface),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = text,
//                style = MaterialTheme.typography.h5,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        // SeekBars for pitch and speed
//        Column(modifier = Modifier.fillMaxWidth()) {
//            Text("Voice Pitch")
//            Slider(
//                value = pitch,
//                onValueChange = { /* Update pitch */ },
//                valueRange = 0.5f..2.0f,
//                modifier = Modifier.padding(16.dp)
//            )
//            Text("Voice Speed")
//            Slider(
//                value = speed,
//                onValueChange = { /* Update speed */ },
//                valueRange = 0.5f..2.0f,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        Text(
//            text = "This app is Developed by Tasmia Chowdhury Alif",
//            modifier = Modifier.padding(16.dp),
//            style = MaterialTheme.typography.body2
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    BanglaTextToSpeechTheme {
//        BluetoothDeviceList()
//    }
//}



//package com.example.banglatexttospeech
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request Bluetooth permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), 1
//            )
//        }
//
//        // Continue with Jetpack Compose UI setup
//        enableEdgeToEdge()
//        setContent {
//            BanglaTextToSpeechTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    BanglaTextToSpeechTheme {
//        Greeting("Android")
//    }
//}