package com.garethevans.church.opensongtablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material.MaterialTheme

class SettingsActivity : ComponentActivity() { // Use ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Your Wear Compose theme here
            MaterialTheme {
                SettingsScreen(this)
            }
        }
    }
}