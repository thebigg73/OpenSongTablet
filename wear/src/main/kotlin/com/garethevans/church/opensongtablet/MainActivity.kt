package com.garethevans.church.opensongtablet

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black)) {
                // Remove the Box here, let MetronomeScreen define the root
                MetronomeScreen()
            }
        }
    }
}

@Preview(showBackground = true, device = "id:wearos_small_round")
@Composable
fun MetronomePreview() {
    MetronomeScreen()
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Suppress("DEPRECATION")
@Composable
fun MetronomeScreen() {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bpm by remember { mutableIntStateOf(120) }
    var beatsTop by remember { mutableIntStateOf(4) }
    var beatsBottom by remember { mutableIntStateOf(4) }
    var isPlaying by remember { mutableStateOf(false) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val prefs = remember { androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) }
    val tapTimestamps = remember { mutableStateListOf<Long>() }
    var showTempoPicker by remember { mutableStateOf(false) }
    var showTimeSigPicker by remember { mutableStateOf(false) }
    // Add a variable to hold the flash job outside the engine lambda
    var flashJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var flashIntensity by remember { mutableIntStateOf(0) } // 0=Off, 1=Dim, 2=Full

    // Intercept the back button/swipe
    BackHandler(enabled = showTempoPicker || showTimeSigPicker) {
        // If either picker is open, close them and stay in the app
        showTempoPicker = false
        showTimeSigPicker = false
    }

    // Engine: Trigger vibration inside the lambda
    // Engine: Now uses the logic based on preferences
    // Inside MetronomeScreen
    val engine = remember {
        MetronomeEngine(onTick = { isOffbeat, currentBeat ->
            println("DEBUG: Tick received, offbeat: $isOffbeat") // Check Logcat

            val mode = if (isOffbeat)
                prefs.getString("pref_off_beat_mode", "Vibrate and flash") ?: "Vibrate and flash"
            else
                prefs.getString("pref_main_beat_mode", "Vibrate and flash") ?: "Vibrate and flash"

            val intensity = prefs.getString("pref_intensity", "Normal") ?: "Normal"

            // Trigger flash
            if (mode.contains("Flash", ignoreCase = true)) {
                flashJob?.cancel()
                flashJob = scope.launch(Dispatchers.Main) {
                    // Set 2 for Downbeat, 1 for Offbeat
                    flashIntensity = if (isOffbeat) 1 else 2
                    delay(50)
                    flashIntensity = 0
                }
            }

            // Vibration
            if (mode.contains("Vibrate", ignoreCase = true)) {
                val amplitude = getAmplitude(intensity, isOffbeat)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30, amplitude))
                }
            }
        })
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Force stop everything when the user swipes away
                flashJob?.cancel()
                flashIntensity = 0
                if (isPlaying) {
                    engine.stop()
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == MetronomeListenerService.ACTION_METRONOME_SYNC) {
                    // 1. Extract values safely
                    val newBpm = intent.getIntExtra("bpm", 120)
                    val newTop = intent.getIntExtra("beatsPerBar", 4)
                    val newBottom = intent.getIntExtra("beatDenominator", 4)
                    val shouldPlay = intent.getBooleanExtra("isPlaying", false)

                    // 2. Update UI State (this triggers the screen to redraw)
                    bpm = newBpm
                    beatsTop = newTop
                    beatsBottom = newBottom
                    isPlaying = shouldPlay

                    // 3. Restart the engine with the fresh values
                    if (isPlaying) {
                        engine.stop()
                        engine.start(bpm, beatsTop, beatsBottom)
                    } else {
                        engine.stop()
                    }
                }
            }
        }

        // Consolidated Receiver Registration
        val appContext = context.applicationContext
        val filter = IntentFilter("com.garethevans.church.opensongtablet.METRONOME_SYNC")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            appContext.registerReceiver(receiver, filter)
        }

        onDispose {
            appContext.unregisterReceiver(receiver)
        }
    }

    // Apply the flash to the background
    // Outer Box controls the flash background
    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            when (flashIntensity) {
                2 -> Color.White          // Full intensity for downbeat
                1 -> Color.Gray           // Dim intensity for offbeats
                else -> Color.Black       // Off
            }
        )
    ) {
        // Inner Box should NOT have a background(Color.Black)
        Box(modifier = Modifier.fillMaxSize()) {
            // This 'if' block MUST be inside the Box to force a layout switch
            when {
                showTempoPicker -> {
                    TempoPicker(initialBpm = bpm) { selectedBpm ->
                        bpm = selectedBpm
                        if (isPlaying) engine.updateBpm(bpm)
                        showTempoPicker = false
                    }
                }

                showTimeSigPicker -> {
                    TimeSignaturePicker { newBeatsTop, newBeatsBottom ->
                        beatsTop = newBeatsTop
                        beatsBottom = newBeatsBottom
                        if (isPlaying) engine.updateBpm(bpm)
                        showTimeSigPicker = false
                    }
                }

                else -> {
                    // Your main content column
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // This Row now holds the Tempo and the Time Sig side-by-side
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Tempo Text added back
                            Text(
                                text = "$bpm",
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            val currentTime = System.currentTimeMillis()

                                            // Clear history if it's been more than 2 seconds since last tap
                                            if (tapTimestamps.isNotEmpty() && (currentTime - tapTimestamps.last() > 2000)) {
                                                tapTimestamps.clear()
                                            }

                                            tapTimestamps.add(currentTime)
                                            if (tapTimestamps.size > 4) tapTimestamps.removeAt(0)

                                            if (tapTimestamps.size >= 2) {
                                                val intervals = (1 until tapTimestamps.size).map {
                                                    tapTimestamps[it] - tapTimestamps[it - 1]
                                                }
                                                val averageInterval = intervals.average()
                                                if (averageInterval > 0) {
                                                    bpm = (60000 / averageInterval).toInt()
                                                        .coerceIn(40, 300)
                                                    if (isPlaying) engine.updateBpm(bpm)
                                                }
                                            }
                                        },
                                        onLongPress = {
                                            println("DEBUG: Long press detected!") // Check Logcat for this
                                            showTempoPicker = true
                                        }
                                    )
                                }
                            )

                            // Add a little space between the big BPM and the fraction
                            Spacer(modifier = Modifier.width(8.dp))

                            val tightStyle = TextStyle(
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy((-4).dp),
                                modifier = Modifier.clickable {
                                    println("DEBUG: Short time signature press detected!") // Check Logcat for this
                                    showTimeSigPicker = true
                                }
                            ) {
                                Text(text = "$beatsTop", style = tightStyle)

                                // Using a Box as a precise horizontal line
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)         // Adjust width here
                                        .height(4.dp)       // Adjust thickness here
                                        .padding(vertical = 1.dp) // Adds space above and below the line
                                        .background(MaterialTheme.colors.onSurface) // Matches text color
                                )

                                Text(text = "$beatsBottom", style = tightStyle)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Settings Button
                            Button(
                                onClick = {
                                    val intent = Intent(context, SettingsActivity::class.java)
                                    context.startActivity(intent)
                                },
                                // Change background to Black
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = Color.White)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.settings),
                                    contentDescription = "Settings",
                                    // Make icon slightly bigger
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Toggle Play/Stop Button
                            Button(
                                onClick = {
                                    isPlaying = !isPlaying
                                    if (isPlaying) engine.start(bpm, beatsTop, beatsBottom)
                                    else engine.stop()
                                },
                                // Change background to Black
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = Color.White)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isPlaying) R.drawable.stop else R.drawable.play),
                                    contentDescription = if (isPlaying) "Stop" else "Start",
                                    // Make icon slightly bigger
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TimeSignaturePicker(onValueSelected: (Int, Int) -> Unit) {
    val options = listOf("2/4", "3/4", "4/4", "5/4", "6/8")
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color.Black), // Enforce black background
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {
            items(options.size) { index ->
                val option = options[index]
                Chip(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    label = {
                        // Centering the text using Box
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(text = option)
                        }
                    },
                    // Enforce a dark/transparent black background for the chip
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    onClick = {
                        val parts = option.split("/")
                        onValueSelected(parts[0].toInt(), parts[1].toInt())
                    }
                )
            }
        }
    }
}

@Composable
fun TempoPicker(initialBpm: Int, onValueSelected: (Int) -> Unit) {
    val tempoOptions = (40..300 step 5).toList()
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {
            items(tempoOptions) { bpm ->
                Chip(
                    onClick = { onValueSelected(bpm) },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (bpm == initialBpm) Color.DarkGray else Color.Black.copy(alpha = 0.5f)
                    ),
                    label = {
                        // Wrapping in a Box with fillMaxWidth and contentAlignment centers the text
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$bpm BPM")
                        }
                    }
                )
            }
        }
    }
}

// Helper to calculate intensity
fun getAmplitude(intensity: String, isOffbeat: Boolean): Int {
    val base = when(intensity) {
        "Strong" -> 255
        "Weak" -> 100
        else -> 175 // Normal
    }
    return if (isOffbeat) base / 2 else base
}