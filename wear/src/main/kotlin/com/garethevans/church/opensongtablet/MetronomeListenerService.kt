package com.garethevans.church.opensongtablet

import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * Listens for metronome beat messages from the phone and vibrates the watch in time.
 * No deprecated APIs; compatible with Play Services 18+ and Wear OS 4+.
 */
class MetronomeListenerService : WearableListenerService() {
    private val tickLength = 50
    private val tockLength = 30
    private val tickAmplitude = 255
    private val tockAmplitude = 120

    private var vibrator: Vibrator? = null

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Optional: trigger a test beat from intent extras
        if (intent != null && intent.hasExtra("test_beat")) {
            val path = intent.getStringExtra("test_beat")
            simulateBeat(path)
        }
        return START_STICKY
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                METRONOME_STATE_PATH == event.dataItem.uri.path
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val isPlaying = dataMap.getBoolean("isPlaying")
                val bpm = dataMap.getInt("bpm")
                val beatsPerBar = dataMap.getInt("beatsPerBar", 4)
                val beatDenominator = dataMap.getInt("beatDenominator", 4) // Pulls 4, 8, etc.

                val intent = Intent(ACTION_METRONOME_SYNC)
                intent.setPackage(this.packageName) // Explicitly target your own app
                intent.putExtra("isPlaying", isPlaying)
                intent.putExtra("bpm", bpm)
                intent.putExtra("beatsPerBar", beatsPerBar)
                intent.putExtra("beatDenominator", beatDenominator)

sendBroadcast(intent)

            }
        }
    }

    /**
     * Call this to simulate a beat for emulator testing
     */
    fun simulateBeat(path: String?) {
        if (BEAT_PATH_TICK == path) {
            triggerVibration(true) // main beat
        } else if (BEAT_PATH_TOCK == path) {
            triggerVibration(false) // offbeat
        }
    }

    @Suppress("DEPRECATION")
    private fun triggerVibration(mainBeat: Boolean) {
        if (vibrator == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (mainBeat)
                VibrationEffect.createOneShot(tickLength.toLong(), tickAmplitude)
            else
                VibrationEffect.createOneShot(tockLength.toLong(), tockAmplitude) // weaker offbeat
            vibrator!!.vibrate(effect)
        } else {
            vibrator!!.vibrate((if (mainBeat) 50 else 30).toLong())
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (BEAT_PATH_TICK == event.path) {
            triggerVibration(50, 255)
        } else if (BEAT_PATH_TOCK == event.path) {
            triggerVibration(30, 120)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun triggerVibration(length: Int, amplitude: Int) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?
        if (vibrator != null && vibrator.hasVibrator()) {
            // Short, crisp vibration for each beat
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(length.toLong(), amplitude))
            } else {
                vibrator.vibrate(length.toLong()) // fallback for older devices
            }
        }
    }


    @Suppress("UNUSED")
    companion object {
        private const val TAG = "MetronomeService"
        const val BEAT_PATH_TICK: String = "/metronome/beat/tick"
        const val BEAT_PATH_TOCK: String = "/metronome/beat/tock"
        private const val METRONOME_STATE_PATH = "/metronome/state"
        const val ACTION_METRONOME_SYNC: String =
            "com.garethevans.church.opensongtablet.METRONOME_SYNC"
    }
}
