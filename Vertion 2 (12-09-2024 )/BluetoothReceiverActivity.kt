package com.example.banglatexttospeech

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class BluetoothReceiverActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var bluetoothSocket: BluetoothSocket
    private var pitch: Float = 1.0f
    private var speed: Float = 1.0f
    private var receivedText by mutableStateOf("আপনার বার্তা এখানে দেখানো হবে")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the BluetoothSocket from the intent
        bluetoothSocket = intent.getParcelableExtra("BLUETOOTH_SOCKET") ?: return

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        setContent {
            BanglaTextToSpeechTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    BluetoothReceiverScreen(
                        modifier = Modifier.padding(paddingValues),
                        onPitchChange = { newPitch -> pitch = newPitch },
                        onSpeedChange = { newSpeed -> speed = newSpeed },
                        onTextReceive = { text ->
                            tts.setPitch(pitch)
                            tts.setSpeechRate(speed)
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        receivedText = receivedText
                    )
                }
            }
        }

        // Start listening for incoming data
        listenForBluetoothData()
    }

    private fun listenForBluetoothData() {
        val inputStream = bluetoothSocket.inputStream

        // Coroutine to listen for data in the background
        lifecycleScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024) // buffer store for the stream
            var bytes: Int

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer)
                    val text = String(buffer, 0, bytes)

                    // Update the received text in the main thread
                    withContext(Dispatchers.Main) {
                        receivedText = text // Display text in the UI
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("bn", "BD") // Set language to Bangle (Bangladesh)
        }
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

