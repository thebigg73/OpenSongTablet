package com.garethevans.church.opensongtablet

import android.os.SystemClock
import kotlinx.coroutines.*

class MetronomeEngine(
    private val onTick: (isOffbeat: Boolean, currentBeat: Int) -> Unit
) {
    private var job: Job? = null
    private var isPlaying = false
    private var bpm = 120
    private var beatsTop = 4
    private var beatsBottom = 4

    // Inside MetronomeEngine.kt

    fun start(bpm: Int, beatsTop: Int, beatsBottom: Int) {
        // 1. Force a clean stop first
        stop()

        this.bpm = bpm
        this.beatsTop = beatsTop
        this.beatsBottom = beatsBottom
        isPlaying = true

        job = CoroutineScope(Dispatchers.Default).launch {
            // 2. EXPLICITLY reset the counter here
            var currentBeat = 0

            while (isPlaying) {
                val start = SystemClock.elapsedRealtime()

                // Logic: isOffbeat is true if it's not the downbeat (0)
                val isOffbeat = (currentBeat != 0)

                withContext(Dispatchers.Main) {
                    onTick(isOffbeat, currentBeat)
                }

                // 3. Move to next beat
                currentBeat = (currentBeat + 1) % beatsTop

                // Calculate the base interval for a quarter note (the BPM value)
                val quarterNoteInterval = 60000L / bpm

                // Adjust based on the denominator
                // If denominator is 8, the pulse should be half as long (eighth notes)
                val interval = if (beatsBottom == 8) {
                    quarterNoteInterval / 2
                } else {
                    quarterNoteInterval
                }

                val elapsed = SystemClock.elapsedRealtime() - start
                val delayTime = (interval - elapsed).coerceAtLeast(0)
                delay(delayTime)
            }
        }
    }

    fun stop() {
        isPlaying = false
        job?.cancel() // Safely cancels the coroutine
    }

    fun updateBpm(newBpm: Int) {
        this.bpm = newBpm
        if (isPlaying) {
            stop()
            start(bpm, beatsTop, beatsBottom)
        }
    }
}