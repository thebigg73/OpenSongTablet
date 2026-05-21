package com.garethevans.church.opensongtablet

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText

@Composable
fun SettingsScreen(context: Context) {
    // 1. Create the state
    val listState = rememberScalingLazyListState()

    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }

    // State to toggle between the list and the picker
    var showPicker by remember { mutableStateOf(false) }

    // State to know which preference we are editing
    var editingKey by remember { mutableStateOf("") }

    // Change these default values from "Off" to "Vibrate and flash"
    var mainBeatMode by remember {
        mutableStateOf(prefs.getString("pref_main_beat_mode", "Vibrate and flash") ?: "Vibrate and flash")
    }

    var offBeatMode by remember {
        mutableStateOf(prefs.getString("pref_off_beat_mode", "Vibrate and flash") ?: "Vibrate and flash")
    }

    var intensity by remember {
        mutableStateOf(prefs.getString("pref_intensity", "Normal") ?: "Normal")
    }

    if (showPicker) {
        val options = if (editingKey == "pref_intensity")
            listOf("Strong", "Normal", "Weak")
        else listOf("Off", "Flash", "Vibrate", "Vibrate and flash")

        SelectionScreen(
            options = options,
            onOptionSelected = { selected ->
                prefs.edit { putString(editingKey, selected) }
                when(editingKey) {
                    "pref_main_beat_mode" -> mainBeatMode = selected
                    "pref_off_beat_mode" -> offBeatMode = selected
                    "pref_intensity" -> intensity = selected
                }
                showPicker = false
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            timeText = { TimeText() },
            // 2. Attach the indicator
            positionIndicator = {
                PositionIndicator(scalingLazyListState = listState)
            }
        ) {
            ScalingLazyColumn(
                state = listState, // 3. Link the state to the column
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 30.dp, bottom = 30.dp)
            ) {
                item { ListHeader { Text("Main Beat") } }
                item {
                    Chip(
                        label = { Text("Feedback Type") },
                        onClick = { editingKey = "pref_main_beat_mode"; showPicker = true },
                        secondaryLabel = { Text(mainBeatMode) }
                    )
                }

                item { ListHeader { Text("Off Beat") } }
                item {
                    Chip(
                        label = { Text("Feedback Type") },
                        onClick = { editingKey = "pref_off_beat_mode"; showPicker = true },
                        secondaryLabel = { Text(offBeatMode) }
                    )
                }

                item { ListHeader { Text("Intensity") } }
                item {
                    Chip(
                        label = { Text("Pulse Strength") },
                        onClick = { editingKey = "pref_intensity"; showPicker = true },
                        secondaryLabel = { Text(intensity) }
                    )
                }

            }
        }
    }
}

@Composable
fun SelectionScreen(
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    val listState = rememberScalingLazyListState()
    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Centers the chips
        ) {
            items(options) { option ->
                Chip(
                    modifier = Modifier
                        .fillMaxWidth(0.9f), // Keeps them from touching the screen edges
                    label = {
                        // This Box centers the text horizontally and vertically
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = option)
                        }
                    },
                    // Set the background color here
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color.Black.copy(alpha = 0.5f) // Semi-transparent black
                    ),
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}