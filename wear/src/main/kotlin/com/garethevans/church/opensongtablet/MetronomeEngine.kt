package com.garethevans.church.opensongtablet

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MetronomeEngine(
    private val onTick: (isOffbeat: Boolean, currentBeat: Int) -> Unit
) {
    private var job: Job? = null
    private var isPlaying = false
    private val mutex = Mutex() // Added Mutex

    private var bpm = 120
    private var beatsTop = 4
    private var beatsBottom = 4



    fun start(bpmReceived: Int, beatsTopReceived: Int, beatsBottomReceived: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            mutex.withLock { // Prevents overlapping start/stop
                stop() // Private helper

                bpm = bpmReceived
                beatsTop = beatsTopReceived
                beatsBottom = beatsBottomReceived
                isPlaying = true

                job = CoroutineScope(Dispatchers.Default).launch {
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
        }
    }

    private var lastStopTimestamp: Long = 0L // Tracks the last stop time

    fun stop() {
        val currentTime = SystemClock.elapsedRealtime()

        // Only proceed if 100 ms has passed since the last stop
        if (currentTime - lastStopTimestamp < 100) {
            return
        }

        lastStopTimestamp = currentTime // Update timestamp
        isPlaying = false
        job?.cancel()
    }


    fun updateBpm(newBpm: Int) {
        this.bpm = newBpm
        if (isPlaying) {
            stop()
            start(bpm, beatsTop, beatsBottom)
        }
    }
}