// gpt next edit
//@Composable
//fun BluetoothReceiverScreen(
//    modifier: Modifier,
//    onPitchChange: (Float) -> Unit,
//    onSpeedChange: (Float) -> Unit,
//    onTextReceive: (String) -> Unit,
//    receivedText: String
//) {
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = receivedText,
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Black,
//            modifier = Modifier.padding(16.dp)
//        )
//
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            SliderWithLabel(
//                label = "Pitch",
//                onValueChange = onPitchChange,
//                modifier = Modifier.weight(1f)
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            SliderWithLabel(
//                label = "Speed",
//                onValueChange = onSpeedChange,
//                modifier = Modifier.weight(1f)
//            )
//        }
//    }
//}
//
//@Composable
//fun SliderWithLabel(label: String, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
//    Column(modifier = modifier) {
//        Text(text = label, fontSize = 16.sp)
//        Slider(value = 1.0f, onValueChange = onValueChange, valueRange = 0.5f..2.0f)
//    }
//}


// existing and working ui
@Composable
fun BluetoothReceiverScreen(
    modifier: Modifier = Modifier,
    onPitchChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onTextReceive: (String) -> Unit,
    receivedText: String
) {
    var pitch by remember { mutableStateOf(1.0f) }
    var speed by remember { mutableStateOf(1.0f) }
    var receivedText by remember { mutableStateOf("আপনার বার্তা এখানে দেখানো হবে") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(7f)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = receivedText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .background(Color(0xFFCCCCCC)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pitch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = pitch,
                onValueChange = {
                    pitch = it
                    onPitchChange(pitch)
                },
                valueRange = 0.5f..2.0f,
                steps = 6,
                modifier = Modifier.padding(16.dp)
            )

            Text("Speed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = speed,
                onValueChange = {
                    speed = it
                    onSpeedChange(speed)
                },
                valueRange = 0.5f..2.0f,
                steps = 6,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = "This app is developed by Tasmia Chowdhury Alif",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF5722),
            modifier = Modifier.padding(16.dp)
        )
    }

    LaunchedEffect(Unit) {
        receivedText = "বাংলা টেক্সট এখানে দেখানো হবে"
        onTextReceive(receivedText)
    }
}







// optimize 2 UI ready and mostly working but not showing the text and does not tts
//package com.example.banglatexttospeech
//
//import android.os.Bundle
//import android.speech.tts.TextToSpeech
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//import java.util.*
//
//class BluetoothReceiverActivity : ComponentActivity(), TextToSpeech.OnInitListener {
//
//    private lateinit var tts: TextToSpeech
//    private var pitch: Float = 1.0f
//    private var speed: Float = 1.0f
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize TextToSpeech
//        tts = TextToSpeech(this, this)
//
//        setContent {
//            BanglaTextToSpeechTheme {
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { paddingValues ->
//                    BluetoothReceiverScreen(
//                        modifier = Modifier.padding(paddingValues),
//                        onPitchChange = { newPitch -> pitch = newPitch },
//                        onSpeedChange = { newSpeed -> speed = newSpeed },
//                        onTextReceive = { text ->
//                            tts.setPitch(pitch)
//                            tts.setSpeechRate(speed)
//                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            tts.language = Locale("bn", "BD") // Set language to Bangle (Bangladesh)
//        }
//    }
//
//    override fun onDestroy() {
//        if (this::tts.isInitialized) {
//            tts.stop()
//            tts.shutdown()
//        }
//        super.onDestroy()
//    }
//}
//
//@Composable
//fun BluetoothReceiverScreen(
//    modifier: Modifier = Modifier,
//    onPitchChange: (Float) -> Unit,
//    onSpeedChange: (Float) -> Unit,
//    onTextReceive: (String) -> Unit
//) {
//    var pitch by remember { mutableStateOf(1.0f) }
//    var speed by remember { mutableStateOf(1.0f) }
//    var receivedText by remember { mutableStateOf("আপনার বার্তা এখানে দেখানো হবে") }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.SpaceBetween,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(7f)
//                .background(Color.White),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = receivedText,
//                fontSize = 24.sp,
//                color = Color.Black,
//                modifier = Modifier.padding(16.dp),
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f)
//                .background(Color(0xFFCCCCCC)),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Pitch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = pitch,
//                onValueChange = {
//                    pitch = it
//                    onPitchChange(pitch)
//                },
//                valueRange = 0.5f..2.0f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            Text("Speed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = speed,
//                onValueChange = {
//                    speed = it
//                    onSpeedChange(speed)
//                },
//                valueRange = 0.5f..2.0f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        Text(
//            text = "This app is developed by Tasmia Chowdhury Alif",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFFFF5722),
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//
//    LaunchedEffect(Unit) {
//        receivedText = "বাংলা টেক্সট এখানে দেখানো হবে"
//        onTextReceive(receivedText)
//    }
//}




// optimize 1
//package com.example.banglatexttospeech
//
//import android.os.Bundle
//import android.speech.tts.TextToSpeech
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//import java.util.*
//
//class BluetoothReceiverActivity : ComponentActivity(), TextToSpeech.OnInitListener {
//
//    private lateinit var tts: TextToSpeech
//    private var pitch: Float = 1.0f
//    private var speed: Float = 1.0f
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize TextToSpeech
//        tts = TextToSpeech(this, this)
//
//        setContent {
//            BanglaTextToSpeechTheme {
//                // Scaffold to hold the layout structure
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { paddingValues ->
//                    BluetoothReceiverScreen(
//                        modifier = Modifier.padding(paddingValues),
//                        onPitchChange = { newPitch -> pitch = newPitch },
//                        onSpeedChange = { newSpeed -> speed = newSpeed },
//                        onTextReceive = { text ->
//                            // Use TextToSpeech to speak the received text
//                            LaunchedEffect(text) {
//                                tts.setPitch(pitch)
//                                tts.setSpeechRate(speed)
//                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            tts.language = Locale("bn", "BD") // Set language to Bangla (Bangladesh)
//        }
//    }
//
//    override fun onDestroy() {
//        if (this::tts.isInitialized) {
//            tts.stop()
//            tts.shutdown()
//        }
//        super.onDestroy()
//    }
//}
//
//@Composable
//fun BluetoothReceiverScreen(
//    modifier: Modifier = Modifier,
//    onPitchChange: (Float) -> Unit,
//    onSpeedChange: (Float) -> Unit,
//    onTextReceive: @Composable (String) -> Unit
//) {
//    var pitch by remember { mutableStateOf(3f) }
//    var speed by remember { mutableStateOf(3f) }
//    var receivedText by remember { mutableStateOf("আপনার বার্তা এখানে দেখানো হবে") }
//
//    Column(
//        modifier = modifier.fillMaxSize().padding(16.dp),
//        verticalArrangement = Arrangement.SpaceBetween,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(7f)
//                .background(Color.White),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = receivedText,
//                fontSize = 24.sp,
//                color = Color.Black,
//                modifier = Modifier.padding(16.dp),
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f)
//                .background(Color(0xFFCCCCCC)),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Pitch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = pitch,
//                onValueChange = {
//                    pitch = it
//                    onPitchChange(pitch / 3.0f)
//                },
//                valueRange = 0f..6f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            Text("Speed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = speed,
//                onValueChange = {
//                    speed = it
//                    onSpeedChange(speed / 3.0f)
//                },
//                valueRange = 0f..6f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        Text(
//            text = "This app is developed by Tasmia Chowdhury Alif",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFFFF5722),
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//
//    LaunchedEffect(Unit) {
//        receivedText = "বাংলা টেক্সট এখানে দেখানো হবে"
//        onTextReceive(receivedText)
//    }
//}




// before fix
//package com.example.banglatexttospeech
//
//import android.os.Bundle
//import android.speech.tts.TextToSpeech
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//import java.util.*
//
//class BluetoothReceiverActivity : ComponentActivity(), TextToSpeech.OnInitListener {
//
//    private lateinit var tts: TextToSpeech
//    private var pitch: Float = 1.0f
//    private var speed: Float = 1.0f
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize TextToSpeech
//        tts = TextToSpeech(this, this)
//
//        setContent {
//            BanglaTextToSpeechTheme {
//                // Scaffold to hold the layout structure
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { paddingValues -> // Here we use paddingValues
//                    BluetoothReceiverScreen(
//                        modifier = Modifier.padding(paddingValues), // Apply padding
//                        pitch = pitch,
//                        speed = speed,
//                        onPitchChange = { newPitch -> pitch = newPitch },
//                        onSpeedChange = { newSpeed -> speed = newSpeed },
//                        onTextReceive = { text ->
//                            // Ensure TextToSpeech is set up in a side effect
//                            LaunchedEffect(text) {
//                                tts.setPitch(pitch)
//                                tts.setSpeechRate(speed)
//                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            tts.language = Locale("bn", "BD") // Set language to Bangla (Bangladesh)
//        }
//    }
//
//    override fun onDestroy() {
//        // Release TextToSpeech resources
//        if (this::tts.isInitialized) {
//            tts.stop()
//            tts.shutdown()
//        }
//        super.onDestroy()
//    }
//}
//
//@Composable
//fun BluetoothReceiverScreen(
//    onPitchChange: (Float) -> Unit,
//    onSpeedChange: (Float) -> Unit,
//    onTextReceive: @Composable (String) -> Unit
//) {
//    var pitch by remember { mutableStateOf(3f) } // Default pitch
//    var speed by remember { mutableStateOf(3f) } // Default speed
//    var receivedText by remember { mutableStateOf("আপনার বার্তা এখানে দেখানো হবে") } // Default received text
//
//    // Main layout
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.SpaceBetween,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // White block for text display (70% height)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(7f)
//                .background(Color.White),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = receivedText,
//                fontSize = 24.sp,
//                color = Color.Black,
//                modifier = Modifier.padding(16.dp),
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center
//            )
//        }
//
//        // Gray block for SeekBars (pitch and speed control) (20% height)
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f)
//                .background(Color(0xFFCCCCCC)),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // SeekBar for pitch control
//            Text("Pitch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = pitch,
//                onValueChange = {
//                    pitch = it
//                    onPitchChange(pitch / 3.0f) // Adjust pitch scale
//                },
//                valueRange = 0f..6f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            // SeekBar for speed control
//            Text("Speed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = speed,
//                onValueChange = {
//                    speed = it
//                    onSpeedChange(speed / 3.0f) // Adjust speed scale
//                },
//                valueRange = 0f..6f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        // Footer block (10% height)
//        Text(
//            text = "This app is developed by Tasmia Chowdhury Alif",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFFFF5722),
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//
//    // Simulate receiving text from Bluetooth (for now, until you connect to real Bluetooth device)
//    LaunchedEffect(Unit) {
//        receivedText = "বাংলা টেক্সট এখানে দেখানো হবে"
//        onTextReceive(receivedText)
//    }
//}





//package com.example.banglatexttospeech
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.BasicText
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.banglatexttospeech.ui.theme.BanglaTextToSpeechTheme
//
//class BluetoothReceiverActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            BanglaTextToSpeechTheme {
//                // Scaffold to hold the layout structure
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    BluetoothReceiverScreen()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BluetoothReceiverScreen() {
//    var pitch by remember { mutableStateOf(3f) } // Default pitch
//    var speed by remember { mutableStateOf(3f) } // Default speed
//    var receivedText by remember { mutableStateOf("Received Text Here") } // Replace with actual received text
//
//    // Main layout
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.SpaceBetween,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // White block for text display (70% height)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(7f)
//                .background(Color.White),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = receivedText,
//                fontSize = 24.sp,
//                color = Color.Black,
//                modifier = Modifier.padding(16.dp),
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center
//            )
//        }
//
//        // Gray block for SeekBars (pitch and speed control) (20% height)
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f)
//                .background(Color(0xFFCCCCCC)),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // SeekBar for pitch control
//            Text("Pitch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = pitch,
//                onValueChange = { pitch = it },
//                valueRange = 0f..6f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            // SeekBar for speed control
//            Text("Speed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            Slider(
//                value = speed,
//                onValueChange = { speed = it },
//                valueRange = 0f..6f,
//                steps = 6,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        // Footer block (10% height)
//        Text(
//            text = "This app is developed by Tasmia Chowdhury Alif",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFFFF5722),
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}
